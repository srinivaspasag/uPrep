import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import {
  loadFoldersForOrgs,
  collectSubtreeIds,
  courseRootOf,
} from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Student-facing course access. Enforces enrollment server-side:
//   GET                 -> the courses the caller is enrolled in
//   GET ?folderId=<id>  -> contents of a folder, ONLY if it lives inside a
//                          course the caller is enrolled in (staff see all).
//
// Access is derived from the server-trusted session cookie, not client input,
// so a student cannot read another course by guessing folder ids.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session)
    return NextResponse.json({ error: "Please log in", courses: [] }, { status: 401 });

  const orgId = session.orgId;
  const staff = isStaff(session.profile);
  const folderId = req.nextUrl.searchParams.get("folderId");

  try {
    const db = await getDb();
    // Catalog = the org's own courses + courses granted to it. Folders are
    // loaded across every owner org so granted subtrees resolve correctly.
    const catalog = await resolveCourseCatalog(db, orgId);
    const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
    const allCourseRoots = catalog.map((c) => c.id);

    // Enrolled course roots (staff preview everything).
    let enrolledRoots: string[];
    if (staff) {
      enrolledRoots = allCourseRoots;
    } else {
      const m: any = ObjectId.isValid(session.id)
        ? await db.collection("orgmembers").findOne({ _id: new ObjectId(session.id) }).catch(() => null)
        : null;
      const ids: string[] = Array.isArray(m?.enrolledCourseIds) ? m.enrolledCourseIds : [];
      // Only keep ids that are still valid top-level courses.
      enrolledRoots = ids.filter((id) => allCourseRoots.includes(id));
    }

    // Browse mode: list one folder's children, gated by enrollment.
    if (folderId) {
      const root = courseRootOf(folders, folderId);
      if (!root || !enrolledRoots.includes(root))
        return NextResponse.json({ error: "Not enrolled in this course" }, { status: 403 });

      const subfolders = folders
        .filter((f) => f.parentId === folderId)
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((f) => ({ id: f.id, name: f.name, type: "FOLDER" as const }));

      // Students only see content marked visible (hidden !== true). Staff see all.
      const visClause = staff ? {} : { hidden: { $ne: true } };
      const contentItems: any[] = [];
      for (const coll of ["videos", "documents", "tests"]) {
        const docs = await db
          .collection(coll)
          .find({ folderId, recordState: "ACTIVE", ...visClause } as any)
          .sort({ name: 1 })
          .toArray();
        for (const d of docs as any[]) {
          contentItems.push({
            id: String(d._id),
            name: d.name || d.title || "(untitled)",
            type: d.type || (coll === "videos" ? "VIDEO" : coll === "documents" ? "DOCUMENT" : "TEST"),
            url: d.url ?? null,
            embedUrl: d.embedUrl ?? null,
            provider: d.provider ?? null,
          });
        }
      }

      const here = folders.find((f) => f.id === folderId);
      return NextResponse.json({
        folder: here ? { id: here.id, name: here.name, parentId: here.parentId } : null,
        courseRootId: root,
        subfolders,
        items: contentItems,
      });
    }

    // Course list mode.
    const courses = enrolledRoots
      .map((id) => folders.find((f) => f.id === id))
      .filter(Boolean)
      .map((f) => {
        const subtree = collectSubtreeIds(folders, [f!.id]);
        return {
          id: f!.id,
          name: f!.name,
          chapterCount: folders.filter((x) => x.parentId === f!.id).length,
          folderCount: subtree.size,
        };
      });

    return NextResponse.json({ courses, staff });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed", courses: [] }, { status: 500 });
  }
}

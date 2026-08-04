import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import {
  loadFoldersForOrgs,
  collectSubtreeIds,
  courseRootOf,
  orderThenNatural,
  naturalCompare,
} from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";
import { resolveStudentEnrollment } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// This response depends entirely on the caller's auth cookie (a student and
// staff hitting the same URL get different course lists) — without an
// explicit no-store, a shared/browser cache has no signal that it must not
// reuse one caller's response for another.
const NO_STORE = { "Cache-Control": "no-store, private", Vary: "Cookie" };

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

    // Enrolled course roots (staff preview everything). A student's access is
    // the union of direct course assignment, Program grant, and Section grant
    // — see resolveStudentEnrollment for the full union logic (shared with
    // /api/library so both surfaces agree on what a student can see).
    let enrolledRoots: string[];
    let studentProgramIds: string[] = []; // used below to scope content visibility too
    let studentSectionIds: string[] = []; // ditto, for section-level publishing
    let programGroups: {
      id: string;
      name: string;
      courseIds: string[];
      centerName: string | null;
      sectionName: string | null;
    }[] = [];
    if (staff) {
      enrolledRoots = allCourseRoots;
    } else {
      const enrollment = await resolveStudentEnrollment(db, session.id, allCourseRoots);
      enrolledRoots = enrollment.enrolledRoots;
      studentProgramIds = enrollment.studentProgramIds;
      studentSectionIds = enrollment.studentSectionIds;
      programGroups = enrollment.programGroups;
    }

    // Browse mode: list one folder's children, gated by enrollment.
    if (folderId) {
      const root = courseRootOf(folders, folderId);
      if (!root || !enrolledRoots.includes(root))
        return NextResponse.json({ error: "Not enrolled in this course" }, { status: 403 });

      const rawSubfolders = orderThenNatural(folders.filter((f) => f.parentId === folderId));

      // Students only see content marked visible (hidden !== true). On top of
      // that, an item is visible if it has NO scoping at all (today's default),
      // OR the student's program is in its program allow-list, OR the student's
      // section is in its published-sections list (visibleSectionIds — the
      // section-level publish workflow: being added to a section does not
      // itself make content visible, only an explicit "Make Visible" does),
      // OR this specific student has an individual "publish to a student"
      // override (visibleUserIds) — and hiddenUserIds excludes this specific
      // student even if everything else says visible (an individual
      // "unpublish from a student" override). Staff see all.
      const visClause = staff
        ? {}
        : {
            hidden: { $ne: true },
            hiddenUserIds: { $nin: [session.id] },
            $or: [
              { visibleUserIds: { $in: [session.id] } },
              { visibleProgramIds: { $exists: false } },
              { visibleProgramIds: { $size: 0 } },
              { visibleProgramIds: { $in: studentProgramIds } },
              { visibleSectionIds: { $in: studentSectionIds } },
            ],
          };
      // Legacy's real chapter row (Library/tags/library/chapter.html) shows
      // Lectures/E-Books/Tests counts right on the row — ours used to be a
      // bare "📁 name" folder tile with no indication of what's inside until
      // you clicked in. Mirrors the same subtree-count approach already used
      // for the top-level subject cards.
      const subfolders = await Promise.all(
        rawSubfolders.map(async (f) => {
          const subtree = Array.from(collectSubtreeIds(folders, [f.id]));
          const [videoCount, documentCount, testCount] = await Promise.all([
            db.collection("videos").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("documents").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("tests").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
          ]);
          return { id: f.id, name: f.name, type: "FOLDER" as const, videoCount, documentCount, testCount };
        })
      );

      const contentItems: any[] = [];
      for (const coll of ["videos", "documents", "tests"]) {
        const docs = await db
          .collection(coll)
          .find({ folderId, recordState: "ACTIVE", ...visClause } as any)
          .toArray();
        // Same sequencing bug as folders (e.g. "Part 10" sorting before
        // "Part 2") — explicit `order` wins, otherwise natural sort by name.
        const sorted = (docs as any[]).sort((a, b) => {
          const ao = typeof a.order === "number" ? a.order : null;
          const bo = typeof b.order === "number" ? b.order : null;
          if (ao !== null && bo !== null) return ao - bo;
          if (ao !== null) return -1;
          if (bo !== null) return 1;
          return naturalCompare(String(a.name || a.title || ""), String(b.name || b.title || ""));
        });
        for (const d of sorted) {
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
      return NextResponse.json(
        {
          folder: here ? { id: here.id, name: here.name, parentId: here.parentId } : null,
          courseRootId: root,
          subfolders,
          items: contentItems,
        },
        { headers: NO_STORE }
      );
    }

    // Course list mode. Legacy's real subject card (Library/subject.html)
    // shows a Chapters / E-Books / Tests count per subject — this mirrors
    // that instead of the bare chapter count the rebuild originally shipped.
    const visClause = staff
      ? {}
      : {
          hidden: { $ne: true },
          hiddenUserIds: { $nin: [session.id] },
          $or: [
            { visibleUserIds: { $in: [session.id] } },
            { visibleProgramIds: { $exists: false } },
            { visibleProgramIds: { $size: 0 } },
            { visibleProgramIds: { $in: studentProgramIds } },
            { visibleSectionIds: { $in: studentSectionIds } },
          ],
        };
    const courses = await Promise.all(
      enrolledRoots
        .map((id) => folders.find((f) => f.id === id))
        .filter(Boolean)
        .map(async (f) => {
          const subtree = Array.from(collectSubtreeIds(folders, [f!.id]));
          const [videoCount, documentCount, testCount] = await Promise.all([
            db.collection("videos").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("documents").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
            db.collection("tests").countDocuments({ folderId: { $in: subtree }, recordState: "ACTIVE", ...visClause } as any),
          ]);
          return {
            id: f!.id,
            name: f!.name,
            chapterCount: folders.filter((x) => x.parentId === f!.id).length,
            folderCount: subtree.length,
            videoCount,
            documentCount,
            testCount,
          };
        })
    );

    return NextResponse.json({ courses, staff, programGroups }, { headers: NO_STORE });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed", courses: [] }, { status: 500 });
  }
}

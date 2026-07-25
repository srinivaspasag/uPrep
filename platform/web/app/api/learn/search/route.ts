import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import { loadFoldersForOrgs, collectSubtreeIds } from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Global content search. The legacy browse is ElasticSearch-backed (not indexed
// on this stack), so we do a name regex across the content collections in Mongo.
//
// Gated by the server-trusted session: a student only searches content inside
// the courses they're enrolled in; staff (and anonymous preview) search the
// whole org. This mirrors /api/library so search can't leak other orgs' or
// unenrolled content.
export async function GET(req: NextRequest) {
  const q = (req.nextUrl.searchParams.get("q") || "").trim();
  if (!q) return NextResponse.json({ results: [] });

  const rx = { $regex: q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), $options: "i" };
  const colls: { coll: string; type: string }[] = [
    { coll: "tests", type: "TEST" },
    { coll: "modules", type: "MODULE" },
    { coll: "documents", type: "DOCUMENT" },
    { coll: "videos", type: "VIDEO" },
    { coll: "questionsets", type: "QUESTION_SET" },
    { coll: "discussions", type: "DISCUSSION" },
  ];

  try {
    const db = await getDb();

    const session = await sessionFromReq(req);
    const isStudent = !!session && !isStaff(session.profile);
    const orgId = isStudent
      ? session!.orgId
      : req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;

    // Enrollment gate for students -> restrict to the subtree of enrolled courses.
    let allowedFolderIds: string[] | null = null;
    if (isStudent) {
      const catalog = await resolveCourseCatalog(db, orgId);
      const courseRoots = catalog.map((c) => c.id);
      const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
      const m: any = ObjectId.isValid(session!.id)
        ? await db.collection("orgmembers").findOne({ _id: new ObjectId(session!.id) }).catch(() => null)
        : null;
      const enrolled = (Array.isArray(m?.enrolledCourseIds) ? m.enrolledCourseIds : []).filter(
        (id: string) => courseRoots.includes(id)
      );
      allowedFolderIds = Array.from(collectSubtreeIds(folders, enrolled));
    }

    const results: any[] = [];
    for (const { coll, type } of colls) {
      const filter: any = { recordState: "ACTIVE", name: rx };
      if (allowedFolderIds) filter.folderId = { $in: allowedFolderIds };
      else filter["contentSrc.id"] = orgId;

      const docs = await db.collection(coll).find(filter).limit(20).toArray();
      for (const d of docs as any[]) {
        results.push({
          id: String(d._id),
          name: d.name || "(untitled)",
          type,
          subject: d.subject || null,
          url: d.url || null,
        });
      }
    }
    return NextResponse.json({ results, query: q });
  } catch (e: any) {
    return NextResponse.json({ results: [], error: e?.message }, { status: 500 });
  }
}

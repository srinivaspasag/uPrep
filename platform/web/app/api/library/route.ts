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

function collForType(type: string): string {
  const m: Record<string, string> = {
    TEST: "tests",
    MODULE: "modules",
    DOCUMENT: "documents",
    VIDEO: "videos",
  };
  return m[type] || "tests";
}

function defaultTypeForColl(coll: string): string {
  const m: Record<string, string> = {
    tests: "TEST",
    modules: "MODULE",
    documents: "DOCUMENT",
    videos: "VIDEO",
  };
  return m[coll] || "TEST";
}

type LibraryItem = {
  id: string;
  name: string;
  type: string;
  questionCount: number;
  durationMin: number;
  totalMarks: number;
  difficulty: string | null;
  url?: string | null;
  embedUrl?: string | null;
  provider?: string | null;
  linkType?: string | null;
};

// Lists browsable content (tests + modules) for an org directly from MongoDB.
// The legacy browse endpoints are ElasticSearch-backed and aren't indexed on
// this stack, so we read Mongo directly for the demo. Detail/take-test still
// use the real content service.
export async function GET(req: NextRequest) {
  const typeParam = req.nextUrl.searchParams.get("type"); // optional: TEST | MODULE

  try {
    const db = await getDb();

    // Enrollment gate: a student sees only content inside the courses they're
    // enrolled in (staff / anonymous preview see the whole org library). The
    // org + role come from the server-trusted session, not query params.
    const session = await sessionFromReq(req);
    const isStudent = !!session && !isStaff(session.profile);
    const orgId = isStudent
      ? session!.orgId
      : req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;

    let allowedFolderIds: string[] | null = null;
    if (isStudent) {
      // Catalog = own + granted courses; load folders across every owner org so
      // granted (cross-org) course subtrees resolve.
      const catalog = await resolveCourseCatalog(db, orgId);
      const courseRoots = catalog.map((c) => c.id);
      const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
      const m: any = ObjectId.isValid(session!.id)
        ? await db.collection("orgmembers").findOne({ _id: new ObjectId(session!.id) }).catch(() => null)
        : null;
      const enrolled = (Array.isArray(m?.enrolledCourseIds) ? m.enrolledCourseIds : []).filter(
        (id: string) => courseRoots.includes(id)
      );
      // Restrict to the subtree of enrolled courses. No enrollment => empty set.
      allowedFolderIds = Array.from(collectSubtreeIds(folders, enrolled));
    }

    const filter: Record<string, unknown> = { recordState: "ACTIVE" };
    if (allowedFolderIds) {
      // Students are scoped purely by folder id — granted content lives under a
      // different provider org, so we must NOT restrict by contentSrc.id here.
      filter.folderId = { $in: allowedFolderIds };
    } else {
      filter["contentSrc.id"] = orgId;
    }
    // Visibility gate: content flagged hidden (Make Invisible on learn/device)
    // disappears for students. Absent flag = visible (back-compat). Staff preview
    // everything so they can manage what's hidden.
    if (isStudent) filter.hidden = { $ne: true };

    const allCollections = ["tests", "modules", "documents", "videos"];
    const collections = typeParam
      ? allCollections.filter((c) => collForType(typeParam) === c)
      : allCollections;
    const items: LibraryItem[] = [];

    for (const coll of collections) {
      const docs = await db
        .collection(coll)
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(100)
        .toArray();

      for (const d of docs as any[]) {
        items.push({
          id: String(d._id),
          name: d.name || d.title || "(untitled)",
          type: d.type || defaultTypeForColl(coll),
          questionCount: d.actualQusCount ?? d.qusCount ?? 0,
          durationMin: d.duration ? Math.round(d.duration / 60000) : 0,
          totalMarks: d.totalMarks ?? 0,
          difficulty: d.difficulty ?? null,
          url: d.url ?? null,
          embedUrl: d.embedUrl ?? null,
          provider: d.provider ?? null,
          linkType: d.linkType ?? null,
        });
      }
    }

    return NextResponse.json({ items, orgId });
  } catch (e: any) {
    return NextResponse.json(
      { items: [], error: e?.message || "Failed to load library" },
      { status: 500 }
    );
  }
}

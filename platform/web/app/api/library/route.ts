import { NextRequest, NextResponse } from "next/server";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import { loadFoldersForOrgs, collectSubtreeIds } from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";
import { resolveStudentEnrollment } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

function collForType(type: string): string {
  const m: Record<string, string> = {
    TEST: "tests",
    MODULE: "modules",
    DOCUMENT: "documents",
    BOOK: "books",
    VIDEO: "videos",
  };
  return m[type] || "tests";
}

function defaultTypeForColl(coll: string): string {
  const m: Record<string, string> = {
    tests: "TEST",
    modules: "MODULE",
    documents: "DOCUMENT",
    books: "BOOK",
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
  lastUpdated: number;
};

// Lists browsable content (tests + modules) for an org directly from MongoDB.
// The legacy browse endpoints are ElasticSearch-backed and aren't indexed on
// this stack, so we read Mongo directly for the demo. Detail/take-test still
// use the real content service.
export async function GET(req: NextRequest) {
  const typeParam = req.nextUrl.searchParams.get("type"); // optional: TEST | MODULE
  // Bug found live: a student's enrolled-course subtree (allowedFolderIds)
  // covers every subject/chapter folder, so the default $or below matched
  // the SAME content already browsable via the subject/chapter tree at
  // /learn/courses — "Other Shared Content" was showing duplicates of
  // Program videos, not the untagged/loose leftovers it's meant for. This
  // flag (used by components/LibrarySection.tsx only — app/learn/playlists
  // still wants the full catalog, so it stays the default) drops the
  // folderId grant and keeps just the section/user "Make Visible" grants —
  // content that genuinely isn't reachable through the subject tree.
  const onlyLoose = req.nextUrl.searchParams.get("onlyLoose") === "1";

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
    let studentSectionIds: string[] = [];
    if (isStudent) {
      // Catalog = own + granted courses; load folders across every owner org so
      // granted (cross-org) course subtrees resolve. Enrollment union (direct +
      // Program + Section grants) is shared with /api/learn/courses so both
      // surfaces agree on exactly what a student can see.
      const catalog = await resolveCourseCatalog(db, orgId);
      const courseRoots = catalog.map((c) => c.id);
      const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
      const enrollment = await resolveStudentEnrollment(db, session!.id, courseRoots);
      // Restrict to the subtree of enrolled courses. No enrollment => empty set.
      allowedFolderIds = Array.from(collectSubtreeIds(folders, enrollment.enrolledRoots));
      studentSectionIds = enrollment.studentSectionIds;
    }

    const filter: Record<string, unknown> = { recordState: "ACTIVE" };
    if (allowedFolderIds) {
      // Two independent grants: content inside a course folder the student is
      // enrolled in, OR a loose item explicitly added to and published
      // ("Make Visible") for their Section regardless of which folder (if
      // any) it's filed under. Previously only the folder path was checked,
      // so section-visible content filed outside a granted course (e.g. a
      // scratch/misc folder) was unreachable no matter what an admin marked
      // Visible — see the "Programs > Content > Add Content" admin flow.
      const grant: Record<string, unknown>[] = onlyLoose ? [] : [{ folderId: { $in: allowedFolderIds } }];
      if (studentSectionIds.length) grant.push({ visibleSectionIds: { $in: studentSectionIds } });
      // Per-student "publish to a student" override — visible to this one
      // student even outside the normal folder/section grant.
      grant.push({ visibleUserIds: { $in: [session!.id] } });
      filter.$or = grant;
    } else {
      filter["contentSrc.id"] = orgId;
    }
    // Visibility gate: content flagged hidden (Make Invisible on learn/device)
    // disappears for students. Absent flag = visible (back-compat). Staff preview
    // everything so they can manage what's hidden. hiddenUserIds is the
    // per-student "unpublish from a student" override — excludes this one
    // student even if everything else says visible.
    if (isStudent) {
      filter.hidden = { $ne: true };
      filter.hiddenUserIds = { $nin: [session!.id] };
    }

    const allCollections = ["tests", "modules", "documents", "books", "videos"];
    const collections = typeParam
      ? allCollections.filter((c) => collForType(typeParam) === c)
      : allCollections;
    const items: LibraryItem[] = [];

    for (const coll of collections) {
      const docs = await db
        .collection(coll)
        .find(filter)
        .sort({ lastUpdated: -1 })
        .limit(150)
        .toArray();

      for (const d of docs as any[]) {
        items.push({
          id: String(d._id),
          name: d.name || d.title || "(untitled)",
          type: d.type || defaultTypeForColl(coll),
          // Bug found live: `actualQusCount` is hardcoded to 0 at test
          // creation (see POST /api/cmds/tests) and never updated anywhere,
          // so `actualQusCount ?? qusCount` always evaluated to 0 — `??`
          // only falls through on null/undefined, not a real zero. Every
          // test in the student Library showed "0 q" regardless of its
          // actual question count.
          questionCount: d.qusCount ?? d.actualQusCount ?? 0,
          durationMin: d.duration ? Math.round(d.duration / 60000) : 0,
          totalMarks: d.totalMarks ?? 0,
          difficulty: d.difficulty ?? null,
          url: d.url ?? null,
          embedUrl: d.embedUrl ?? null,
          provider: d.provider ?? null,
          linkType: d.linkType ?? null,
          lastUpdated: Number(d.lastUpdated) || 0,
        });
      }
    }

    // Bug found live: each collection was queried and sorted independently,
    // then just concatenated in a fixed order (all tests, then all modules,
    // then all documents, then all videos) — "Recently Added" was never
    // actually sorted by recency across types at all, so e.g. any tests
    // (how ever few) always buried every video behind them regardless of
    // which was newer. Re-sort the merged list by real recency.
    items.sort((a, b) => b.lastUpdated - a.lastUpdated);

    return NextResponse.json(
      { items, orgId },
      { headers: { "Cache-Control": "no-store, private", Vary: "Cookie" } }
    );
  } catch (e: any) {
    return NextResponse.json(
      { items: [], error: e?.message || "Failed to load library" },
      { status: 500 }
    );
  }
}

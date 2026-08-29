import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { sessionFromReq } from "@/lib/server-session";
import { loadOrgFolders, collectSubtreeIds } from "@/lib/courses";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Seller Dashboard "distribution groups" — legacy's SDCardGroup, simplified
// to pure content bookkeeping (see plan: legacy's own SDCardManager never
// copies bytes either, it only links content to a virtual card/group; the
// actual physical media prep happens outside this system).
const GROUPS_COLL = "sellergroups";

function requireManager(req: NextRequest) {
  return sessionFromReq(req).then((s) => (s?.profile || "").trim().toUpperCase() === "MANAGER");
}

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(GROUPS_COLL)
      .find({ orgId } as any)
      .sort({ createdAt: -1 })
      .toArray();
    return NextResponse.json({
      groups: (docs as any[]).map((g) => ({
        id: String(g._id),
        name: g.name,
        itemCount: Array.isArray(g.contentIds) ? g.contentIds.length : 0,
        createdAt: g.createdAt,
        programId: g.programId || null,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ groups: [], error: e?.message }, { status: 500 });
  }
}

type CreateBody = { name?: string; contentIds?: string[]; programId?: string };

// Every content collection a group can pull from — same set app/api/cmds/content
// aggregates across, minus FOLDER (never a real downloadable item).
const CONTENT_COLLECTIONS = ["documents", "videos", "books", "tests", "modules", "questionsets"];

// Resolve every content item belonging to a Program. Two independent paths,
// unioned together:
//
// Path 1 — explicit tagging (sectionIds / visibleProgramIds on the content
// doc itself). A small, deliberately-curated set some content may carry.
//
// Path 2 — course-folder access, which is what ACTUALLY gates what an
// enrolled student sees (app/api/learn/courses/route.ts, via
// lib/enrollment.ts's resolveStudentEnrollment): a Program's `courseIds`
// (unioned with its Sections' `courseIds` — same fold-in logic, since real
// data showed the Program's own list is often empty and the Section carries
// the real one) grants access to those folders' entire subtree, and content
// is filed there via `folderId` — a completely different field than
// `sectionIds`. Missing this path was a real bug found live: "Pack a whole
// Program" produced near-empty packages (2-9 items) for programs whose real
// content — thousands of videos/documents — lives here, the common case.
// `sectionIds`/`visibleProgramIds` tagging turned out to be the rare
// exception, not the norm.
//
// This is a point-in-time snapshot either way, matching how a manually-picked
// group already behaves — a card that's already been burned and shipped
// doesn't retroactively gain content added to the program afterward.
type CourseGroup = { courseId: string; courseName: string; itemIds: string[] };
type ProgramResolution = { programName: string; contentIds: string[]; courseGroups: CourseGroup[] };

// Same two-path resolution as before, but now also tracks which top-level
// Course each item came from — so the SD-card manifest (and the Android
// reader's UI) can show Program → Courses → Content instead of one flat
// file list, matching what a student actually sees browsing Learn → Courses
// online. Items reached only via Path 1 (sectionIds/visibleProgramIds
// tagging, no folderId) have no course to attach to and are left out of
// courseGroups — the flat contentIds list (unchanged) still includes them,
// so nothing is silently dropped, they just don't get a course bucket.
async function resolveProgramContentIds(db: any, orgId: string, programId: string): Promise<ProgramResolution> {
  const [program, sections] = await Promise.all([
    db.collection("orgprograms").findOne({ _id: new ObjectId(programId) } as any),
    db.collection("orgsections").find({ orgId, programId, recordState: "ACTIVE" } as any).toArray(),
  ]);
  const sectionIds = (sections as any[]).map((s) => String(s._id));
  const programName = program?.name || "(untitled program)";

  const ids = new Set<string>();

  for (const coll of CONTENT_COLLECTIONS) {
    const docs = await db
      .collection(coll)
      .find({
        "contentSrc.id": orgId,
        recordState: "ACTIVE",
        $or: [
          ...(sectionIds.length ? [{ sectionIds: { $in: sectionIds } }] : []),
          { visibleProgramIds: programId },
        ],
      } as any)
      .project({ _id: 1 })
      .toArray();
    for (const d of docs as any[]) ids.add(String(d._id));
  }

  const courseIds = new Set<string>(Array.isArray(program?.courseIds) ? program.courseIds : []);
  for (const s of sections as any[]) {
    for (const cid of Array.isArray(s.courseIds) ? s.courseIds : []) courseIds.add(cid);
  }

  const courseGroups: CourseGroup[] = [];
  if (courseIds.size > 0) {
    const folders = await loadOrgFolders(db, orgId);
    const folderNameById = new Map(folders.map((f) => [f.id, f.name]));
    for (const courseId of Array.from(courseIds)) {
      const subtree = Array.from(collectSubtreeIds(folders, [courseId]));
      if (subtree.length === 0) continue;
      const itemIds: string[] = [];
      for (const coll of ["documents", "videos", "books", "tests"]) {
        const docs = await db
          .collection(coll)
          .find({ folderId: { $in: subtree }, recordState: "ACTIVE", hidden: { $ne: true } } as any)
          .project({ _id: 1 })
          .toArray();
        for (const d of docs as any[]) {
          const sid = String(d._id);
          ids.add(sid);
          itemIds.push(sid);
        }
      }
      if (itemIds.length > 0) {
        courseGroups.push({ courseId, courseName: folderNameById.get(courseId) || "(untitled course)", itemIds });
      }
    }
  }

  return { programName, contentIds: Array.from(ids), courseGroups };
}

export async function POST(req: NextRequest) {
  if (!(await requireManager(req)))
    return NextResponse.json({ error: "Only institute admins can create distribution groups." }, { status: 403 });

  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  const orgId = await resolveOrgId(req, null);
  const session = await sessionFromReq(req);

  try {
    const db = await getDb();

    let contentIds: string[];
    let programId: string | null = null;
    let programName: string | null = null;
    let courseGroups: CourseGroup[] = [];
    if (b.programId && ObjectId.isValid(b.programId)) {
      programId = b.programId;
      const resolved = await resolveProgramContentIds(db, orgId, programId);
      contentIds = resolved.contentIds;
      programName = resolved.programName;
      courseGroups = resolved.courseGroups;
      if (contentIds.length === 0)
        return NextResponse.json({ error: "This program has no content in any of its sections yet." }, { status: 400 });
    } else {
      contentIds = Array.from(new Set((Array.isArray(b.contentIds) ? b.contentIds : []).map(String)));
      if (contentIds.length === 0) return NextResponse.json({ error: "Pick at least one content item" }, { status: 400 });
    }

    const _id = new ObjectId();
    await db.collection(GROUPS_COLL).insertOne({
      _id,
      orgId,
      name,
      contentIds,
      programId,
      programName,
      courseGroups,
      createdBy: session?.id || null,
      createdAt: Date.now(),
    });
    return NextResponse.json({ id: _id.toHexString(), name, itemCount: contentIds.length });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

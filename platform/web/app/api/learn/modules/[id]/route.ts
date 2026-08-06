import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";
import { isStaff } from "@/lib/roles";
import { loadFoldersForOrgs, collectSubtreeIds } from "@/lib/courses";
import { resolveCourseCatalog, catalogOwnerOrgs } from "@/lib/grants";
import { resolveStudentEnrollment } from "@/lib/enrollment";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Student-facing module viewer. Bug found live: a module could be assigned
// to a student (shows up in "Other Shared Content" via /api/library), but
// there was NOWHERE a student could actually open one — the only module
// detail route (/api/cmds/modules/[id]) is staff-only (canManageContent
// gate on both the API and the CmdsShell-wrapped page), so even a naive
// link would 403. This mirrors that route's content-resolution logic but
// gates access the same way /api/library does for the student's own view
// (enrollment/section/user visibility), not staff permission.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid module id" }, { status: 400 });

  try {
    const db = await getDb();
    const session = await sessionFromReq(req);
    const isStudent = !!session && !isStaff(session.profile);
    const orgId = isStudent ? session!.orgId : req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;

    const mod: any = await db.collection("modules").findOne({ _id: new ObjectId(id), recordState: "ACTIVE" });
    if (!mod) return NextResponse.json({ error: "Module not found" }, { status: 404 });

    if (isStudent) {
      const catalog = await resolveCourseCatalog(db, orgId);
      const courseRoots = catalog.map((c) => c.id);
      const folders = await loadFoldersForOrgs(db, catalogOwnerOrgs(orgId, catalog));
      const enrollment = await resolveStudentEnrollment(db, session!.id, courseRoots);
      const allowedFolderIds = new Set(collectSubtreeIds(folders, enrollment.enrolledRoots));

      const inGrantedFolder = mod.folderId && allowedFolderIds.has(mod.folderId);
      const sectionVisible =
        Array.isArray(mod.visibleSectionIds) && mod.visibleSectionIds.some((sid: string) => enrollment.studentSectionIds.includes(sid));
      const userVisible = Array.isArray(mod.visibleUserIds) && mod.visibleUserIds.includes(session!.id);
      const userHidden = Array.isArray(mod.hiddenUserIds) && mod.hiddenUserIds.includes(session!.id);
      const noScoping = !mod.folderId && (!mod.visibleSectionIds || mod.visibleSectionIds.length === 0);

      const allowed = !mod.hidden && !userHidden && (inGrantedFolder || sectionVisible || userVisible || noScoping);
      if (!allowed) return NextResponse.json({ error: "Not enrolled in this module" }, { status: 403 });
    }

    const contentIds: string[] = Array.isArray(mod.contentIds) ? mod.contentIds : [];
    const validOids = contentIds.filter((cid) => ObjectId.isValid(cid)).map((cid) => new ObjectId(cid));

    const [documents, videos, tests, questionsets] = validOids.length
      ? await Promise.all([
          db.collection("documents").find({ _id: { $in: validOids } }).toArray(),
          db.collection("videos").find({ _id: { $in: validOids } }).toArray(),
          db.collection("tests").find({ _id: { $in: validOids } }).toArray(),
          db.collection("questionsets").find({ _id: { $in: validOids } }).toArray(),
        ])
      : [[], [], [], []];

    const byId = new Map<string, any>();
    for (const d of documents as any[]) byId.set(String(d._id), { ...d, __type: "DOCUMENT" });
    for (const d of videos as any[]) byId.set(String(d._id), { ...d, __type: "VIDEO" });
    for (const d of tests as any[]) byId.set(String(d._id), { ...d, __type: "TEST" });
    for (const d of questionsets as any[]) byId.set(String(d._id), { ...d, __type: "QUESTION_SET" });

    function resolve(cid: string) {
      const d = byId.get(cid);
      if (!d) return null;
      return {
        id: cid,
        name: d.name || d.title || "(untitled)",
        type: d.__type as string,
        url: d.url ?? null,
        embedUrl: d.embedUrl ?? null,
        qusCount: d.qusCount ?? d.actualQusCount ?? undefined,
      };
    }
    const items = contentIds.map(resolve).filter((x): x is NonNullable<typeof x> => x !== null);

    const rawSessions: { name: string; contentIds: string[] }[] = Array.isArray(mod.sessions) ? mod.sessions : [];
    const groupedItems = rawSessions.map((s) => ({
      name: s.name,
      items: (s.contentIds || []).map(resolve).filter((x): x is NonNullable<typeof x> => x !== null),
    }));

    return NextResponse.json(
      {
        module: { id: String(mod._id), name: mod.name || "(untitled module)", subject: mod.subject || null },
        items,
        groupedItems,
      },
      { headers: { "Cache-Control": "no-store, private", Vary: "Cookie" } }
    );
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load module" }, { status: 500 });
  }
}

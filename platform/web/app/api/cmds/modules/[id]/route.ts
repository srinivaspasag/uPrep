import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import { canManageContent } from "@/lib/roles";

export const dynamic = "force-dynamic";

// Module detail — resolves a module's contentIds (which can point into any
// of documents/videos/tests/questionsets) into real, viewable items. Bug
// found live: modules had a creation form but no viewer at all, so a module
// row in Institute Resources was inert — no way to see what was actually
// inside one.
export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const session = await sessionFromReq(req);
  if (!canManageContent(session?.profile))
    return NextResponse.json({ error: "You don't have access to content management." }, { status: 403 });

  const id = params.id;
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid module id" }, { status: 400 });

  try {
    const db = await getDb();
    const mod: any = await db.collection("modules").findOne({ _id: new ObjectId(id) });
    if (!mod) return NextResponse.json({ error: "Module not found" }, { status: 404 });

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

    // Preserve the module's own ordering (contentIds is an ordered list),
    // and skip any id whose target was deleted since being added.
    function resolve(cid: string) {
      const d = byId.get(cid);
      if (!d) return null;
      return {
        id: cid,
        name: d.name || d.title || "(untitled)",
        type: d.__type as string,
        url: d.url ?? null,
        embedUrl: d.embedUrl ?? null,
        // See app/api/library/route.ts — actualQusCount is hardcoded to 0 at
        // creation and never updated, so it must never be preferred over
        // the real qusCount via `??` (0 isn't nullish).
        qusCount: d.qusCount ?? d.actualQusCount ?? undefined,
      };
    }
    const items = contentIds.map(resolve).filter((x): x is NonNullable<typeof x> => x !== null);

    // Sessions group the same contentIds into named chunks (Session 1,
    // Session 2, ...) — see ModuleForm. Resolved here the same way as the
    // flat `items` list, so the viewer can render session headers instead
    // of one undifferentiated list.
    const rawSessions: { name: string; contentIds: string[] }[] = Array.isArray(mod.sessions) ? mod.sessions : [];
    const groupedItems = rawSessions.map((s) => ({
      name: s.name,
      items: (s.contentIds || []).map(resolve).filter((x): x is NonNullable<typeof x> => x !== null),
    }));

    return NextResponse.json({
      module: {
        id: String(mod._id),
        name: mod.name || "(untitled module)",
        subject: mod.subject || null,
        boardIds: Array.isArray(mod.boardIds) ? mod.boardIds : [],
        contentIds,
        sessions: rawSessions,
        folderId: mod.folderId || null,
      },
      items,
      groupedItems,
      missingCount: contentIds.length - items.length,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to load module" }, { status: 500 });
  }
}

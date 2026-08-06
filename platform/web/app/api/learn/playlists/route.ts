import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";
import { sessionFromReq } from "@/lib/server-session";

export const dynamic = "force-dynamic";

// Playlists — a `playlists` collection ({name, description, items[], userId}).
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("playlists")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((p) => ({
        id: String(p._id),
        name: p.name,
        description: p.description || "",
        items: p.items || [],
        userName: p.userName || "Student",
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type CreateBody = {
  name?: string;
  description?: string;
  items?: any[];
};

// Security fix: userId/userName used to come straight from the request
// body (attributing ownership to whoever the client claimed), and PATCH had
// NO authorization check at all — any caller who knew a playlist's id could
// add/remove its items. Ownership now comes only from the session, and
// PATCH verifies the caller owns the playlist before mutating it.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Playlist name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("playlists").insertOne({
      _id,
      orgId: session.orgId || DEFAULT_ORG_ID,
      userId: session.id,
      userName: [session.firstName, session.lastName].filter(Boolean).join(" ") || "Student",
      name,
      description: (b.description || "").trim(),
      items: Array.isArray(b.items) ? b.items : [],
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to create playlist" }, { status: 500 });
  }
}

type PatchBody = { id?: string; addItem?: any; removeEntityId?: string };

export async function PATCH(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session) return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  const b = (await req.json().catch(() => ({}))) as PatchBody;
  if (!b.id || !ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    const playlist = await db.collection("playlists").findOne({ _id: new ObjectId(b.id) });
    if (!playlist) return NextResponse.json({ error: "Playlist not found" }, { status: 404 });
    if (playlist.userId !== session.id)
      return NextResponse.json({ error: "Not your playlist" }, { status: 403 });
    if (b.addItem) {
      await db
        .collection("playlists")
        .updateOne({ _id: new ObjectId(b.id) }, { $addToSet: { items: b.addItem }, $set: { lastUpdated: Date.now() } });
    } else if (b.removeEntityId) {
      await db
        .collection("playlists")
        .updateOne(
          { _id: new ObjectId(b.id) },
          { $pull: { items: { entityId: b.removeEntityId } } as any, $set: { lastUpdated: Date.now() } }
        );
    }
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

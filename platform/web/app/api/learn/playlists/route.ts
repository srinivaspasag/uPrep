import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

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
  orgId?: string;
  userId?: string;
  userName?: string;
  name?: string;
  description?: string;
  items?: any[];
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as CreateBody;
  const name = (b.name || "").trim();
  if (!name) return NextResponse.json({ error: "Playlist name is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("playlists").insertOne({
      _id,
      orgId: b.orgId || DEFAULT_ORG_ID,
      userId: b.userId || null,
      userName: b.userName || "Student",
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
  const b = (await req.json().catch(() => ({}))) as PatchBody;
  if (!b.id || !ObjectId.isValid(b.id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
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

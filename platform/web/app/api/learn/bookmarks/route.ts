import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Student bookmarks — a `bookmarks` collection ({userId, entityId, entityType,
// name, url}). Created on demand; not part of the legacy seed.
export async function GET(req: NextRequest) {
  const userId = req.nextUrl.searchParams.get("userId") || "";
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  if (!userId) return NextResponse.json({ items: [] });
  try {
    const db = await getDb();
    const docs = await db
      .collection("bookmarks")
      .find({ userId, orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((b) => ({
        id: String(b._id),
        entityId: b.entityId,
        entityType: b.entityType,
        name: b.name,
        url: b.url || null,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  userId?: string;
  orgId?: string;
  entityId?: string;
  entityType?: string;
  name?: string;
  url?: string | null;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const userId = b.userId || "";
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!userId || !b.entityId) return NextResponse.json({ error: "Missing fields" }, { status: 400 });
  try {
    const db = await getDb();
    // Toggle: remove if it exists, else add.
    const existing = await db
      .collection("bookmarks")
      .findOne({ userId, entityId: b.entityId, recordState: "ACTIVE" });
    if (existing) {
      await db
        .collection("bookmarks")
        .updateOne({ _id: existing._id }, { $set: { recordState: "INACTIVE" } });
      return NextResponse.json({ bookmarked: false });
    }
    const now = Date.now();
    await db.collection("bookmarks").insertOne({
      _id: new ObjectId(),
      userId,
      orgId,
      entityId: b.entityId,
      entityType: b.entityType || "CONTENT",
      name: b.name || "",
      url: b.url || null,
      recordState: "ACTIVE",
      timeCreated: now,
    });
    return NextResponse.json({ bookmarked: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed" }, { status: 500 });
  }
}

import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Challenge Channels — mirrors content-service :19013 /channels. The legacy
// stack has no `channels` collection seeded, so we manage it here directly.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("channels")
      .find({ "contentSrc.id": orgId, recordState: "ACTIVE" })
      .sort({ lastUpdated: -1 })
      .toArray();
    const channels = (docs as any[]).map((c) => ({
      id: String(c._id),
      name: c.name || "",
      contentCount: c.contentCount ?? 0,
    }));
    return NextResponse.json({ channels, orgId });
  } catch (e: any) {
    return NextResponse.json({ channels: [], error: e?.message }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as { name?: string; orgId?: string; userId?: string };
  const name = (b.name || "").trim();
  const orgId = b.orgId || DEFAULT_ORG_ID;
  if (!name) return NextResponse.json({ error: "Title is required" }, { status: 400 });

  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("channels").insertOne({
      _id,
      name,
      contentCount: 0,
      contentSrc: { type: "ORGANIZATION", id: orgId },
      userId: b.userId || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), name });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

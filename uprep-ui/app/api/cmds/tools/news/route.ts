import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// News feed — a `news` collection ({title, body, imageUrl}). Shown to students
// on /learn/news and managed here.
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("news")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(100)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((n) => ({
        id: String(n._id),
        title: n.title,
        body: n.body || "",
        imageUrl: n.imageUrl || null,
        at: n.timeCreated || 0,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = { orgId?: string; title?: string; body?: string; imageUrl?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const title = (b.title || "").trim();
  if (!title) return NextResponse.json({ error: "Title is required" }, { status: 400 });
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("news").insertOne({
      _id,
      orgId: b.orgId || DEFAULT_ORG_ID,
      title,
      body: (b.body || "").trim(),
      imageUrl: (b.imageUrl || "").trim() || null,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), title });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to post" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection("news")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}

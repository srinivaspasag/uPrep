import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { DEFAULT_ORG_ID } from "@/lib/config";

export const dynamic = "force-dynamic";

// Referrals — a `referrals` collection ({code, description, reward, uses}).
export async function GET(req: NextRequest) {
  const orgId = req.nextUrl.searchParams.get("orgId") || DEFAULT_ORG_ID;
  try {
    const db = await getDb();
    const docs = await db
      .collection("referrals")
      .find({ orgId, recordState: "ACTIVE" })
      .sort({ timeCreated: -1 })
      .limit(200)
      .toArray();
    return NextResponse.json({
      items: (docs as any[]).map((r) => ({
        id: String(r._id),
        code: r.code,
        description: r.description || "",
        reward: r.reward || "",
        uses: r.uses || 0,
      })),
    });
  } catch (e: any) {
    return NextResponse.json({ items: [], error: e?.message }, { status: 500 });
  }
}

type Body = { orgId?: string; code?: string; description?: string; reward?: string };

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const orgId = b.orgId || DEFAULT_ORG_ID;
  const code = (b.code || "").trim().toUpperCase() || `REF${Date.now().toString().slice(-6)}`;
  try {
    const db = await getDb();
    const now = Date.now();
    const _id = new ObjectId();
    await db.collection("referrals").insertOne({
      _id,
      orgId,
      code,
      description: (b.description || "").trim(),
      reward: (b.reward || "").trim(),
      uses: 0,
      recordState: "ACTIVE",
      timeCreated: now,
      lastUpdated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), code });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Failed to create referral" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  try {
    const db = await getDb();
    await db
      .collection("referrals")
      .updateOne({ _id: new ObjectId(id) }, { $set: { recordState: "INACTIVE", lastUpdated: Date.now() } });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}

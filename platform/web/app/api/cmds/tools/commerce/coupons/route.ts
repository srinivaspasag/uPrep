import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { COUPONS_COLL } from "@/lib/commerce";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    const docs = await db
      .collection(COUPONS_COLL)
      .find({ orgId } as any)
      .sort({ timeCreated: -1 })
      .toArray();
    const coupons = (docs as any[]).map((c) => ({
      id: String(c._id),
      code: c.code,
      percentOff: c.percentOff || null,
      amountOffCents: c.amountOffCents || null,
      active: c.active !== false,
      maxRedemptions: c.maxRedemptions ?? null,
      redeemed: c.redeemed || 0,
    }));
    return NextResponse.json({ coupons, orgId });
  } catch (e: any) {
    return NextResponse.json({ coupons: [], error: e?.message }, { status: 500 });
  }
}

type Body = {
  code?: string;
  percentOff?: number;
  amountOff?: number;
  maxRedemptions?: number | null;
  orgId?: string;
};

export async function POST(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as Body;
  const orgId = await resolveOrgId(req, b.orgId);
  const code = (b.code || "").trim().toUpperCase();
  if (!code) return NextResponse.json({ error: "Coupon code is required" }, { status: 400 });

  const percentOff = b.percentOff ? Math.max(1, Math.min(100, Math.round(b.percentOff))) : 0;
  const amountOffCents = b.amountOff ? Math.max(0, Math.round(b.amountOff * 100)) : 0;
  if (!percentOff && !amountOffCents)
    return NextResponse.json({ error: "Set a percent or amount discount" }, { status: 400 });

  try {
    const db = await getDb();
    const clash = await db.collection(COUPONS_COLL).findOne({ orgId, code } as any);
    if (clash) return NextResponse.json({ error: `Coupon "${code}" already exists` }, { status: 409 });

    const now = Date.now();
    const _id = new ObjectId();
    await db.collection(COUPONS_COLL).insertOne({
      _id,
      orgId,
      code,
      percentOff: percentOff || null,
      amountOffCents: amountOffCents || null,
      active: true,
      maxRedemptions: b.maxRedemptions ?? null,
      redeemed: 0,
      timeCreated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), code });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Create failed" }, { status: 500 });
  }
}

export async function DELETE(req: NextRequest) {
  const id = req.nextUrl.searchParams.get("id") || "";
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  try {
    const db = await getDb();
    await db.collection(COUPONS_COLL).updateOne({ _id: new ObjectId(id), orgId } as any, {
      $set: { active: false },
    });
    return NextResponse.json({ ok: true });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Delete failed" }, { status: 500 });
  }
}

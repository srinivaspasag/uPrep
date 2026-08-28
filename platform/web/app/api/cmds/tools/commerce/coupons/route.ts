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
    const coupons = (docs as any[]).map((c) => {
      const validUntilStr = c.validUntil
        ? c.validUntil instanceof Date
          ? c.validUntil.toISOString()
          : typeof c.validUntil === "number"
          ? new Date(c.validUntil).toISOString()
          : String(c.validUntil)
        : null;
      const isExpired = validUntilStr ? new Date(validUntilStr).getTime() < Date.now() : false;
      return {
        id: String(c._id),
        code: c.code,
        percentOff: c.percentOff || null,
        amountOffCents: c.amountOffCents || null,
        active: c.active !== false,
        maxRedemptions: c.maxRedemptions ?? null,
        validUntil: validUntilStr,
        isExpired,
        redeemed: c.redeemed || 0,
      };
    });
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
  validUntil?: string | null;
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

  let validUntilIso: string | null = null;
  if (b.validUntil && b.validUntil.trim()) {
    const raw = b.validUntil.trim();
    const dateToParse = /^\d{4}-\d{2}-\d{2}$/.test(raw) ? `${raw}T23:59:59.999Z` : raw;
    const parsedDate = new Date(dateToParse);
    if (isNaN(parsedDate.getTime())) {
      return NextResponse.json({ error: "Invalid validity date" }, { status: 400 });
    }
    validUntilIso = parsedDate.toISOString();
  }

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
      validUntil: validUntilIso,
      redeemed: 0,
      timeCreated: now,
    });
    return NextResponse.json({ id: _id.toHexString(), code, validUntil: validUntilIso });
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

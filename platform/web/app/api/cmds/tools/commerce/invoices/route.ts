import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { resolveOrgId } from "@/lib/org-scope";
import { INVOICES_COLL, fmtMoney } from "@/lib/commerce";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

export async function GET(req: NextRequest) {
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));
  const status = (req.nextUrl.searchParams.get("status") || "").toUpperCase();
  try {
    const db = await getDb();
    const filter: any = { orgId };
    if (status) filter.status = status;
    const docs = await db
      .collection(INVOICES_COLL)
      .find(filter)
      .sort({ timeCreated: -1 })
      .limit(500)
      .toArray();
    const invoices = (docs as any[]).map((i) => ({
      id: String(i._id),
      number: i.number,
      buyerName: i.buyerName || "",
      buyerId: i.memberId || "",
      courseName: i.courseName || "",
      amount: fmtMoney(i.amountCents, i.currency),
      amountCents: i.amountCents,
      currency: i.currency,
      couponCode: i.couponCode || null,
      status: i.status,
      gateway: i.gateway,
      createdAt: i.timeCreated,
      paidAt: i.paidAt || null,
    }));
    return NextResponse.json({ invoices, orgId });
  } catch (e: any) {
    return NextResponse.json({ invoices: [], error: e?.message }, { status: 500 });
  }
}

type PatchBody = { id?: string; action?: "markPaid" | "cancel" };

// Admin action: mark an invoice PAID (grants course enrollment) or CANCEL it.
export async function PATCH(req: NextRequest) {
  const b = (await req.json().catch(() => ({}))) as PatchBody;
  const id = String(b.id || "");
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid invoice id" }, { status: 400 });
  const orgId = await resolveOrgId(req, req.nextUrl.searchParams.get("orgId"));

  try {
    const db = await getDb();
    const inv: any = await db.collection(INVOICES_COLL).findOne({ _id: new ObjectId(id), orgId } as any);
    if (!inv) return NextResponse.json({ error: "Invoice not found" }, { status: 404 });

    if (b.action === "cancel") {
      await db
        .collection(INVOICES_COLL)
        .updateOne({ _id: inv._id }, { $set: { status: "CANCELLED", lastUpdated: Date.now() } });
      return NextResponse.json({ ok: true, status: "CANCELLED" });
    }

    // markPaid (idempotent).
    if (inv.status !== "PAID") {
      await db
        .collection(INVOICES_COLL)
        .updateOne({ _id: inv._id }, { $set: { status: "PAID", paidAt: Date.now(), lastUpdated: Date.now() } });

      // Grant enrollment for the purchased course.
      if (inv.userId && inv.courseId && ObjectId.isValid(inv.userId)) {
        const member: any = await db.collection("orgmembers").findOne({ _id: new ObjectId(inv.userId) });
        if (member) {
          const enrolled = new Set(
            (Array.isArray(member.enrolledCourseIds) ? member.enrolledCourseIds : []).map(String)
          );
          enrolled.add(String(inv.courseId));
          await db
            .collection("orgmembers")
            .updateOne({ _id: member._id }, { $set: { enrolledCourseIds: Array.from(enrolled), lastUpdated: Date.now() } });
        }
      }
      // Count the coupon redemption.
      if (inv.couponCode) {
        await db
          .collection("coupons")
          .updateOne({ orgId, code: inv.couponCode } as any, { $inc: { redeemed: 1 } });
      }
    }
    return NextResponse.json({ ok: true, status: "PAID" });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Update failed" }, { status: 500 });
  }
}

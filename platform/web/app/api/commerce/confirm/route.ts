import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { INVOICES_COLL } from "@/lib/commerce";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

type Body = { invoiceId?: string; secret?: string; status?: string };

// Payment-gateway callback (webhook provider). The relay posts here after a
// successful charge; we verify a shared secret, mark the invoice PAID and grant
// enrollment. Configure PAYMENT_WEBHOOK_SECRET to enable.
export async function POST(req: NextRequest) {
  const secret = process.env.PAYMENT_WEBHOOK_SECRET;
  const b = (await req.json().catch(() => ({}))) as Body;
  const headerSecret = req.headers.get("x-webhook-secret") || b.secret || "";
  if (!secret || headerSecret !== secret)
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const id = String(b.invoiceId || "");
  if (!ObjectId.isValid(id)) return NextResponse.json({ error: "Invalid invoice id" }, { status: 400 });

  try {
    const db = await getDb();
    const inv: any = await db.collection(INVOICES_COLL).findOne({ _id: new ObjectId(id) });
    if (!inv) return NextResponse.json({ error: "Invoice not found" }, { status: 404 });

    const paid = (b.status || "PAID").toUpperCase() === "PAID";
    if (!paid) {
      await db.collection(INVOICES_COLL).updateOne({ _id: inv._id }, { $set: { status: "FAILED", lastUpdated: Date.now() } });
      return NextResponse.json({ ok: true, status: "FAILED" });
    }

    if (inv.status !== "PAID") {
      await db
        .collection(INVOICES_COLL)
        .updateOne({ _id: inv._id }, { $set: { status: "PAID", paidAt: Date.now(), lastUpdated: Date.now() } });
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
      if (inv.couponCode)
        await db.collection("coupons").updateOne({ orgId: inv.orgId, code: inv.couponCode } as any, { $inc: { redeemed: 1 } });
    }
    return NextResponse.json({ ok: true, status: "PAID" });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Confirm failed" }, { status: 500 });
  }
}

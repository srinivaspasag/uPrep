import { NextRequest, NextResponse } from "next/server";
import { ObjectId } from "mongodb";
import { getDb } from "@/lib/mongo";
import { sessionFromReq } from "@/lib/server-session";
import {
  PRODUCTS_COLL,
  INVOICES_COLL,
  applyCoupon,
  startGatewayCheckout,
  paymentProvider,
  fmtMoney,
} from "@/lib/commerce";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

// Student storefront: list purchasable products for their org.
export async function GET(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.orgId) return NextResponse.json({ products: [] });
  try {
    const db = await getDb();
    const docs = await db
      .collection(PRODUCTS_COLL)
      .find({ orgId: session.orgId, recordState: "ACTIVE" } as any)
      .sort({ timeCreated: -1 })
      .toArray();

    const member: any = ObjectId.isValid(session.id)
      ? await db.collection("orgmembers").findOne({ _id: new ObjectId(session.id) })
      : null;
    const owned = new Set(
      (Array.isArray(member?.enrolledCourseIds) ? member.enrolledCourseIds : []).map(String)
    );

    const products = (docs as any[]).map((p) => ({
      id: String(p._id),
      courseId: p.courseId,
      name: p.name,
      priceCents: p.priceCents,
      price: fmtMoney(p.priceCents, p.currency || "INR"),
      currency: p.currency || "INR",
      owned: owned.has(String(p.courseId)),
    }));
    return NextResponse.json({ products });
  } catch (e: any) {
    return NextResponse.json({ products: [], error: e?.message }, { status: 500 });
  }
}

type Body = { productId?: string; couponCode?: string };

// Create an invoice for a product purchase. Free/manual invoices land as
// PENDING (admin confirms → enrollment). A webhook gateway returns a checkoutUrl.
export async function POST(req: NextRequest) {
  const session = await sessionFromReq(req);
  if (!session?.id) return NextResponse.json({ error: "Please sign in" }, { status: 401 });

  const b = (await req.json().catch(() => ({}))) as Body;
  const productId = String(b.productId || "");
  if (!ObjectId.isValid(productId)) return NextResponse.json({ error: "Invalid product" }, { status: 400 });

  try {
    const db = await getDb();
    const product: any = await db.collection(PRODUCTS_COLL).findOne({ _id: new ObjectId(productId) });
    if (!product || product.recordState !== "ACTIVE")
      return NextResponse.json({ error: "Product unavailable" }, { status: 404 });
    if (product.orgId !== session.orgId)
      return NextResponse.json({ error: "Product not available for your institute" }, { status: 403 });

    const userCouponCode = (b.couponCode || "").trim();
    let couponResult: { finalCents: number; coupon: any | null; discountCents: number; error?: string } = {
      finalCents: product.priceCents,
      coupon: null,
      discountCents: 0,
    };
    if (userCouponCode) {
      couponResult = await applyCoupon(
        db,
        session.orgId,
        product.priceCents,
        userCouponCode
      );
      if (!couponResult.coupon) {
        return NextResponse.json(
          { error: couponResult.error || "Invalid or expired coupon code" },
          { status: 400 }
        );
      }
    }
    const { finalCents, coupon, discountCents } = couponResult;

    const now = Date.now();
    const _id = new ObjectId();
    const number = `INV-${now.toString().slice(-8)}-${String(_id).slice(-4).toUpperCase()}`;
    const buyerName = [session.firstName, session.lastName].filter(Boolean).join(" ") || "Student";

    // Free (or fully-discounted) purchases are auto-paid and enrolled.
    const autoPaid = finalCents <= 0;

    const invoice = {
      _id,
      number,
      orgId: session.orgId,
      userId: session.id,
      memberId: session.memberId || null,
      buyerName,
      productId: String(product._id),
      courseId: product.courseId,
      courseName: product.name,
      amountCents: finalCents,
      currency: product.currency || "INR",
      couponCode: coupon?.code || null,
      discountCents,
      status: autoPaid ? "PAID" : "PENDING",
      gateway: autoPaid ? "free" : paymentProvider(),
      timeCreated: now,
      paidAt: autoPaid ? now : null,
    };
    await db.collection(INVOICES_COLL).insertOne(invoice);

    if (autoPaid) {
      const member: any = ObjectId.isValid(session.id)
        ? await db.collection("orgmembers").findOne({ _id: new ObjectId(session.id) })
        : null;
      if (member) {
        const enrolled = new Set(
          (Array.isArray(member.enrolledCourseIds) ? member.enrolledCourseIds : []).map(String)
        );
        enrolled.add(String(product.courseId));
        await db
          .collection("orgmembers")
          .updateOne({ _id: member._id }, { $set: { enrolledCourseIds: Array.from(enrolled), lastUpdated: now } });
      }
      if (coupon?.code)
        await db.collection("coupons").updateOne({ orgId: session.orgId, code: coupon.code } as any, { $inc: { redeemed: 1 } });
      return NextResponse.json({ invoiceId: String(_id), number, status: "PAID", enrolled: true });
    }

    const gw = await startGatewayCheckout({
      id: String(_id),
      amountCents: finalCents,
      currency: product.currency || "INR",
      description: product.name,
    });

    return NextResponse.json({
      invoiceId: String(_id),
      number,
      status: "PENDING",
      amount: fmtMoney(finalCents, product.currency || "INR"),
      checkoutUrl: gw?.checkoutUrl || null,
      manual: !gw,
    });
  } catch (e: any) {
    return NextResponse.json({ error: e?.message || "Checkout failed" }, { status: 500 });
  }
}

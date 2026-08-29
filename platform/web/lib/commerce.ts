import type { Db } from "mongodb";

// Lightweight commerce for the new stack: products (a course sold at a price),
// coupons, and invoices. Payment is provider-agnostic:
//   PAYMENT_PROVIDER = "manual" (default) | "webhook"
//   PAYMENT_WEBHOOK_URL -> POST invoice, returns { checkoutUrl } for a hosted
//                          gateway (Razorpay/Stripe/etc. via a small relay).
// In manual mode an admin marks invoices paid; enrollment is granted on PAID.

export const PRODUCTS_COLL = "products";
export const COUPONS_COLL = "coupons";
export const INVOICES_COLL = "invoices";

export type Coupon = {
  code: string;
  orgId: string;
  percentOff?: number;
  amountOffCents?: number;
  active: boolean;
  maxRedemptions?: number | null;
  validUntil?: string | number | Date | null;
  redeemed: number;
};

// Returns the discounted price (never below 0) and the matched coupon, if valid.
export async function applyCoupon(
  db: Db,
  orgId: string,
  priceCents: number,
  code?: string | null
): Promise<{ finalCents: number; coupon: any | null; discountCents: number; error?: string }> {
  if (!code) return { finalCents: priceCents, coupon: null, discountCents: 0 };
  const coupon: any = await db
    .collection(COUPONS_COLL)
    .findOne({ orgId, code: code.trim().toUpperCase(), active: true } as any);
  if (!coupon) return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Invalid coupon code" };

  if (coupon.validUntil) {
    const expiry = new Date(coupon.validUntil).getTime();
    if (!isNaN(expiry) && Date.now() > expiry)
      return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Coupon code has expired" };
  }

  if (coupon.maxRedemptions != null && (coupon.redeemed || 0) >= coupon.maxRedemptions)
    return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Coupon usage limit reached" };

  let discount = 0;
  if (coupon.percentOff) discount = Math.round((priceCents * coupon.percentOff) / 100);
  else if (coupon.amountOffCents) discount = coupon.amountOffCents;
  discount = Math.max(0, Math.min(discount, priceCents));
  return { finalCents: priceCents - discount, coupon, discountCents: discount };
}

export function paymentProvider(): string {
  return (process.env.PAYMENT_PROVIDER || "manual").toLowerCase();
}

// Hands the invoice to a hosted gateway relay and returns a checkout URL, when
// PAYMENT_PROVIDER=webhook. Manual mode returns null (admin confirms payment).
export async function startGatewayCheckout(invoice: {
  id: string;
  amountCents: number;
  currency: string;
  description: string;
}): Promise<{ checkoutUrl: string } | null> {
  if (paymentProvider() !== "webhook") return null;
  const url = process.env.PAYMENT_WEBHOOK_URL;
  if (!url) return null;
  try {
    const r = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(invoice),
    });
    if (!r.ok) return null;
    const d = await r.json().catch(() => ({}));
    return d?.checkoutUrl ? { checkoutUrl: d.checkoutUrl } : null;
  } catch {
    return null;
  }
}

export function fmtMoney(cents: number, currency = "INR"): string {
  const symbol = currency === "INR" ? "₹" : currency === "USD" ? "$" : "";
  return `${symbol}${(cents / 100).toFixed(2)}`;
}

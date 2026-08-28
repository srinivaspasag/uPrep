// Test script to verify applyCoupon validity handling
const assert = require("assert");

function applyCouponSim(couponDoc, priceCents, code) {
  if (!code) return { finalCents: priceCents, coupon: null, discountCents: 0 };
  if (!couponDoc || !couponDoc.active) return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Invalid coupon code" };

  if (couponDoc.validUntil) {
    const expiry = new Date(couponDoc.validUntil).getTime();
    if (!isNaN(expiry) && Date.now() > expiry)
      return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Coupon code has expired" };
  }

  if (couponDoc.maxRedemptions != null && (couponDoc.redeemed || 0) >= couponDoc.maxRedemptions)
    return { finalCents: priceCents, coupon: null, discountCents: 0, error: "Coupon usage limit reached" };

  let discount = 0;
  if (couponDoc.percentOff) discount = Math.round((priceCents * couponDoc.percentOff) / 100);
  else if (couponDoc.amountOffCents) discount = couponDoc.amountOffCents;
  discount = Math.max(0, Math.min(discount, priceCents));
  return { finalCents: priceCents - discount, coupon: couponDoc, discountCents: discount };
}

console.log("Running Coupon Validity Tests...\n");

// Test 1: Lifetime active coupon
const lifetimeCoupon = {
  code: "LIFETIME50",
  active: true,
  percentOff: 50,
  validUntil: null,
  redeemed: 0,
};
const res1 = applyCouponSim(lifetimeCoupon, 10000, "LIFETIME50");
assert.strictEqual(res1.finalCents, 5000);
assert.strictEqual(res1.discountCents, 5000);
assert.strictEqual(res1.coupon.code, "LIFETIME50");
console.log("✓ Test 1 Passed: Lifetime coupon applies 50% discount");

// Test 2: Active coupon with future expiration date
const futureDate = new Date(Date.now() + 86400000 * 7).toISOString(); // 7 days in future
const futureCoupon = {
  code: "FUTURE20",
  active: true,
  percentOff: 20,
  validUntil: futureDate,
  redeemed: 0,
};
const res2 = applyCouponSim(futureCoupon, 10000, "FUTURE20");
assert.strictEqual(res2.finalCents, 8000);
assert.strictEqual(res2.discountCents, 2000);
console.log("✓ Test 2 Passed: Future validity coupon applies 20% discount");

// Test 3: Expired coupon with past expiration date
const pastDate = new Date(Date.now() - 86400000).toISOString(); // 1 day in past
const expiredCoupon = {
  code: "EXPIRED50",
  active: true,
  percentOff: 50,
  validUntil: pastDate,
  redeemed: 0,
};
const res3 = applyCouponSim(expiredCoupon, 10000, "EXPIRED50");
assert.strictEqual(res3.finalCents, 10000);
assert.strictEqual(res3.coupon, null);
assert.strictEqual(res3.error, "Coupon code has expired");
console.log("✓ Test 3 Passed: Expired coupon rejected with 'Coupon code has expired'");

// Test 4: Flat amount discount with future validity
const amountCoupon = {
  code: "FLAT500",
  active: true,
  amountOffCents: 50000, // ₹500
  validUntil: futureDate,
  redeemed: 0,
};
const res4 = applyCouponSim(amountCoupon, 100000, "FLAT500");
assert.strictEqual(res4.finalCents, 50000);
assert.strictEqual(res4.discountCents, 50000);
console.log("✓ Test 4 Passed: Flat amount discount applied properly");

// Test 5: Inactive coupon
const inactiveCoupon = {
  code: "DISABLED",
  active: false,
  percentOff: 50,
  validUntil: futureDate,
};
const res5 = applyCouponSim(inactiveCoupon, 10000, "DISABLED");
assert.strictEqual(res5.finalCents, 10000);
assert.strictEqual(res5.error, "Invalid coupon code");
console.log("✓ Test 5 Passed: Inactive coupon rejected");

console.log("\nAll 5 coupon validity tests passed successfully!");

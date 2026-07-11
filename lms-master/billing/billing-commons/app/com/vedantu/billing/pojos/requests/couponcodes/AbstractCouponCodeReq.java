package com.vedantu.billing.pojos.requests.couponcodes;

import play.data.validation.Constraints.Required;

import com.vedantu.billing.enums.CouponType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public abstract class AbstractCouponCodeReq extends AbstractAuthCheckReq {
    @Required
    public String     orgId;

    @Required
    public String     code;

    @Required
    public CouponType couponType;

    public int        discountValue;     // for FLAT discount coupons
    public int        discountPercentage; // for PERCENTAGE coupons
    public String     currencyCode;      // e.g. INR

    public int        minPurchaseValue;
    public int        maxDiscount;
    public long       expiryTime;

    public int        maxUsageCount;

    public String validate() {
        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }

        if (null == code) {
            return "Code is missing";
        }
        if (null == couponType) {
            return "couponType is missing";
        }

        if (CouponType.FLAT.equals(couponType)) {
            if (discountValue <= 0) {
                return "discountValue is missing or invalid";
            }
        } else if (CouponType.PERCENTAGE.equals(couponType)) {
            if (discountPercentage <=0 || discountPercentage > 100) {
                return "discountPercentage is missing or invalid";
            }
        } else {
            return "Invalid Coupon Type";
        }

        if (maxUsageCount <= 0) {
            return "maxUsageCount is invalid";
        }
        return null;
    }
}

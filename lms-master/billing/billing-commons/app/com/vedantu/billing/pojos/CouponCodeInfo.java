package com.vedantu.billing.pojos;

import com.vedantu.billing.enums.CouponType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class CouponCodeInfo extends ModelBasicInfo {

    public String     code;
    public String     orgId;
    public String     creatorId;

    public CouponType couponType;        // FLAT or PERCENTAGE

    public int        discountValue;     // for FLAT discount coupons
    public int        discountPercentage; // for PERCENTAGE coupons
    public String     currencyCode;      // e.g. INR

    public int        minPurchaseValue;
    public int        maxDiscount;
    public long       expiryTime;

    public int        usageCount;
    public int        maxUsageCount;

    public boolean    expired;

    public CouponCodeInfo(String code, String orgId, String creatorId,
            CouponType couponType, int discountValue, int discountPercentage,
            String currencyCode, int minPurchaseValue, int maxDiscount,
            long expiryTime, int usageCount, int maxUsageCount, boolean expired) {
        this.code = code;
        this.orgId = orgId;
        this.creatorId = creatorId;
        this.couponType = couponType;
        this.discountValue = discountValue;
        this.discountPercentage = discountPercentage;
        this.currencyCode = currencyCode;
        this.minPurchaseValue = minPurchaseValue;
        this.maxDiscount = maxDiscount;
        this.expiryTime = expiryTime;
        this.usageCount = usageCount;
        this.maxUsageCount = maxUsageCount;
        this.expired = expired;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("CouponCodeInfo [code=");
        builder.append(code);
        builder.append(", orgId=");
        builder.append(orgId);
        builder.append(", couponType=");
        builder.append(couponType.toString());
        builder.append(", discountValue=");
        builder.append(discountValue);
        builder.append(", discountPercentage=");
        builder.append(discountPercentage);
        builder.append(", currencyCode=");
        builder.append(currencyCode);
        builder.append(", minPurchaseValue=");
        builder.append(minPurchaseValue);
        builder.append(", expired=");
        builder.append(expired);
        builder.append("]");
        return builder.toString();
    }

}

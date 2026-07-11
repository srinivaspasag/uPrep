package com.vedantu.billing.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.billing.enums.CouponType;
import com.vedantu.billing.pojos.CouponCodeInfo;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

/**
 * Model class for storing coupon code information.
 * Coupons can be of two types- Flat discount or Percentage discount.
 */
@Entity(value = "couponcodes", noClassnameStored = true)
public class CouponCode extends VedantuBaseMongoModel {
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

    public boolean    expired = false;

    @Override
    public ModelBasicInfo toBasicInfo() {
        CouponCodeInfo info = new CouponCodeInfo(code, orgId, creatorId,
                couponType, discountValue, discountPercentage, currencyCode,
                minPurchaseValue, maxDiscount, expiryTime, usageCount,
                maxUsageCount, expired);
        return info;
    }
}

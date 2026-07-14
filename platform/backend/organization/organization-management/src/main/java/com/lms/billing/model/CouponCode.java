package com.lms.billing.model;


import com.lms.billing.enums.CouponType;
import com.lms.billing.pojo.CouponCodeInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "couponcodes")
@Setter
@Getter
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

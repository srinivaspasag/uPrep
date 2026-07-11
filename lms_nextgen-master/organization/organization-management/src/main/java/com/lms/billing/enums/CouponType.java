package com.lms.billing.enums;

public enum CouponType {
    FLAT, PERCENTAGE, UNKNOWN;

    public static CouponType valueOfKey(String value) {
        CouponType couponType = UNKNOWN;
        try {
            couponType = CouponType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return couponType;
    }

}

package com.vedantu.billing.enums;

public enum PaymentType {
    CASH, CHEQUE, PAYTM, ETRANSFER, SWIPE;

    public static PaymentType valueOfKey(String value) {
        PaymentType paymentType = null;
        try {
             paymentType = PaymentType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return paymentType;
    }
}

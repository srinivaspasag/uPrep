package com.vedantu.billing.pojos;

public class PaymentInfo {

    public long   paymentTime;
    public String paymentMode;
    public String refNo;

    public PaymentInfo() {

        super();
    }

    public PaymentInfo(String paymentMode, String refNo) {

        super();
        this.paymentTime = System.currentTimeMillis();
        this.paymentMode = paymentMode;
        this.refNo = refNo;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{paymentTime:").append(paymentTime).append(", paymentMode:")
                .append(paymentMode).append(", refNo:").append(refNo).append("}");
        return builder.toString();
    }

}

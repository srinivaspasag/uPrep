package com.vedantu.billing.models;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.billing.enums.PaymentType;

@Embedded
public class PaymentItem {

    public PaymentType paymentType;

    public int amount;

    public String bankName;

    public long chequeNumber;

    public long payableDate;

    public String reference;

    public boolean isReceived;

    public long paymentTime;

    public PaymentItem() {
    }

    public PaymentItem(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PaymentItemInfo [type=");
        builder.append(paymentType);
        builder.append(", amount = ");
        builder.append(amount);
        builder.append(", bankName = ");
        builder.append(bankName);
        builder.append(", chequeNumber = ");
        builder.append(chequeNumber);
        builder.append(", reference = ");
        builder.append(reference);
        builder.append("]");
        return builder.toString();
    }
}

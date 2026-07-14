package com.vedantu.billing.pojos.responses;

import com.vedantu.billing.enums.TransactionStatus;

public class OnPaymentReceivedRes {

    public long              orderId;
    public String            transactionId;
    public TransactionStatus transactionStatus;

    public String            item_sku;
    public String            callbackUrl;

    public OnPaymentReceivedRes(long orderId, String transactionId,
            TransactionStatus transactionStatus, String item_sku, String callbackUrl) {

        super();
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.item_sku = item_sku;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{orderId:").append(orderId).append(", transactionId:")
                .append(transactionId).append(", transactionStatus:").append(transactionStatus)
                .append(", item_sku:").append(item_sku).append(", callbackUrl:")
                .append(callbackUrl).append("}");
        return builder.toString();
    }

}

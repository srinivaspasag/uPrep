package com.vedantu.billing.pojos.responses;

import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.SrcEntity;

public class GetTransactionStatusRes extends AbstractTransactionRes {

    public DeviceType        deviceType;
    public TransactionStatus transactionStatus;
    public SrcEntity         item;
    public String            item_sku;
    public String            callbackUrl;
    public boolean           consumed;
    public int               amount;

    public GetTransactionStatusRes(DeviceType deviceType, String transactionId, long orderId,
            TransactionStatus transactionStatus, SrcEntity item, String item_sku,
            String callbackUrl, boolean consumed, int amount) {

        super();
        this.deviceType = deviceType;
        this.transactionId = transactionId;
        this.orderId = String.valueOf(orderId);
        this.transactionStatus = transactionStatus;
        this.item = item;
        this.item_sku = item_sku;
        this.callbackUrl = callbackUrl;
        this.consumed = consumed;
        this.amount = amount;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{deviceType:").append(deviceType).append(", transactionStatus:")
                .append(transactionStatus).append(", item:").append(item).append(", item_sku:")
                .append(item_sku).append(", callbackUrl:").append(callbackUrl)
                .append(", consumed:").append(consumed).append(", amount:").append(amount)
                .append(", transactionId:").append(transactionId).append(", orderId:")
                .append(orderId).append("}");
        return builder.toString();
    }

}

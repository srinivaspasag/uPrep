package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;

public class PaymentReq {

    @Required
    public String transactionId;

    @Required
    public String userId;

    @Required
    public String paymentChannel; // CCAvenue/PayZippy
    @Required
    public String item_sku;      // if the caller provides these value they will be returned in the
                                  // callback url if specified
    @Required
    public String callbackUrl;

    @Required
    @Email
    public String email;

    @Required
    public String phone;

    @Required
    public long orderId;

    @Required
    public String orgId;

    @Required
    public String buyer_name;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{transactionId:").append(transactionId).append(", userId:").append(userId)
                .append(", paymentChannel:").append(paymentChannel).append(", item_sku:")
                .append(item_sku).append(", callbackUrl:").append(callbackUrl).append("}");
        return builder.toString();
    }

}

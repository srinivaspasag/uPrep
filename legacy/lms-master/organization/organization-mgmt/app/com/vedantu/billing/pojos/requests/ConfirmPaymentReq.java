package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

public class ConfirmPaymentReq {
    @Required
    public String orgId;
    @Required
    public String payment_request_id;
    @Required
    public String payment_status;
}

package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOrderItemInfoReq extends AbstractAuthCheckReq {

    @Required
    public long orderId;
}

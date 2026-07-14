package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GenerateOrderInfoReq extends AbstractOrgScopeReq {

    @Required
    public long orderId;

}

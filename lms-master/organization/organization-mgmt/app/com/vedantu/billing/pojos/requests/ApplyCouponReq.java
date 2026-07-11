package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ApplyCouponReq extends AbstractOrgScopeReq {

    @Required
    public String couponCode;

    @Required
    public long orderId;
}

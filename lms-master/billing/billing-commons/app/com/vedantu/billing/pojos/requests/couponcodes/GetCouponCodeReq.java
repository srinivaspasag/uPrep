package com.vedantu.billing.pojos.requests.couponcodes;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetCouponCodeReq extends AbstractAuthCheckReq {
    @Required
    public String orgId;

    @Required
    public String code;
}

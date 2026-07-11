package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ApplyLPCreditsReq extends AbstractOrgScopeReq {

    @Required
    public int  lpCredits;

    @Required
    public long orderId;

}

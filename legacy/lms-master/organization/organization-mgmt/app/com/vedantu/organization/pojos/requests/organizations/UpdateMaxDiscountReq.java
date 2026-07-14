package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateMaxDiscountReq extends AbstractAuthCheckReq {

    @Required
    public String sectionId;

    @Required
    public int maxDiscount;
}

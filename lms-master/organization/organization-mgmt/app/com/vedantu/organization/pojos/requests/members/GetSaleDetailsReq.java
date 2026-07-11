package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetSaleDetailsReq extends AbstractAuthCheckReq {

    @Required
    public String           orgId;
    @Required
    public String           targetUserId;
    @Required
    public String           sectionId;

}

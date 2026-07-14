package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateOrgRefererReq extends AbstractAuthCheckReq {

    public String  referer;
    public boolean remove;

    @Required
    public String  orgId;
}

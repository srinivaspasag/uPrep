package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateOrgSlugReq extends AbstractAuthCheckReq {

    @Required
    public String slug;

    @Required
    public String orgId;
}

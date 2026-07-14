package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class CheckSlugReq extends AbstractAppCheckReq {

    @Required
    public String slug;
}

package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class CheckRefererReq extends AbstractAppCheckReq {

    @Required
    public String referer;
}

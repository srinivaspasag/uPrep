package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GenerateAppCredentialsReq extends AbstractOrgScopeReq {

    @Required
    public String appId;
}

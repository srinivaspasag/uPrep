package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class VerifyAppCredentialsReq extends AbstractOrgScopeReq {

    @Required
    public String appId;

    @Required
    public String authToken;

    @Required
    public String secretKey;

}

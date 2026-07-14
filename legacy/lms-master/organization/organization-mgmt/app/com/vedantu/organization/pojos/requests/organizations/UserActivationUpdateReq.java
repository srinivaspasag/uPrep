package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class UserActivationUpdateReq extends AbstractOrgScopeReq {
    @Required
    public String         targetUserId;
    @Required
    public long         activateFrom;
    @Required
    public long         activateTill;
}

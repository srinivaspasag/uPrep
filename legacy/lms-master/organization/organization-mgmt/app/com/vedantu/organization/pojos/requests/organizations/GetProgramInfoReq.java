package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetProgramInfoReq extends AbstractOrgScopeReq {

    @Required
    public String programId;
}

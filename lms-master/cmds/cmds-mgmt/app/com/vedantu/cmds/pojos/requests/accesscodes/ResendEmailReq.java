package com.vedantu.cmds.pojos.requests.accesscodes;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ResendEmailReq extends AbstractOrgScopeReq{
	@Required
    public String accessCodeId;
}

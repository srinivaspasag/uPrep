package com.vedantu.cmds.pojos.requests.tests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetCMDSTestReq extends AbstractOrgScopeReq {

	@Required
	public String id;
}

package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrgProgramReq extends AddOrgProgramReq {
	
	@Required
	public String programId;

}

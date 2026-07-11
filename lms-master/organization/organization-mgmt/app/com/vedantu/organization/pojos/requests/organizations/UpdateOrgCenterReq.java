package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrgCenterReq extends AddOrgCenterReq {
	
	@Required
	public String centerId;

}

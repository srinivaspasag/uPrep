package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOrgDepartmentsReq extends AbstractAuthCheckReq {
	
	@Required
	public String orgId;

}

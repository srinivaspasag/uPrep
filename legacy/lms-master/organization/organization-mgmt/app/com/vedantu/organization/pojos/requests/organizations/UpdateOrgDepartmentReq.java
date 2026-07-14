package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrgDepartmentReq extends AddOrgDepartmentReq {
	
	@Required
	public String departmentId;

}

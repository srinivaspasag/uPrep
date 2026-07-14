package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class AddOrgProgramCoursesReq extends AbstractAuthCheckReq {

	@Required
	public String orgId;
	@Required
	public String programId;
	@Required
	public List<String> courseIds;

}

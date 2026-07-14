package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

public class UnsetEmailReq extends
		com.vedantu.user.pojos.requests.UnsetEmailReq {

	@Required
	public String orgId;
	@Required
	public String targetUserId;
	@Required
	public String targetOrgMemberId;

}

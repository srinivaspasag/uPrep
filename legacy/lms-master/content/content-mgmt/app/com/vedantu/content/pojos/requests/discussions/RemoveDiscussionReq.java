package com.vedantu.content.pojos.requests.discussions;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class RemoveDiscussionReq extends AbstractOrgScopeReq {

	@Required
	public String id;
}

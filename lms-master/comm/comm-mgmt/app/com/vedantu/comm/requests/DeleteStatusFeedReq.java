package com.vedantu.comm.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class DeleteStatusFeedReq extends AbstractAuthCheckReq {
	@Required
	public String id;
	@Required
	public String orgId;
}

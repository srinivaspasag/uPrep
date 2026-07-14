package com.vedantu.comm.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetStatusFeedReq extends AbstractAuthCheckReq {
	@Required
	public String feedId;
	@Required
	public String orgId;
}

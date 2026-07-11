package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class MarkMessageAsUnreadReq extends AbstractAuthCheckReq {

	@Required
	public String	userMessageId;
	public String	orgId;
}

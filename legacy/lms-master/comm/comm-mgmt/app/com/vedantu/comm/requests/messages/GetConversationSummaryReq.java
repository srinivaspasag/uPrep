package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetConversationSummaryReq extends AbstractAuthCheckReq {
	@Required
	public String	userConversationId;
	public String	orgId;
}

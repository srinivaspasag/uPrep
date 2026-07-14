package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetConversationReq extends AbstractAuthCheckReq {

	@Required
	public String	conversationId;
	public String	orgId;
}

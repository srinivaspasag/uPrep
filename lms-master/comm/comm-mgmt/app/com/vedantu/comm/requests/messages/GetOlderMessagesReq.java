package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOlderMessagesReq extends AbstractAuthCheckReq {

	@Required
	public String	userMessageId;
	@Required
	public String	conversationId;
	public int		size;
	public String	orgId;
}

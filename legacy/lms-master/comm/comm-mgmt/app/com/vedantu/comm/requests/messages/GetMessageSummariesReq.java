package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetMessageSummariesReq extends AbstractAuthCheckReq {

	@Required
	public String	conversationId;
	public int		size;

	public String	userMessageId;
	public String	orgId;

}

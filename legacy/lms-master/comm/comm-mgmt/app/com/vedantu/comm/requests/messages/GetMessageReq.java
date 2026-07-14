package com.vedantu.comm.requests.messages;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetMessageReq extends AbstractAuthCheckReq {

	@Required
	public String	messageId;
	public String	orgId;
}

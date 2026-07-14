package com.vedantu.comm.requests.messages;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetConversationUsersReq extends AbstractAuthCheckReq {

	@Required
	public String	conversationId;
	public int		start;
	public int		size;
	public String	orgId;
	public List<String> excludeUserIds;
}

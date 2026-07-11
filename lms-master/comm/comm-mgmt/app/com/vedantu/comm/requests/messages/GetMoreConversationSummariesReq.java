package com.vedantu.comm.requests.messages;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetMoreConversationSummariesReq extends AbstractAuthCheckReq {

	public int		size;
	public boolean	future;
	public long		timestamp	= -1L;
	public String	orgId;

}
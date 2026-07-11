package com.lms.pojos.requests.messages;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetConversationSummariesReq extends AbstractAuthCheckReq {

	public String orgId;
	public int start;
	public int size;
	public boolean future = false;
	public long timestamp = -1L;
	public String conversationId;

}

package com.vedantu.pojos.response.messages;

import com.vedantu.comm.models.mongo.UserMailBoxInfo;

public class GetUserMailBoxInfoRes {

	public String userId;
	public long conversationCount;
	public long unreadConversationCount;
	public long sentCount;

	public GetUserMailBoxInfoRes(UserMailBoxInfo userMailBoxInfo) {
		this.userId = userMailBoxInfo.userId;
		this.conversationCount = userMailBoxInfo.conversationCount;
		this.unreadConversationCount = userMailBoxInfo.unreadConversationCount;
		this.sentCount = userMailBoxInfo.sentCount;
	}
}

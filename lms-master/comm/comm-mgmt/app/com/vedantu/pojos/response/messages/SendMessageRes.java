package com.vedantu.pojos.response.messages;

import com.vedantu.comm.models.hbase.messages.MessageSummary;

public class SendMessageRes {
	public SendMessageRes(boolean isReceived, MessageSummary message) {
		super();
		this.isReceived = isReceived;
		this.message = message;
	}
	public boolean isReceived =false;
	public MessageSummary message;
}

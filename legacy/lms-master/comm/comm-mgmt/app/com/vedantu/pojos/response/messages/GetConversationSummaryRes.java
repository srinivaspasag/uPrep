package com.vedantu.pojos.response.messages;

import org.codehaus.jackson.annotate.JsonManagedReference;

import com.vedantu.comm.models.hbase.messages.ConversationSummary;

public class GetConversationSummaryRes {
	 @JsonManagedReference
	 public ConversationSummary summary;
	 public String orgId;
}

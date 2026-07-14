package com.vedantu.pojos.response.messages;

import org.codehaus.jackson.annotate.JsonManagedReference;

import com.vedantu.comm.models.hbase.messages.MessageSummary;

public class GetMessageSummaryRes {
	 @JsonManagedReference
	 public MessageSummary summary;
}

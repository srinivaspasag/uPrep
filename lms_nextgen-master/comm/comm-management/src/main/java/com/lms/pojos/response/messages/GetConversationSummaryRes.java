package com.lms.pojos.response.messages;

import com.lms.models.messages.ConversationSummary;
import org.codehaus.jackson.annotate.JsonManagedReference;


public class GetConversationSummaryRes {
	@JsonManagedReference
	public ConversationSummary summary;
	public String orgId;
}

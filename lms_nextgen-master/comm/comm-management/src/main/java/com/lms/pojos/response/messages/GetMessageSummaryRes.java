package com.lms.pojos.response.messages;

import com.lms.models.messages.MessageSummary;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetMessageSummaryRes {
	public MessageSummary summary;
}

package com.lms.pojos.requests.messages;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetConversationSummaryReq extends AbstractAuthCheckReq {
	@NotBlank(message = "userConversationId should not be empty")
	public String userConversationId;
	public String orgId;
}

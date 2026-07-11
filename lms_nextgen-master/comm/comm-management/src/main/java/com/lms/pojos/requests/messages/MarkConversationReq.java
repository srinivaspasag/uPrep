package com.lms.pojos.requests.messages;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.ConversationStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MarkConversationReq extends AbstractAuthCheckReq {

	@NotBlank(message = "userConversationId should not be empty")
	public String userConversationId;
	@NotNull(message = "status should not be empty")
	public ConversationStatus status;
	public String orgId;
}

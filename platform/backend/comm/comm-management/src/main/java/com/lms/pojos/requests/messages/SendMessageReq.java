package com.lms.pojos.requests.messages;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.models.messages.Message;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SendMessageReq extends AbstractAuthCheckReq {

	public String orgId;
	@NotNull(message = "message should not be empty")
	public Message message;

}

package com.lms.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetMessageSummariesReq extends AbstractAuthCheckReq {

    @NotBlank(message = "conversationId should not be null")
    public String conversationId;
    public int size;

    public String userMessageId;
    public String orgId;

}

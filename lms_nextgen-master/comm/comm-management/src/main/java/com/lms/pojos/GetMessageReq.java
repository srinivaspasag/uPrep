package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetMessageReq extends AbstractAuthCheckReq {

    @NotBlank(message = "messageId should not be null")
    public String messageId;
    public String orgId;
}

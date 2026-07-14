package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetChallengeHintReq extends AbstractAuthCheckReq {

    @NotBlank(message = "token should not be null")
    public String token;
}

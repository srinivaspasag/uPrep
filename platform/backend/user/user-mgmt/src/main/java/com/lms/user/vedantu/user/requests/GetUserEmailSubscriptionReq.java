package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class GetUserEmailSubscriptionReq extends AbstractAppCheckReq {

    @NotBlank(message="userid should not be null")
    private String userId;
    @NotBlank(message="targetUserId should not be null")
    private String targetUserId;

}

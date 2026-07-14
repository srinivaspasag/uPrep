package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Setter
@Getter
public class EmailSubscribeReq {
    @NotBlank(message = " userid should not be null")
    public String userId;
    @NotBlank(message = "targetUserId should not be null")
    public String targetUserId;
    @NotBlank(message ="mailCategory should not be null")
            public String mailCategory;
}

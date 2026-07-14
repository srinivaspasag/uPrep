package com.lms.user.vedantu.user.requests;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;


@Setter
@Getter
public class UserDirectLoginReq {
    @NotBlank(message = "targetUserId is required")
    public String targetUserId;

    @NotBlank(message = "orgId is required")
    public String orgId;
}

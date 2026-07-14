package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateUsernameReq extends UpdateUserPasswordReq {
    @NotBlank(message = "New user name is required")
    private String newUsername;
}

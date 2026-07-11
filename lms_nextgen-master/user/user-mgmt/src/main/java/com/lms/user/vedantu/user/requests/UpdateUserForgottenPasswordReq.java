package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateUserForgottenPasswordReq {
    @NotBlank(message = "User ID is required")
    public String userId;
    @NotBlank(message = "Code is required")
    public String code;
    @NotBlank(message = "New password is required")
    public String newPassword;
}

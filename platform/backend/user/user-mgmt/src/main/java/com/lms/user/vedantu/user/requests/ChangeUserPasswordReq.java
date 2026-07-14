package com.lms.user.vedantu.user.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class ChangeUserPasswordReq{

    @NotBlank(message = "Email is required")
    public String email;
    @NotBlank(message = "Target user ID is required")
    public String targetUserId;
    @NotBlank(message = "Old Password is required")
    public String oldPassword;
    @NotBlank(message = "New password is required")
    public String newPassword;

}
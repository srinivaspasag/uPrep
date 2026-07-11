package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ValidateEmailReq extends AbstractAppCheckReq{
    @NotBlank(message = "User ID is required")
    public String userId;
    @NotBlank(message = "Code is required")
    public String code;
    @NotNull(message = "Verified flag is required")
    public Boolean isVerified;
}

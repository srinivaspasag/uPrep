package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class BlacklistEmailReq {

    @NotBlank(message = "email should be mandatory")
    public String email;

    @NotBlank(message = "reason should not be null" )
    public String reason;
}

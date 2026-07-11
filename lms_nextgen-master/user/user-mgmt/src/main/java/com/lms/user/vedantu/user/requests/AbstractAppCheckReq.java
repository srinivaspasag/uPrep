package com.lms.user.vedantu.user.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class AbstractAppCheckReq {
    @NotBlank(message = "Calling App is required")
    public String callingApp;
    @NotBlank(message = "Calling App ID is required")
    public String callingAppId;
}

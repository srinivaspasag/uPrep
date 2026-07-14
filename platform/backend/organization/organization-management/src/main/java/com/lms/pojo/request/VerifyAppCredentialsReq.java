package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class VerifyAppCredentialsReq extends AbstractOrgScopeReq {

    @NotBlank(message = "appId should not be null")
    public String appId;

    @NotBlank(message = "appId should not be null")
    public String authToken;

    @NotBlank(message = "appId should not be null")
    public String secretKey;

}

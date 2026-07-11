package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class GenerateAppCredentialsReq extends AbstractOrgScopeReq {

    @NotBlank(message = "appId Should not be null")
    public String appId;
}

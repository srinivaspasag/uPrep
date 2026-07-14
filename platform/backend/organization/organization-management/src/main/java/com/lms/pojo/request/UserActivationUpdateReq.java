package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserActivationUpdateReq extends AbstractOrgScopeReq {
    @NotBlank(message = "targetUserId should not be null")
    public String targetUserId;
    @NotNull
    public long activateFrom;
    @NotNull
    public long activateTill;
}

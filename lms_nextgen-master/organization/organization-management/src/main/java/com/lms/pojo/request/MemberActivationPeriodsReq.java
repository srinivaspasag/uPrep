package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MemberActivationPeriodsReq {
    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "userId should not be null")
    public String userId;
    @NotNull
    public long from;
    @NotNull
    public long till;
}

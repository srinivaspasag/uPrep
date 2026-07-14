package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class MemberAuthReq {
    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "memberId should not be null")
    private String memberId;
    @NotBlank(message = "password should not be null")
    public String password;


}

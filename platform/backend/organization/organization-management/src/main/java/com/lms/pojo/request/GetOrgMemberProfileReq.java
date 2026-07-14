package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetOrgMemberProfileReq {
    @NotBlank(message = "orgId should not be null")
    public String  orgId;
    @NotBlank(message = "orgId should not be null")
    public String  targetUserId;

    public boolean ensureCourseInfo;

    public boolean getKey;
}

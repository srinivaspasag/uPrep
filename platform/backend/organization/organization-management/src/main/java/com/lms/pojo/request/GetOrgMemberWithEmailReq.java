package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class GetOrgMemberWithEmailReq extends AbstractAppCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String  orgId;

    @NotBlank(message = "email should not be null")
    public String  email;

    public boolean ensureCourseInfo;
    public boolean getKey;
    public boolean loginStatusRequested;

}

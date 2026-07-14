package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.DeviceType;
import com.lms.enums.OrgMemberProfile;
import com.lms.user.vedantu.user.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserStatusReq extends AbstractAuthCheckReq {
    @NotBlank(message = "orgId is missing")
    public String orgId;

    public DeviceType deviceType;

    public String query;
    public int start;
    public int size;
    public UserStatus status;

    public String programId;
    public String centerId;
    public String sectionId;
    public OrgMemberProfile profile;
}

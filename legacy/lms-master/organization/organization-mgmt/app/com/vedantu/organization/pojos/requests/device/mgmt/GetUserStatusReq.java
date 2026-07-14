package com.vedantu.organization.pojos.requests.device.mgmt;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.user.enums.UserStatus;

public class GetUserStatusReq extends AbstractAuthCheckReq {

    @Required
    public String           orgId;

    public DeviceType       deviceType;

    public String           query;
    public int              start;
    public int              size;
    public UserStatus       status;

    public String           programId;
    public String           centerId;
    public String           sectionId;
    public OrgMemberProfile profile;

}

package com.vedantu.organization.pojos.requests.device.mgmt;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.user.enums.UserStatus;

public class GetUserDeviceStatusReq extends AbstractAuthCheckReq {

    @Required
    public String     orgId;
    @Required
    public String     targetUserId;
    public int        start = 0;
    public int        size = 10;
    public UserStatus status;
    public DeviceType deviceType;

}

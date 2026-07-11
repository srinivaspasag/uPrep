package com.vedantu.organization.pojos.requests.device.mgmt;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class DeviceLoginReq extends AbstractAuthCheckReq {

    @Required
    public String     deviceId;
    @Required 
    public DeviceType deviceType;
    public long       expiryTimeOffset;
}

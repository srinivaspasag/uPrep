package com.vedantu.organization.pojos.requests.device.mgmt;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class RecordActivityReq extends AbstractAuthCheckReq {

    @Required
    public String     orgId;
    @Required
    public String     deviceId;
    @Required
    public DeviceType deviceType;
    @Required
    public String     page;

    public String     userAction;
    public SrcEntity  entity;
    public long       activityTime = 0;

}

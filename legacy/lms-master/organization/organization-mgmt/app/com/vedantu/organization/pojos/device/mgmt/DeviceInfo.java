package com.vedantu.organization.pojos.device.mgmt;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.user.enums.UserStatus;
import com.vedantu.user.models.LoginStatus;

public class DeviceInfo implements IListResponseObj {

    public DeviceInfo(LoginStatus status) {

        super();
        deviceId=status.deviceId;
        deviceType= status.deviceType;
        callingApp= status.callingApp;
        callingUserId= status.callingUserId;
        callingAppId= status.callingAppId;
        loginTime= status.loginTime;
        logoutTime=status.logoutTime;
        this.status = status.status;
        
        
    }

    public String     deviceId;
    public String     deviceType;
    public String     callingAppId;
    public String     callingUserId;
    public String     callingApp;
    public long       loginTime;
    public long       logoutTime;
    public UserStatus status;
    // acitivity specifics
    public long       lastActivityTime;
    public String     page;
    public String     userAction;
    public SrcEntity  entity;

}

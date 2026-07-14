package com.lms.pojo;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.user.vedantu.user.enums.UserStatus;
import com.lms.user.vedantu.user.model.LoginStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DeviceInfo implements IListResponseObj
{

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
    public SrcEntity entity;

}


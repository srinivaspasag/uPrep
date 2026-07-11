package com.vedantu.user.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.user.enums.UserStatus;

@Entity(value = "logins", noClassnameStored = true)
public class LoginStatus extends VedantuBaseMongoModel {

    public LoginStatus() {

    }

    public LoginStatus(String userId, String deviceId, DeviceType deviceType) {

        super();
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceType = deviceType.name();
        this.loginTime = 0;
        this.logoutTime = 0;
        this.status = UserStatus.LOGGED_OUT;
        this.expiryTime = -1;
    }

    public String     userId;
    public String     deviceId;
    public String     deviceType;
    public String     callingAppId;
    public String     callingApp;
    public String     callingUserId;

    public long       loginTime;
    public long       logoutTime;
    public UserStatus status;
    public long       expiryTime;  // relative -1 / value

    @Override
    public String toString() {

        return "LoginStatus [userId=" + userId + ", deviceId=" + deviceId + ", deviceType="
                + deviceType + ", callingAppId=" + callingAppId + ", callingApp=" + callingApp
                + ", loginTime=" + loginTime + ", logoutTime=" + logoutTime + ", status=" + status
                + ", expiryTime=" + expiryTime + "]";
    }
}

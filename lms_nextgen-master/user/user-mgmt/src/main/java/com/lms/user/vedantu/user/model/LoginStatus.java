package com.lms.user.vedantu.user.model;

import com.lms.common.vedantu.enums.DeviceType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.user.vedantu.user.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "logins")
@Setter
@Getter
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

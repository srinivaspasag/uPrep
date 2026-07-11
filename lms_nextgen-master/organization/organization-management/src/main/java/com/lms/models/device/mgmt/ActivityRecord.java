package com.lms.models.device.mgmt;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.DeviceType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "activityrecords")
public class ActivityRecord extends VedantuBaseMongoModel {
    public String userId;
    public String orgId;
    public String deviceId;
    public String deviceType;
    public String callingAppId;
    public String callingApp;
    public String callingUserId;
    public String page;
    public String action;
    public SrcEntity entity;

    public ActivityRecord() {

        super();
    }

    public ActivityRecord(String callingAppId, String callingApp, String callingUserId, String userId, String orgId,
                          String deviceId, DeviceType deviceType, String page, String action, SrcEntity entity) {

        super();
        this.userId = userId;
        this.orgId = orgId;
        this.deviceId = deviceId;
        this.deviceType = deviceType.name();
        this.page = page;
        this.action = action;
        this.entity = entity;
        this.callingAppId = callingAppId;
        this.callingApp = callingApp;
        this.callingUserId = callingUserId;

    }
}

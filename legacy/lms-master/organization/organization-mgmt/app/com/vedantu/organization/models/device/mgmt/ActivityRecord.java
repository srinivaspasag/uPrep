package com.vedantu.organization.models.device.mgmt;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "activityrecords", noClassnameStored = true)
public class ActivityRecord extends VedantuBaseMongoModel {

    public String    userId;
    public String    orgId;
    public String    deviceId;
    public String    deviceType;
    public String    callingAppId;
    public String    callingApp;
    public String    callingUserId;
    public String    page;
    public String    action;
    public SrcEntity entity;

    public ActivityRecord() {

        super();
    }

    public ActivityRecord(String callingAppId, String callingApp,String callingUserId, String userId, String orgId,
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
        this.callingUserId= callingUserId;

    }
}

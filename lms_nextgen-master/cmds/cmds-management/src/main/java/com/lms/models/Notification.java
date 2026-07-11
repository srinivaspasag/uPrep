package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojos.NotificationInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "notifications")
public class Notification extends VedantuBaseMongoModel {


    @Indexed(unique = true, dropDups = true)
    public String regId;
    public String deviceId;
    public String userId;
    public String orgId;
    public String programName;

    @Override
    public ModelBasicInfo toBasicInfo() {
        NotificationInfo notiInfo = new NotificationInfo(regId, userId);
        return notiInfo;
    }
}

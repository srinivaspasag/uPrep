package com.vedantu.cmds.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.cmds.pojos.NotificationInfo;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "notifications", noClassnameStored = true)
public class Notification extends VedantuBaseMongoModel {
	@Indexed(unique = true, dropDups=true)
	public String regId;
	public String deviceId;
	public String userId;
	public String orgId;
	public String programName;

	@Override
    public ModelBasicInfo toBasicInfo() {
    	NotificationInfo notiInfo = new NotificationInfo(regId,userId);
    	return notiInfo;
    }
	
}
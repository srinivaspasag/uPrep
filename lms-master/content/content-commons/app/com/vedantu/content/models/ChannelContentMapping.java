package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "channelcontentmapping", noClassnameStored = true)
public class ChannelContentMapping extends VedantuBaseMongoModel {

	public String userId;
	public SrcEntity entity;

	public ChannelContentMapping() {
		super();
	}

}

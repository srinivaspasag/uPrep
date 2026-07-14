package com.vedantu.comm.models.mongo;

import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;
@Entity(value = "remarks", noClassnameStored = true)
@Indexes(@Index(value = "provideeId"))
public class Remark extends VedantuBaseMongoModel {
	@Required
	public String providerId;
	
	@Required
	public String provideeId;
	@Required
	public String content;
	
	public String orgId;
	
	public Remark(){
		super();
	}
	
	public Remark(String providerId, String provideeId, String content, String orgId) {
		super();
		this.providerId = providerId;
		this.provideeId = provideeId;
		this.content = content;
		this.orgId = orgId;
	}

}

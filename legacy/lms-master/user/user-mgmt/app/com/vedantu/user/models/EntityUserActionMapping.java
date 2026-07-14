package com.vedantu.user.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "entityuseractionmapping", noClassnameStored = true)
@Indexes({ @Index(value = "userId,actionType,target.id"),
		@Index(value = "userId,actionType") })
public class EntityUserActionMapping extends VedantuBaseMongoModel {

	public String userId;

	public UserActionType actionType;

	public SrcEntity target;
	
	public SrcEntity context;

	public EntityUserActionMapping() {
		this(null, null, null, null);
	}

	public EntityUserActionMapping(String userId, UserActionType actionType,
			SrcEntity target, SrcEntity context) {
		super();
		this.userId = userId;
		this.actionType = actionType;
		this.target = target;
		this.context = context;
	}

	@Override
	public String toString() {
		return "EntityUserActionMapping [userId=" + userId + ", actionType="
				+ actionType + ", target=" + target + ", id=" + id
				+ ", timeCreated=" + timeCreated + ", lastUpdated="
				+ lastUpdated + ", recordState=" + recordState + "]";
	}

}

package com.vedantu.comm.models.mongo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.comm.enums.ShareType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "entitysharemapping", noClassnameStored = true)
@Indexes(@Index("userId, entity.id, entity.type"))
public class EntityShare extends VedantuBaseMongoModel {

	public String			userId;
	public SrcEntity		entity;
	public Set<SrcEntity>	with;
	public String			content;
	public ShareType		type;

	// public ActivityPermission permission;
	public EntityShare() {

	}

	public EntityShare(String userId, SrcEntity entity,
			Collection<? extends SrcEntity> with, String content, ShareType type) {
		super();
		this.userId = userId;
		this.entity = entity;
		this.with = new HashSet<SrcEntity>();
		for (SrcEntity wth : with) {
			if (wth != null) {
				this.with.add(wth);
			}
		}
		this.content = StringUtils.defaultString(content);
		this.type = type;
		// this.permission = ActivityPermission.VIEW;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityShare [userId:").append(userId)
				.append(", entity:").append(entity).append(", with:")
				.append(with).append(", content:").append(content)
				.append(", type:").append(type)
				// .append(", permission:").append(permission)
				.append(", ").append(super.toString()).append("]");
		return builder.toString();
	}

}

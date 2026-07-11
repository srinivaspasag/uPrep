package com.vedantu.content.pojos.tests;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.EntityMeasures;

public class EntityInfo extends SrcEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String name;
	public EntityMeasures measures;//
	public int lastRank;
	public long totalAttempts;

	public EntityInfo() {
		super();
	}

	public EntityInfo(EntityType type, String id, String name) {
		super(type, id);
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{name:");
		builder.append(name);
		builder.append(", type:");
		builder.append(type);
		builder.append(", id:");
		builder.append(id);
		builder.append("}");
		return builder.toString();
	}

}

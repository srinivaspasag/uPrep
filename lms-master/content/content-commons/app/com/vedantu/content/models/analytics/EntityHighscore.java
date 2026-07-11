package com.vedantu.content.models.analytics;

import java.util.HashSet;
import java.util.Set;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "entityhighscores", noClassnameStored = true)
@Indexes({ @Index("entity.id,entity.type,score"),
		@Index("entity.id,entity.type,userIds") })
public class EntityHighscore extends VedantuBaseMongoModel {

	// test, challenge, assignment, OR question
	public SrcEntity entity;
	// public int totalMarks; we do not need totalMarks, it can be fetched from
	// test collection

	public AcademicDimension acadDim;

	public double score;
	public Set<String> userIds;

	public EntityHighscore() {
		super();
		this.userIds = new HashSet<String>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{entity:");
		builder.append(entity);
		builder.append(", score:");
		builder.append(score);
		builder.append(", userIds:");
		builder.append(userIds);
		builder.append("}");
		return builder.toString();
	}

}

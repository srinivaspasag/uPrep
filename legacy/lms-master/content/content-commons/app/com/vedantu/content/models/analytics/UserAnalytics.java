package com.vedantu.content.models.analytics;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "useranalytics", noClassnameStored = true)
@Indexes(@Index(value = "userId, acadDim.type, acadDim.id"))
public class UserAnalytics extends VedantuBaseMongoModel {

	public String userId;

	// overall, course, topic, subtopic-wise
	public AcademicDimension acadDim;

	public EntityMeasures measures;

	public UserAnalytics() {
		super();
	}

	public UserAnalytics(String userId, AcademicDimension acadDim,
			EntityMeasures measures) {
		super();
		this.userId = userId;
		this.acadDim = acadDim;
		this.measures = measures;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{userId:");
		builder.append(userId);
		builder.append(", acadDim:");
		builder.append(acadDim);
		builder.append(", measures:");
		builder.append(measures);
		builder.append(", id:");
		builder.append(id);
		builder.append(", timeCreated:");
		builder.append(timeCreated);
		builder.append(", lastUpdated:");
		builder.append(lastUpdated);
		builder.append(", recordState:");
		builder.append(recordState);
		builder.append("}");
		return builder.toString();
	}

}

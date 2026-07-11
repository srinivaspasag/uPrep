package com.vedantu.user.models.points;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.enums.points.PointCategory;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userpointsdetails")
public class UserPointsDetail extends VedantuBaseMongoModel {
	@Indexed
	public String userId;
	public SrcEntity srcEntity;
	public PointCategory pointCategory;
	public int points;

	public UserPointsDetail(String userId, SrcEntity srcEntity,
			PointCategory pointCategory, int points) {
		super();
		this.userId = userId;
		this.srcEntity = srcEntity;
		this.pointCategory = pointCategory;
		this.points = points;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserPointsDetail [userId=").append(userId)
				.append(", srcEntity=").append(srcEntity)
				.append(", pointCategory=").append(pointCategory)
				.append(", points=").append(points).append(", toString()=")
				.append(super.toString()).append("]");
		return builder.toString();
	}

}

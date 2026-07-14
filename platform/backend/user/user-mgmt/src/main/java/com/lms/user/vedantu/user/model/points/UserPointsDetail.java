package com.lms.user.vedantu.user.model.points;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.user.vedantu.user.enums.points.PointCategory;

@Document(value = "userpointsdetails")
public class UserPointsDetail extends VedantuBaseMongoModel {
	@Indexed
	public String userId;
	public SrcEntity srcEntity;
	public PointCategory pointCategory;
	public int points;

	public UserPointsDetail(String userId, SrcEntity srcEntity, PointCategory pointCategory, int points) {
		super();
		this.userId = userId;
		this.srcEntity = srcEntity;
		this.pointCategory = pointCategory;
		this.points = points;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserPointsDetail [userId=").append(userId).append(", srcEntity=").append(srcEntity)
				.append(", pointCategory=").append(pointCategory).append(", points=").append(points)
				.append(", toString()=").append(super.toString()).append("]");
		return builder.toString();
	}

}

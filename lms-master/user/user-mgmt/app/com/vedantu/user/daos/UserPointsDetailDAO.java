package com.vedantu.user.daos;

import org.bson.types.ObjectId;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.enums.points.PointCategory;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.models.points.UserPointsDetail;

public class UserPointsDetailDAO extends
		VedantuBasicDAO<UserPointsDetail, ObjectId> {

	public static UserPointsDetailDAO INSTANCE = new UserPointsDetailDAO();

	private UserPointsDetailDAO() {
		super(UserPointsDetail.class);
	}

	public UserPointsDetail addUserPointsDetail(String userId,
			SrcEntity srcEntity, PointCategory pointCategory, int points) {
		UserPointsDetail pointsDetails = new UserPointsDetail(userId,
				srcEntity, pointCategory, points);
		save(pointsDetails);
		return pointsDetails;
	}

}
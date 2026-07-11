package com.vedantu.user.daos;

import org.bson.types.ObjectId;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.models.points.UserPoints;

public class UserPointsDAO extends VedantuBasicDAO<UserPoints, ObjectId> {

	public static UserPointsDAO INSTANCE = new UserPointsDAO();

	private UserPointsDAO() {
		super(UserPoints.class);
	}

	public UserPoints getUserPoints(String userId) {
		UserPoints userPoints = getQuery().filter(ConstantsGlobal.USER_ID,
				userId).get();
		if (userPoints == null) {
			userPoints = new UserPoints(userId);
			save(userPoints);
		}
		return userPoints;
	}

}

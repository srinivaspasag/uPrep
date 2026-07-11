package com.vedantu.content.daos.analytics;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.models.analytics.UserAnalytics;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserAnalyticsDAO extends VedantuBasicDAO<UserAnalytics, ObjectId> {

	private static final ALogger LOGGER = Logger.of(UserAnalyticsDAO.class);

	public static final UserAnalyticsDAO INSTANCE = new UserAnalyticsDAO();

	private UserAnalyticsDAO() {
		super(UserAnalytics.class);
	}

	public UserAnalytics getAnalytics(String userId,
			AcademicDimensionType acadDimType, String acadDimId) {
		LOGGER.debug("getAnalytics userId: " + userId + ", acadDimType: "
				+ acadDimType + ", acadDimId: " + acadDimId);

		UserAnalytics userAnalytics = getQuery().filter("userId", userId)
				.filter("acadDim.type", acadDimType)
				.filter("acadDim.id", acadDimId).get();

		LOGGER.info("getAnalytics userAnalytics: " + userAnalytics);
		return userAnalytics;
	}

	public boolean addAnalytics(String userId,
			AcademicDimensionType acadDimType, String acadDimId,
			EntityMeasures measures) {

		UpdateOperations<UserAnalytics> updateOps = getDS()
				.createUpdateOperations(UserAnalytics.class)
				.inc("measures.attempts", measures.attempts)
				.inc("measures.correct", measures.correct)
				.inc("measures.incorrect", measures.incorrect)
				.inc("measures.left", measures.left)
				.inc("measures.score", measures.score)
				.inc("measures.timeTaken", measures.timeTaken);

		Query<UserAnalytics> query = getQuery().filter("userId", userId)
				.filter("acadDim.type", acadDimType)
				.filter("acadDim.id", acadDimId);

		final boolean createIfNotPresent = true;
		UpdateResults<UserAnalytics> updateResult = getDS().update(query,
				updateOps, createIfNotPresent);

		log(LOGGER, updateResult);

		return !updateResult.getHadError();
	}

}

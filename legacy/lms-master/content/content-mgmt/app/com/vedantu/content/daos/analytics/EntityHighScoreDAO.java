package com.vedantu.content.daos.analytics;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.AcademicDimension;
import com.vedantu.content.models.analytics.EntityHighscore;
import com.vedantu.mongo.VedantuBasicDAO;

public class EntityHighScoreDAO extends
		VedantuBasicDAO<EntityHighscore, ObjectId> {

	private static final ALogger LOGGER = Logger.of(EntityHighScoreDAO.class);

	public static final EntityHighScoreDAO INSTANCE = new EntityHighScoreDAO();

	private EntityHighScoreDAO() {
		super(EntityHighscore.class);
	}

	public long getRank(SrcEntity entity, int score, String acadDimId) {
		Query<EntityHighscore> query = getQuery()
				.filter(ConstantsGlobal.ENTITY, entity)
				.filter(ConstantsGlobal.ACAD_DIM_DOT_ID, acadDimId)
				.field(ConstantsGlobal.USER_IDS).notEqual(null)
				.field(ConstantsGlobal.SCORE).greaterThan(score);
		return count(query) + 1;
	}

	public EntityHighscore getEntityHighScore(SrcEntity entity, int score,
			String userId, AcademicDimension acadDim) {

		Query<EntityHighscore> query = getQuery()
				.filter(ConstantsGlobal.ENTITY, entity)
				.filter(ConstantsGlobal.USER_IDS, userId)
				.filter("acadDim", acadDim);
		if (score > 0) {
			query.filter(ConstantsGlobal.SCORE, score);
		}
		EntityHighscore highscore = find(query).get();
		return highscore;
	}

	public boolean updateEntityHighScore(SrcEntity entity, double score,
			String userId, AcademicDimension acadDim) {
		UpdateOperations<EntityHighscore> updateOps = getDS()
				.createUpdateOperations(EntityHighscore.class).add(
						ConstantsGlobal.USER_IDS, userId);

		Query<EntityHighscore> query = getQuery()
				.filter(ConstantsGlobal.ENTITY, entity)
				.filter(ConstantsGlobal.SCORE, score)
				.filter("acadDim", acadDim);
		UpdateResults<EntityHighscore> updateResult = getDS().update(query,
				updateOps, true);
		log(LOGGER, updateResult);
		return !updateResult.getHadError();
	}
}

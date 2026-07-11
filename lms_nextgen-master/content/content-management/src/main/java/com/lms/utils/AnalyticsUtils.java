package com.lms.utils;

import com.lms.board.model.Board;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.enums.AcademicDimensionType;
import com.lms.enums.AnswerCorrectness;
import com.lms.models.EntityMeasures;
import com.lms.models.UserEntityAttempt;
import com.lms.models.analytics.*;
import com.lms.repository.EntityAnalyticsRepo;
import com.lms.repository.UserEntityAnalyticsRepo;
import com.lms.repository.UserQuestionAnalyticsRepo;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AnalyticsUtils {
	private static final Logger logger = LoggerFactory.getLogger(AnalyticsUtils.class);
	@Autowired
	private UserQuestionAnalyticsRepo userQuestionAnalyticsRepo;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private BoardRepo boardRepo;
	private static final String ACAD_DIM_TYPE = "acadDim.type";
	private static final String ACAD_DIM_ID = "acadDim.id";
	@Autowired
	private UserEntityAnalyticsRepo userEntityAnalyticsRepo;
	@Autowired
	private EntityAnalyticsRepo entityAnalyticsRepo;

	public UserQuestionAnalytics addUserQuestionAnalytics(
			UserQuestionAttempt userQuestionAttempt) {

		if (userQuestionAttempt == null) {
			return null;
		}
		// update UserQuestionAnalytics
		UserQuestionAnalytics userQuestionAnalytics =
				addAnalytics(userQuestionAttempt.userId, userQuestionAttempt.attemptId,
						userQuestionAttempt.parentEntity, userQuestionAttempt.qId,
						userQuestionAttempt.answerGiven, userQuestionAttempt.matrixAnswerGiven,
						userQuestionAttempt.isJudgeable, userQuestionAttempt.isCorrect,
						userQuestionAttempt.score, userQuestionAttempt.timeTaken);
		return userQuestionAnalytics;
	}

	public UserQuestionAnalytics addAnalytics(String userId, String attemptId, SrcEntity parentEntity, String qId,
											  List<String> answerGiven, Map<String, List<String>> matrixAnswerGiven, boolean isJudgeable,
											  AnswerCorrectness isCorrect, double score, long timeTaken) {

		UserQuestionAnalytics userQuestionAnalytics = getAnalytics(userId, parentEntity, qId);
		if (userQuestionAnalytics == null) {
			userQuestionAnalytics = new UserQuestionAnalytics(userId, attemptId, parentEntity, qId, answerGiven,
					isJudgeable, isCorrect, score, timeTaken);
			// userQuestionAnalytics.totalMarks = totalMarks;
			userQuestionAnalytics.matrixAnswerGiven = matrixAnswerGiven;

			userQuestionAnalyticsRepo.save(userQuestionAnalytics);
			logger.info("addAttempt saved userQuestionAnalytics: " + userQuestionAnalytics);
		}
		return userQuestionAnalytics;
	}

	public UserQuestionAnalytics getAnalytics(String userId, SrcEntity parentEntity, String qId) {

		logger.debug("getAnalytics userId: " + userId + ", parentEntity: " + parentEntity + ", qId: " + qId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("userId").is(userId);
		criteria.and("parentEntity.type").is(parentEntity.type);
		criteria.and("parentEntity.id").is(parentEntity.id);
		criteria.and("qId").is(qId);
		UserQuestionAnalytics userQuestionAnalytics = mongoTemplate.findOne(query, UserQuestionAnalytics.class);

		logger.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

		return userQuestionAnalytics;
	}

	public Set<AcademicDimension> getAcadDimensions(Set<String> brdIds) {

		Set<AcademicDimension> acadDims = new HashSet<AcademicDimension>();
		acadDims.add(new AcademicDimension(AcademicDimensionType.OVERALL,
				AcademicDimensionType.OVERALL.name()));

		Map<String, BoardBasicInfo> boardBasicInfos = getBasicInfosByIds(brdIds);
		if (MapUtils.isEmpty(boardBasicInfos)) {
			logger.debug("finalizeQuestionAttempt no boards found for brdIds: " + brdIds);
		} else {
			for (Map.Entry<String, BoardBasicInfo> entry : boardBasicInfos.entrySet()) {
				BoardBasicInfo boardBasicInfo = entry.getValue();
				if (null == boardBasicInfo) {
					continue;
				}
				acadDims.add(new AcademicDimension(AcademicDimensionType
						.getType(boardBasicInfo.type), boardBasicInfo.id));
			}
		}
		return acadDims;
	}

	public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {

		// logger.debug("getBasicInfosByIds ids: {" + StringUtils.join(ids, ", ") +
		// "}");
		List<Board> results = getBoardByIds(ids);
		Map<String, BoardBasicInfo> basicInfoMap = toBasicInfosMap(results);
		// logger.info("getBasicInfosByIds basicInfoMap: {" +
		// StringUtils.join(basicInfoMap, ", ")+ "}");
		return basicInfoMap;
	}

	private Map<String, BoardBasicInfo> toBasicInfosMap(List<Board> results) {
		Map<String, BoardBasicInfo> infosMap = new LinkedHashMap<String, BoardBasicInfo>();
		for (Board board : results) {
			BoardBasicInfo basicInfo = new BoardBasicInfo(board);
			infosMap.put(board._getStringId(), basicInfo);
		}
		return infosMap;
	}

	private List<Board> getBoardByIds(Set<String> ids) {
		// TODO Auto-generated method stub
		return boardRepo.findAllByIdIn(ids);
	}

	public boolean updateUserEntityAnalytics(String userId, SrcEntity entity,
											 AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures, double percentageScore,
											 String orgId) {

		UserEntityAnalytics userEntityAnalytics = addAnalytics(userId, entity,
				acadDimType, acadDimId, measures, percentageScore, orgId);
		logger.debug("finalize UserEntityAnalytics acadDim:{type: " + acadDimType + ", id:" + acadDimId + "}"
				+ ", userEntityAnalytics: " + userEntityAnalytics);

		boolean addedEntityAnalytics = addAnalytics(entity, acadDimType, acadDimId,
				measures);
		logger.debug("finalize EntityAnalytics acadDim:{type: " + acadDimType + ", id:" + acadDimId + "}"
				+ ", added: " + addedEntityAnalytics);

		return addedEntityAnalytics && userEntityAnalytics != null;
	}

	public UserEntityAnalytics addAnalytics(String userId, SrcEntity entity,
											AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures,
											double percentageScore, String orgId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
		criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(entity.type);
		criteria.and(ACAD_DIM_TYPE).is(acadDimType);
		criteria.and(ACAD_DIM_ID).is(acadDimId);
		UserEntityAnalytics userEntityAnalytics = mongoTemplate.findOne(query, UserEntityAnalytics.class);
		if (userEntityAnalytics == null) {
			userEntityAnalytics = new UserEntityAnalytics(userId, new AcademicDimension(
					acadDimType, acadDimId), measures, null, entity, percentageScore, orgId);
		} else {
			userEntityAnalytics.measures.attempts += measures.attempts;
			userEntityAnalytics.measures.correct += measures.correct;
			userEntityAnalytics.measures.partial += measures.partial;
			userEntityAnalytics.measures.incorrect += measures.incorrect;
			userEntityAnalytics.measures.left += measures.left;
			userEntityAnalytics.measures.score += measures.score;
			userEntityAnalytics.measures.timeTaken += measures.timeTaken;

			// get total test attempted by the user
			Query query2 = new Query();
			Criteria criteria2 = new Criteria();
			criteria2.and(ConstantsGlobal.USER_ID).is(userId);
			criteria2.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
			criteria2.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(entity.type);
			query2.addCriteria(criteria2);
			long attemptCount = mongoTemplate.count(query2, UserEntityAttempt.class);
			if (attemptCount == 0) {
				attemptCount = 1;// 1st attempt
			}
			userEntityAnalytics.percentage = ((userEntityAnalytics.percentage * (attemptCount - 1)) + percentageScore)
					/ attemptCount;
			userEntityAnalytics.orgId = orgId;
		}
		userEntityAnalytics.lastUpdated = System.currentTimeMillis();
		userEntityAnalyticsRepo.save(userEntityAnalytics);
		// update analytics to corresponding overall data
		// entity{TYPE:TEST,id:OVERALL}
		if (entity.id != AcademicDimensionType.OVERALL.name()) {
			addAnalytics(userId, new SrcEntity(entity.type, AcademicDimensionType.OVERALL.name()),
					acadDimType, acadDimId, measures, percentageScore, orgId);
		}


		return userEntityAnalytics;
	}

	public boolean addAnalytics(SrcEntity entity, AcademicDimensionType acadDimType,
								String acadDimId, EntityMeasures measures) {
		logger.debug("addAnalytics for entity: " + entity);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("entity").is(entity);
		criteria.and(ACAD_DIM_TYPE).is(acadDimType);
		criteria.and(ACAD_DIM_ID).is(acadDimId);
		EntityAnalytics entityAnalytics = mongoTemplate.findOne(query, EntityAnalytics.class);
		if (entityAnalytics == null) {
			entityAnalytics = new EntityAnalytics();
			entityAnalytics.setMeasures(measures);

		} else {
			entityAnalytics.getMeasures().setAttempts(entityAnalytics.getMeasures().getAttempts() + measures.attempts);
			entityAnalytics.getMeasures().setCorrect(entityAnalytics.getMeasures().getCorrect() + measures.correct);
			entityAnalytics.getMeasures().setTimeTaken(entityAnalytics.getMeasures().getTimeTaken() + measures.timeTaken);
			entityAnalytics.getMeasures().setIncorrect(entityAnalytics.getMeasures().getIncorrect());

		}
		EntityAnalytics updateResult = entityAnalyticsRepo.save(entityAnalytics);
		if (measures.score > updateResult.measures.maxScore) {
			updateResult.measures.maxScore = measures.score;
			logger.debug("updating maxScore : " + measures.score);
			entityAnalyticsRepo.save(updateResult);
		}
		logger.debug("update enalytics result");

		return true;
	}

}

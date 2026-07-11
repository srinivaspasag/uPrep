package com.vedantu.content.daos.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.analytics.UserQuestionAttempt;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;

public class UserQuestionAttemptDAO extends VedantuBasicDAO<UserQuestionAttempt, ObjectId> {

    private static final ALogger               LOGGER   = Logger.of(UserQuestionAttemptDAO.class);

    public static final UserQuestionAttemptDAO INSTANCE = new UserQuestionAttemptDAO();

    private UserQuestionAttemptDAO() {

        super(UserQuestionAttempt.class);
    }

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, String qId) {

        return getAttempts(userId, parentEntity, qId, null);
    }
    
    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, List<String> qIds) {

    	LOGGER.debug("getAttempts userId: " + userId + ", parentEntity: " + parentEntity
                + ", qIds: " + qIds);
        if (qIds == null) {
            qIds = new ArrayList<String>();
        }
        Query<UserQuestionAttempt> query = getQuery().filter("userId", userId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id).field("qId").hasAllOf(qIds)
                .order(ConstantsGlobal.TIME_CREATED);
        List<UserQuestionAttempt> userQuestionAttempts = query.asList();

        LOGGER.info("getAttempts userQuestionAttempts.size: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
    }

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, String qId,
            Boolean isJudgeable) {

        return getAttempts(userId, parentEntity, Arrays.asList(qId), isJudgeable);
    }

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity,
            List<String> qIds, Boolean isJudgeable) {

        LOGGER.debug("getAttempts userId: " + userId + ", parentEntity: " + parentEntity
                + ", qIds: " + qIds);
        if (qIds == null) {
            qIds = new ArrayList<String>();
        }
        Query<UserQuestionAttempt> query = getQuery().filter("userId", userId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id).field("qId").hasAnyOf(qIds)
                .order(ConstantsGlobal.TIME_CREATED);
        if (isJudgeable != null) {
            query = query.filter("isJudgeable", isJudgeable.booleanValue());
        }
        List<UserQuestionAttempt> userQuestionAttempts = query.asList();

        LOGGER.info("getAttempts userQuestionAttempts.size: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
    }

    public UserQuestionAttempt getFinilazedQuestionAttempt(String attemptId, String qId, boolean isJudgeable) throws VedantuException {
        LOGGER.debug("getlastAttemptTime attemptId: " + attemptId);
        if (StringUtils.isEmpty(attemptId)) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        UserQuestionAttempt res = new UserQuestionAttempt();
        DBObject query = new BasicDBObject("attemptId", attemptId);
        query.put("qId", qId);
        query.put("isJudgeable", isJudgeable);
        VedantuDBResult<UserQuestionAttempt> results = getInfos(
                query,
                null,
                MongoManager.NO_START,
                1,
                MongoManager.getSortQuery(ConstantsGlobal.TIME_CREATED,
                        SortOrder.DESC.name()));
        for(UserQuestionAttempt lastQuestionAttempt : results.results){
            LOGGER.debug("Id of the latest question attempt of qId "+qId+" and attemptId "+attemptId+" is "+lastQuestionAttempt.id);
            res =  lastQuestionAttempt;
            break;
        }
        return res;
    }

    public List<UserQuestionAttempt> getAttempts(String userId, String attemptId, String qId,
            Boolean isJudgeable) {

        Query<UserQuestionAttempt> query = getQuery().filter("userId", userId)
                .filter(ConstantsGlobal.QID, qId).filter(ConstantsGlobal.ATTEMPT_ID, attemptId)
                .order(ConstantsGlobal.TIME_CREATED);

        if (isJudgeable != null) {
            query = query.filter("isJudgeable", isJudgeable.booleanValue());
        }
        List<UserQuestionAttempt> userQuestionAttempts = query.asList();

        LOGGER.info("getAttempts userQuestionAttempts.sizes: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
    }

    public UserQuestionAttempt addAttempt(String userId, String attemptId, SrcEntity parentEntity,
                                          String qId, List<String> answerGiven, Map<String, List<String>> matrixAnswerGiven,
                                          QuestionType type, boolean isJudgeable, AnswerCorrectness isCorrect, int score, long timeTaken) {

        LOGGER.debug("addAttempt userId: " + userId + ", attemptId: " + attemptId
                + ", parentEntity: " + parentEntity + ", qId: " + qId + ", answerGiven: {"
                + StringUtils.join(answerGiven, ", ") + "}, isJudgeable:" + isJudgeable
                + ", isCorrect: " + isCorrect + ", score: " + score + ", timeTaken: " + timeTaken);

        UserQuestionAttempt userQuestionAttempt = new UserQuestionAttempt(userId, attemptId,
                parentEntity, qId, answerGiven, isJudgeable, isCorrect, score, timeTaken);
        userQuestionAttempt.matrixAnswerGiven = matrixAnswerGiven;
        userQuestionAttempt.type = type;

        save(userQuestionAttempt);
        LOGGER.info("addAttempt saved userQuestionAttempt: " + userQuestionAttempt);

        return userQuestionAttempt;
    }

    public void removeUserQuestionAttempt(String userId, SrcEntity target) throws VedantuException {

        List<UserQuestionAttempt> userQuestionAttempt = getQuery().filter(ConstantsGlobal.USER_ID, userId).filter("parentEntity", target)
                .asList();
        LOGGER.debug("deleted UserQuestionAttempt ");
        if (userQuestionAttempt.isEmpty()) {
//            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }else {
            for(UserQuestionAttempt userQuestionAttempts : userQuestionAttempt){
                delete(userQuestionAttempts);
            }
        }
    }
    public List<UserQuestionAttempt> getAllAttempts(String userId, SrcEntity target) {

        Query<UserQuestionAttempt> query = getQuery().filter("userId", userId)
                .filter("parentEntity.id", target.id)
                .filter("parentEntity.type", target.type);
        List<UserQuestionAttempt> userQuestionAttempts = query.asList();

        LOGGER.info("getAttempts userQuestionAttempts.sizes: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
    }

    public long getTimeTaken(String attemptId) {
        // TODO Auto-generated method stub
        long timeTaken = 0;
        List<UserQuestionAttempt> userQuestionAttemptList = getQuery().filter(ConstantsGlobal.ATTEMPT_ID, attemptId).asList();
        for(UserQuestionAttempt userQuestionAttempt : userQuestionAttemptList){
            timeTaken += userQuestionAttempt.timeTaken;
        }
        return timeTaken;
    }

	public List<UserQuestionAttempt> getStudentAttempts(SrcEntity target,
			String qId, String isCorrect, int start, int size,
			MutableLong totalHits) {
		Query<UserQuestionAttempt> query = getQuery().filter("qId", qId)
				.filter("parentEntity.id", target.id)
				.filter("parentEntity.type", target.type)
				.filter("isJudgeable", true)
				.order(ConstantsGlobal.TIME_CREATED);
		if (StringUtils.isNotEmpty(isCorrect)) {
			query.filter("isCorrect", "UNKNOWN");
		}
		if (size > 0) {
			query.offset(start).limit(size);
		}
		if (totalHits != null) {
			totalHits.setValue(query.countAll());
		}
		return query.asList();
	}

    public List<UserQuestionAttempt> getRemainingSubjectiveAttempts(String attemptId) {
        // TODO Auto-generated method stub
        Query<UserQuestionAttempt> query = getQuery().filter("attemptId", attemptId)
                .filter("type", "SUBJECTIVE")
                .filter("isJudgeable", true)
                .filter("isCorrect", "UNKNOWN");
        List<UserQuestionAttempt> userQuestionAttempts = query.asList();
        return userQuestionAttempts;
    }
}

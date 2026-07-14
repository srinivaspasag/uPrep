package com.vedantu.content.daos.analytics;

import java.util.List;
import java.util.Map;

import com.vedantu.content.enums.AnswerCorrectness;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserQuestionAnalyticsDAO extends VedantuBasicDAO<UserQuestionAnalytics, ObjectId> {

    private static final ALogger                 LOGGER   = Logger.of(UserQuestionAnalyticsDAO.class);

    public static final UserQuestionAnalyticsDAO INSTANCE = new UserQuestionAnalyticsDAO();

    public UserQuestionAnalyticsDAO() {

        super(UserQuestionAnalytics.class);
    }

    public UserQuestionAnalytics getAnalytics(String userId, SrcEntity parentEntity, String qId) {

        LOGGER.debug("getAnalytics userId: " + userId + ", parentEntity: " + parentEntity
                + ", qId: " + qId);

        UserQuestionAnalytics userQuestionAnalytics = getQuery().filter("userId", userId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id).filter("qId", qId).get();

        LOGGER.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

        return userQuestionAnalytics;
    }

    public UserQuestionAnalytics addAnalytics(String userId, String attemptId,
            SrcEntity parentEntity, String qId, List<String> answerGiven,
            Map<String, List<String>> matrixAnswerGiven, boolean isJudgeable, AnswerCorrectness isCorrect,
            double score, long timeTaken) {

        LOGGER.debug("addAnalytics userId: " + userId + ", attemptId: " + attemptId
                + ", parentEntity: " + parentEntity + ", qId: " + qId + ", answerGiven: {"
                + StringUtils.join(answerGiven, ", ") + "},matrixAnswerGiven: {"
                + matrixAnswerGiven + "}, isJudgeable:" + isJudgeable + ", isCorrect: " + isCorrect
                + ", timeTaken: " + timeTaken);

        UserQuestionAnalytics userQuestionAnalytics = getAnalytics(userId, parentEntity, qId);
        if (userQuestionAnalytics == null) {
            userQuestionAnalytics = new UserQuestionAnalytics(userId, attemptId, parentEntity, qId,
                    answerGiven, isJudgeable, isCorrect, score, timeTaken);
            // userQuestionAnalytics.totalMarks = totalMarks;
            userQuestionAnalytics.matrixAnswerGiven = matrixAnswerGiven;

            save(userQuestionAnalytics);
            LOGGER.info("addAttempt saved userQuestionAnalytics: " + userQuestionAnalytics);
        }
        return userQuestionAnalytics;
    }

    public List<UserQuestionAnalytics> getAllUsersAnalytics(String qId, AnswerCorrectness isCorrect, SrcEntity parentEntity) {

        LOGGER.debug("getAnalytics qId: " + qId);

        List<UserQuestionAnalytics> userQuestionAnalytics = getQuery().filter("isCorrect", isCorrect).filter("qId", qId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id).asList();

        LOGGER.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

        return userQuestionAnalytics;
    }

    public void removeUserQuestionAnalytics(String userId, SrcEntity target) throws VedantuException {

        List<UserQuestionAnalytics> userQuestionAnalytics = getQuery().filter(ConstantsGlobal.USER_ID, userId).filter("parentEntity",
                        target).asList();
        LOGGER.debug("deleted UserQuestionAnalytics ");
        if (userQuestionAnalytics.isEmpty()) {
//            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }else {
            for(UserQuestionAnalytics userQuestionAnalytic : userQuestionAnalytics){
                delete(userQuestionAnalytic);
            }
        }
    }
}

package com.vedantu.content.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.models.Answer;

import org.apache.commons.collections.MapUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.analytics.EntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.EntityHighScoreDAO;
import com.vedantu.content.daos.analytics.UserEntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserQuestionAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserQuestionAttemptDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.analytics.AcademicDimension;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityHighscore;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.content.models.analytics.UserQuestionAttempt;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.QuestionResultStatus;

public class AnalyticsUtils {

    private static final ALogger LOGGER = Logger.of(AnalyticsUtils.class);

    public static boolean updateUserEntityAnalytics(String userId, SrcEntity entity,
                                                    AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures,
                                                    double percentageScore, String orgId) {

        UserEntityAnalytics userEntityAnalytics = UserEntityAnalyticsDAO.INSTANCE.addAnalytics(
                userId, entity, acadDimType, acadDimId, measures, percentageScore, orgId);
        LOGGER.debug("finalize UserEntityAnalytics acadDim:{type: " + acadDimType + ", id:"
                + acadDimId + "}" + ", userEntityAnalytics: " + userEntityAnalytics);

        boolean addedEntityAnalytics = EntityAnalyticsDAO.INSTANCE.addAnalytics(entity,
                acadDimType, acadDimId, measures);
        LOGGER.debug("finalize EntityAnalytics acadDim:{type: " + acadDimType + ", id:" + acadDimId
                + "}" + ", added: " + addedEntityAnalytics);

        return addedEntityAnalytics && userEntityAnalytics != null;
    }

    public static void updateHighScores(String userId, SrcEntity entity, SrcEntity parent,
                                        double score, AcademicDimension acadDim) {

        EntityHighScoreDAO.INSTANCE.updateEntityHighScore(entity, score, userId, acadDim);
        if (parent != null) {
            EntityHighscore highscore = EntityHighScoreDAO.INSTANCE.getEntityHighScore(parent, -1,
                    userId, acadDim);
            if (highscore == null) {
                EntityHighScoreDAO.INSTANCE.updateEntityHighScore(parent, score, userId, acadDim);
            } else {
                double oldScore = highscore.score;
                double newScore = oldScore + score;
                highscore.userIds.remove(userId);
                EntityHighScoreDAO.INSTANCE.save(highscore);
                EntityHighScoreDAO.INSTANCE
                        .updateEntityHighScore(parent, newScore, userId, acadDim);
            }
        }
    }

    public static Set<AcademicDimension> getAcadDimensions(Set<String> brdIds) {

        Set<AcademicDimension> acadDims = new HashSet<AcademicDimension>();
        acadDims.add(new AcademicDimension(AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name()));

        Map<String, BoardBasicInfo> boardBasicInfos = BoardDAO.INSTANCE.getBasicInfosByIds(brdIds);
        if (MapUtils.isEmpty(boardBasicInfos)) {
            LOGGER.debug("finalizeQuestionAttempt no boards found for brdIds: " + brdIds);
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

    public static UserQuestionAnalytics addUserQuestionAnalytics(
            UserQuestionAttempt userQuestionAttempt) {

        if (userQuestionAttempt == null) {
            return null;
        }
        // update UserQuestionAnalytics
        UserQuestionAnalytics userQuestionAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                .addAnalytics(userQuestionAttempt.userId, userQuestionAttempt.attemptId,
                        userQuestionAttempt.parentEntity, userQuestionAttempt.qId,
                        userQuestionAttempt.answerGiven, userQuestionAttempt.matrixAnswerGiven,
                        userQuestionAttempt.isJudgeable, userQuestionAttempt.isCorrect,
                        userQuestionAttempt.score, userQuestionAttempt.timeTaken);
        return userQuestionAnalytics;
    }

    // this function allot marks to the users
    public static Map<String, UserQuestionAttempt> toFinalAttempts(
            Map<String, Marks> qIdsToMarksMap, List<UserQuestionAttempt> attempts) {

        Map<String, UserQuestionAttempt> qIdToFinalAttempts = new HashMap<String, UserQuestionAttempt>();
        for (UserQuestionAttempt attempt : attempts) {
            UserQuestionAttempt finalAttempt = qIdToFinalAttempts.get(attempt.qId);
            if (finalAttempt == null || finalAttempt.timeCreated < attempt.timeCreated) {
                qIdToFinalAttempts.put(attempt.qId, attempt);
            }
        }

        for (Entry<String, UserQuestionAttempt> entry : qIdToFinalAttempts.entrySet()) {
            UserQuestionAttempt userQuestionAttempt = entry.getValue();
            Marks marks = qIdsToMarksMap.get(entry.getKey());
            if (marks == null) {
                continue;
            }
            double score;
            LOGGER.info("Answer correct? " + userQuestionAttempt.isCorrect.name());
            switch (userQuestionAttempt.isCorrect) {
                case CORRECT:
                    score = marks.positive;
                    break;
                case INCORRECT:
                    score = -marks.negative;
                    break;
                case PARTIAL:
                    Question ques = QuestionDAO.INSTANCE.getById(userQuestionAttempt.qId);
                    Answer answer = AnswerDAO.INSTANCE.getQuestionAnswer(userQuestionAttempt.qId);
                    if(answer == null) {
                        LOGGER.error("Answer is null");
                        score = 0;
                        break;
                    }
                    Set<String> correctAnswers = new HashSet<String>(answer.answer);
                    if(ques.type == QuestionType.SUBJECTIVE){
                        score = userQuestionAttempt.score;
                    }else{
                        double partial = (double)(marks.positive)/ques.options.size();
                        score = calculatePartialScore(userQuestionAttempt.answerGiven,correctAnswers, userQuestionAttempt.qId, partial);
                    }
                    break;
                default:
                    score = 0;
                    break;
            }

            if (marks.status == QuestionResultStatus.BONUS) {
                score = marks.positive;
            } else if (marks.status == QuestionResultStatus.CANCELLED) {
                score = 0;
            }
            userQuestionAttempt.score = score;
            userQuestionAttempt.isFinalized = true;
            // save this UserQuestionAttempt
            LOGGER.debug("HEMAN score is " + score);
            LOGGER.info("saving user final attempts: " + userQuestionAttempt);
            UserQuestionAttemptDAO.INSTANCE.save(userQuestionAttempt);
        }
        return qIdToFinalAttempts;
    }

    private static double calculatePartialScore(List<String> answerGiven, Set<String> correctAnswers, String qId, double partialScore) {
        double score = 0;
        for(String ans : answerGiven) {
            if(correctAnswers.contains(ans)) {
                score += partialScore;
            }
        }
        return score;
    }

    public static Map<String, AcademicDimension> getBoardswiseAcademicDimensions(
            Map<String, Question> qIdToQuestionMap) {

        Set<String> brdIds = new HashSet<String>();
        for (Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {
            if (entry.getValue().boardIds != null) {
                brdIds.addAll(entry.getValue().boardIds);
            }
        }
        Map<String, AcademicDimension> acadDimnMap = new HashMap<String, AcademicDimension>();
        Set<AcademicDimension> acadDims = getAcadDimensions(brdIds);
        for (AcademicDimension acadDim : acadDims) {
            acadDimnMap.put(acadDim.id, acadDim);
        }
        return acadDimnMap;
    }

    public static Set<AcademicDimension> getAcadDimensionsSubset(Set<String> brdIds,
                                                                 Map<String, AcademicDimension> acadDimMap) {

        Set<AcademicDimension> acadDims = new HashSet<AcademicDimension>();
        acadDims.add(acadDimMap.get(AcademicDimensionType.OVERALL.name()));
        for (String brdId : brdIds) {
            AcademicDimension acadDim = acadDimMap.get(brdId);
            if (acadDim != null) {
                acadDims.add(acadDim);
            }
        }
        return acadDims;
    }

}

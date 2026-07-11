package com.vedantu.eventbus.processors.challenges;

import java.util.Arrays;
import java.util.List;

import com.vedantu.content.enums.AnswerCorrectness;
import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.enums.EnumBasket;
import com.vedantu.content.enums.EnumBasket.Judgement;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.pojos.requests.analytics.EndAttemptReq;
import com.vedantu.content.pojos.requests.analytics.RecordAttemptReq;
import com.vedantu.content.pojos.requests.analytics.StartAttemptReq;
import com.vedantu.content.pojos.responses.analytics.RecordAttemptRes;
import com.vedantu.content.pojos.responses.analytics.StartAttemptRes;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class ChallengeUserAttemptStatusProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(ChallengeUserAttemptStatusProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
                .fetchEventDetails();
        LOGGER.info("getting user ChallengeTaken for challenge[" + details.id + "]");
        List<SrcEntity> entities = details.entities;
        if (entities == null) {
            LOGGER.error("no entities found for challenge[" + details.id + "] ");
            return Status.NOT_CONSUMABLE;
        }
        boolean process = true;
        DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, details.id);
        query.put(ConstantsGlobal.PROCESSED, false);
        // ChallengeAnalytics challengeAnalytics = ChallengeUtilCommon
        // .getChallengeAnalytics(details.challengeId);
        int start = 0;
        while (process) {

            List<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(query, null,
                    start, ChallengeDAO.DEFAULT_BATCH_SIZE, null).results;
            start += challengeTakens.size();
            if (challengeTakens.size() == 0) {
                process = false;
                break;
            }

            for (ChallengeTaken challengeTaken : challengeTakens) {
                LOGGER.info("marking " + challengeTaken + " processed");
                challengeTaken.processed = true;

                LOGGER.info("checking  challengeTaken[" + challengeTaken._getStringId()
                        + "] status {attempted correctly or not}");
                int correctCount = 0;
                // challengeAnalytics.updateHintCount(challengeTaken.hint);
                for (SrcEntity entity : entities) {
                    VedantuBaseMongoModel model = EntityTypeDAOFactory.INSTANCE.get(entity.type)
                            .getById(entity.id);

                    if (model != null && model instanceof Question) {
                        Question question = (Question) model;
                        Answer answer = AnswerDAO.INSTANCE.getQuestionAnswer(entity.id);
                        if (answer == null) {
                            continue;
                        }

                        StartAttemptReq startAttemptReq = new StartAttemptReq(
                                challengeTaken.userId, challengeTaken.userId,
                                challengeTaken.challengeId, EntityType.CHALLENGE, null);
                        try {
                            startAttemptReq.qIds = Arrays.asList(entity.id);
                            StartAttemptRes startAttemptRes = AnalyticsManager.startAttempt(
                                    startAttemptReq, false);
                            RecordAttemptReq recordAttemptReq = new RecordAttemptReq(
                                    challengeTaken.userId, challengeTaken.userId,
                                    challengeTaken.challengeId, EntityType.CHALLENGE, null,
                                    startAttemptRes.info.id, question._getStringId(),
                                    challengeTaken.answer, challengeTaken.timeTaken, null);
                            RecordAttemptRes recordAttemptRes = AnalyticsManager
                                    .recordAttempt(recordAttemptReq);
                            boolean isCorrect = CollectionUtils.isNotEmpty(recordAttemptReq
                                    .getAnswerGiven())
                                    && question.type.isCorrect(Judgement.JUDGE,
                                            recordAttemptReq.getAnswerGiven(), answer.answer,
                                            EnumBasket.Status.COMPLETE, false,true) == AnswerCorrectness.CORRECT;
                            LOGGER.info("================================== user["
                                    + challengeTaken.userId + "] has given answer["
                                    + challengeTaken.answer + "] for challenge["
                                    + challengeTaken.challengeId + "] and entity[" + entity
                                    + "], isCorrect: " + isCorrect + ", recordAttemptRes : "
                                    + recordAttemptRes
                                    + "=========================================");
                            if (isCorrect) {
                                correctCount++;
                            }

                            EndAttemptReq endAttemptReq = new EndAttemptReq(challengeTaken.userId,
                                    challengeTaken.userId, challengeTaken.challengeId,
                                    EntityType.CHALLENGE, null, startAttemptRes.info.id, null);
                            AnalyticsManager.endAttempt(endAttemptReq, 0);
                        } catch (VedantuException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        if (correctCount >= 1) {
                            challengeTaken.success = true;
                        }

                        ChallengeTakenDAO.INSTANCE.save(challengeTaken);

                        LOGGER.info("user[" + challengeTaken.userId + "] overall challenge["
                                + challengeTaken.challengeId + "] status : "
                                + challengeTaken.success);
                    } else {
                        process = false;
                    }
                }
            }
        }
        // LOGGER.info("saving challenge analytics : " + challengeAnalytics);
        // TODO: challengeAnalytics.saveNow();
        return Status.SUCCESS;
    }
}

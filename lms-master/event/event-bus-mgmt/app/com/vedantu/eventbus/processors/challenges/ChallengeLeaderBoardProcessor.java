package com.vedantu.eventbus.processors.challenges;

import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeLeaderBoardDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.models.challenges.ChallengeLeaderBoard;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager;

public class ChallengeLeaderBoardProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(ChallengeLeaderBoardProcessor.class);
    private static int           MIN_SUCCESSFULL_ATTEMPT;

    public ChallengeLeaderBoardProcessor() {

        super();
        MIN_SUCCESSFULL_ATTEMPT = Play.application().configuration()
                .getInt("challenge.leaderboard.min.attempt", 1);
    }

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
                .fetchEventDetails();
        DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, details.id);
        query.put(ConstantsGlobal.SUCCESS, true);
        boolean process = true;
        int start = 0;
        while (process) {

            List<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(query,
                    MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.CHALLENGE_ID,
                            ConstantsGlobal.USER_ID, ConstantsGlobal.HINT,
                            ConstantsGlobal.TIME_TAKEN, ConstantsGlobal.PARENT),
                            MongoManager.INCLUDE_FIELD), start, ChallengeDAO.DEFAULT_BATCH_SIZE,
                    null).results;
            start += challengeTakens.size();
            if (challengeTakens.size() > 0) {
                if (start < MIN_SUCCESSFULL_ATTEMPT) {
                    LOGGER.info("challeneg[" + details.id + "] has only[" + start
                            + "] successful attempts hence not creating leaderBoard ");
                    return Status.SUCCESS;
                }

                for (ChallengeTaken challengeTaken : challengeTakens) {
                    ChallengeLeaderBoard leaderBoard = getChallengeLeaderBoard(
                            challengeTaken.userId, challengeTaken.challengeId,
                            challengeTaken.parent);
                    if (leaderBoard == null) {
                        leaderBoard = ChallengeLeaderBoardDAO.INSTANCE.addLearderBoard(
                                challengeTaken.userId, challengeTaken.challengeId,
                                challengeTaken.timeTaken, challengeTaken.hint,
                                challengeTaken.parent);
                    }
                    LOGGER.info("saved challenge[" + challengeTaken.challengeId
                            + "] leader board info for user[" + challengeTaken.userId + "]");
                }
            } else {
                process = false;
            }
        }
        return Status.SUCCESS;
    }

    private ChallengeLeaderBoard getChallengeLeaderBoard(String userId, String challengeId,
            SrcEntity parent) {

        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.CHALLENGE_ID, challengeId);
        query.put(ConstantsGlobal.PARENT_DOT_ID, parent.id);
        query.put(ConstantsGlobal.PARENT_DOT_TYPE, parent.type.name());
        return ChallengeLeaderBoardDAO.INSTANCE.findOne(query, null);
    }
}

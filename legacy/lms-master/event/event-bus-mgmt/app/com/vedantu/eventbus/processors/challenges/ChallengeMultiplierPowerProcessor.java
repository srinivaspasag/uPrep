package com.vedantu.eventbus.processors.challenges;

import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.daos.challenges.MultiplierPowerDAO;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager;

public class ChallengeMultiplierPowerProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(ChallengeMultiplierPowerProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        Event event = (Event) consumable;
        ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
                .fetchEventDetails();
        if (details == null) {
            LOGGER.error("no details found for event: " + event);
            return Status.NOT_CONSUMABLE;
        }
        DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, details.id);
        boolean process = true;
        int start = 0;
        while (process) {
            List<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(query,
                    MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.CHALLENGE_ID,
                            ConstantsGlobal.USER_ID, ConstantsGlobal.HINT, ConstantsGlobal.PARENT),
                            MongoManager.INCLUDE_FIELD), start, ChallengeDAO.DEFAULT_BATCH_SIZE,
                    null).results;
            if (challengeTakens.size() > 0) {
                start += challengeTakens.size();
                for (ChallengeTaken challengeTaken : challengeTakens) {
                    LOGGER.info("processing challenge[" + challengeTaken.challengeId
                            + "] for multiplier power for user[" + challengeTaken.userId + "]");
                    MultiplierPowerDAO.INSTANCE.allowtMultiplierPower(challengeTaken.userId,
                            challengeTaken.challengeId, challengeTaken.parent);
                }
            } else {
                process = false;
            }
        }
        return Status.SUCCESS;
    }

}

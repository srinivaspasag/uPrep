package com.vedantu.content.daos.challenges;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.mongo.VedantuBasicDAO;

public class ChallengeTakenDAO extends VedantuBasicDAO<ChallengeTaken, ObjectId> {

    private static final ALogger          LOGGER   = Logger.of(ChallengeTakenDAO.class);

    public static final ChallengeTakenDAO INSTANCE = new ChallengeTakenDAO();

    private ChallengeTakenDAO() {

        super(ChallengeTaken.class);
    }

    public ChallengeTaken addChallengeTaken(String userId, String challengeId, long endTime,
            SrcEntity parent) {

        ChallengeTaken challengeTaken = new ChallengeTaken(challengeId, userId, endTime, parent);
        save(challengeTaken);
        LOGGER.debug("saving challengeTaken info : " + challengeTaken);
        return challengeTaken;
    }

    public ChallengeTaken getChallengeTaken(String challengeId, String userId) {

        return getQuery().filter(ConstantsGlobal.CHALLENGE_ID, challengeId)
                .filter(ConstantsGlobal.USER_ID, userId).get();
    }
}

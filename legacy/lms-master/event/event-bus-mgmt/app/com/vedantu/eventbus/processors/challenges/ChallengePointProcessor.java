package com.vedantu.eventbus.processors.challenges;

import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.daos.challenges.ChallengeUserInfoDAO;
import com.vedantu.content.daos.challenges.MultiplierPowerDAO;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.enums.challenges.RankType;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.models.challenges.ChallengeUserInfo;
import com.vedantu.content.models.challenges.MultiplierPower;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.enums.points.PointCategory;
import com.vedantu.eventbus.utils.PointIncrementer;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.enums.Status;
import com.vedantu.user.daos.UserPointsDAO;
import com.vedantu.user.daos.UserPointsDetailDAO;
import com.vedantu.user.models.points.UserPoints;

public class ChallengePointProcessor extends AbstractChallengeRewardProcessor {

    private static final ALogger LOGGER = Logger.of(ChallengePointProcessor.class);

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
            List<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(query, null,
                    start, ChallengeDAO.DEFAULT_BATCH_SIZE, null).results;
            if (challengeTakens.size() > 0) {
                start += challengeTakens.size();

                for (ChallengeTaken challengeTaken : challengeTakens) {
                    LOGGER.info("incrementing points for user[" + challengeTaken.userId
                            + "] for challenge[" + challengeTaken.challengeId + "]");
                    incrementChallengePoint(challengeTaken, details, event);
                }
            } else {
                process = false;
            }
        }
        return Status.SUCCESS;
    }

    private void incrementChallengePoint(ChallengeTaken challengeTaken,
            ChallengeSearchIndexDetails details, Event event) {

        if (!challengeTaken.success) {
            LOGGER.debug("user has not answered the question correctly hence not counting the point processor");
            return;
        }
        MultiplierPowerType multiplierPowerType = MultiplierPowerType.SINGLE;
        MultiplierPower activeMultiplierPower = MultiplierPowerDAO.INSTANCE
                .getActiveMultiplierPower(challengeTaken.userId, challengeTaken.parent);
        if (activeMultiplierPower != null) {
            activeMultiplierPower.useCount++;
            MultiplierPowerDAO.INSTANCE.save(activeMultiplierPower);
            multiplierPowerType = activeMultiplierPower.type;
        }

        int basePoints = getEarnedChallengePointsAfterHintTaken(challengeTaken.hint, details);
        LOGGER.info("user[" + challengeTaken.userId + "], has earned [" + basePoints
                + "] basePoints for challenge[" + challengeTaken.challengeId + "] with ["
                + challengeTaken.hint + "] hints");

        int totalPoints = basePoints * multiplierPowerType.getMultiplier();

        challengeTaken.multiplierPower = multiplierPowerType;
        challengeTaken.basePoint = basePoints;
        challengeTaken.totalPoint = basePoints * challengeTaken.multiplierPower.getMultiplier();
        ChallengeTakenDAO.INSTANCE.save(challengeTaken);

        UserPointsDetailDAO.INSTANCE.addUserPointsDetail(challengeTaken.userId, event.srcEntity,
                PointCategory.CHALLENGE, totalPoints);

        LOGGER.info("incrementing over-all point for user[" + challengeTaken.userId + "]");

        // increment the user total points
        UserPoints userPoint = UserPointsDAO.INSTANCE.getUserPoints(challengeTaken.userId);
        userPoint.addPointInfoAsActor(PointCategory.CHALLENGE, totalPoints);
        PointIncrementer incrementer = PointIncrementer.getInstance();
        incrementer.saveUserPoints(userPoint, event);
        updateChallengeRanks(challengeTaken, totalPoints);
    }

    /**
     * this update challenge weekly, monthly, overall leaderboard
     **/
    private void updateChallengeRanks(ChallengeTaken challengeTaken, int totalPoints) {

        LOGGER.debug("======================== processing challenge user infos for rank calculation ======================");
        for (RankType rankType : RankType.values()) {

            ChallengeUserInfo challengeUserInfo = ChallengeUserInfoDAO.INSTANCE
                    .getChallengeUserInfo(challengeTaken.userId, rankType, challengeTaken.parent);

            LOGGER.info("===========================  current " + rankType.name()
                    + " points of user[" + challengeTaken.userId + "] [" + challengeUserInfo.points
                    + "], and total points earned  for challenge[" + challengeTaken.challengeId
                    + "]: " + totalPoints + " ============================");

            challengeUserInfo.updateHintCount(challengeTaken.hint);
            challengeUserInfo.points += totalPoints;
            challengeUserInfo.correctAttempts++;
            challengeUserInfo.calculateStrikeRate();
            ChallengeUserInfoDAO.INSTANCE.save(challengeUserInfo);
            LOGGER.info("=========================== updating user[" + challengeUserInfo.userId
                    + "] ChallengePointInfo: " + challengeUserInfo + "  in es rankType:" + rankType
                    + " ============================");
        }
    }

    private int
            getEarnedChallengePointsAfterHintTaken(int hint, ChallengeSearchIndexDetails details) {

        if (details.hintsDeductionValues == null || hint == 0) {
            return details.maxBid;
        }
        int deducations = 0;
        for (int i = 0; i <= (hint - 1); i++) {
            deducations += details.hintsDeductionValues.get(i);
        }
        int finalEarnedPoints = details.maxBid - deducations;
        return (finalEarnedPoints < 0 ? 0 : finalEarnedPoints);
    }
}

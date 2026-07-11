package com.vedantu.content.enums;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.challenges.ChallengeLeaderBoardDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.enums.challenges.MultiplierPowerValidityType;
import com.vedantu.content.models.challenges.ChallengeLeaderBoard;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.models.challenges.MultiplierPower;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;

public enum MultiplierPowerRule {

    TOP_LEADER_BOARD(5, 1, MultiplierPowerValidityType.CHALLENGE, MultiplierPowerType.TRIPLE) {

        @Override
        public MultiplierPower allowatMulitplierPower(String userId, String challengeId,
                SrcEntity parent) {

            DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, challengeId);
            query.put(ConstantsGlobal.PARENT_DOT_ID, parent.id);
            query.put(ConstantsGlobal.PARENT_DOT_TYPE, parent.type.name());

            MultiplierPower multiplierPower = null;

            List<ChallengeLeaderBoard> leaderBoards = ChallengeLeaderBoardDAO.INSTANCE.getInfos(
                    query, null, MongoManager.NO_START, 1, new BasicDBObject(
                            ConstantsGlobal.RANKER, SortOrder.ASC.getValue())).results;
            if (leaderBoards.isEmpty()) {
                return null;
            }
            ChallengeLeaderBoard leaderBoard = leaderBoards.get(0);

            if (StringUtils.equals(userId, leaderBoard.userId)) {
                multiplierPower = new MultiplierPower(userId, this.getPower(), this.getValidFor(),
                        this.getValidityType(), new SrcEntity(EntityType.CHALLENGE, challengeId),
                        this.name(), parent);
            }
            return multiplierPower;
        }
    },
    SOLVE_THREE_CHALLENGE_CONTINUOUSLY_WITH_NO_HINT(
            5,
            3,
            MultiplierPowerValidityType.DAYS,
            MultiplierPowerType.DOUBLE) {

        @Override
        public MultiplierPower allowatMulitplierPower(String userId, String challengeId,
                SrcEntity parent) {

            int continousCorrectCount = getContinousZeroHintSuccessChallengeTakenCount(userId,
                    this.getMinZeroHintSuccessCount());
            LOGGER.debug("in last [" + this.getMinZeroHintSuccessCount()
                    + "] corectly answered challenge count with no hints : "
                    + continousCorrectCount);
            if (continousCorrectCount == this.getMinZeroHintSuccessCount()) {
                return new MultiplierPower(userId, this.getPower(), this.getValidFor(),
                        this.getValidityType(), new SrcEntity(EntityType.CHALLENGE, challengeId),
                        this.name(), parent);
            }
            return null;
        }
    },
    SOLVE_FIVE_CHALLENGE_CONTINUOUSLY_WITH_NO_HINT(
            5,
            5,
            MultiplierPowerValidityType.DAYS,
            MultiplierPowerType.TRIPLE) {

        @Override
        public MultiplierPower allowatMulitplierPower(String userId, String challengeId,
                SrcEntity parent) {

            int continousCorrectCount = getContinousZeroHintSuccessChallengeTakenCount(userId,
                    this.getMinZeroHintSuccessCount());
            LOGGER.debug("in last [" + this.getMinZeroHintSuccessCount()
                    + "] corectly answered challenge count with no hints : "
                    + continousCorrectCount);
            if (continousCorrectCount == this.getMinZeroHintSuccessCount()) {
                return new MultiplierPower(userId, this.getPower(), this.getValidFor(),
                        this.getValidityType(), new SrcEntity(EntityType.CHALLENGE, challengeId),
                        this.name(), parent);
            }
            return null;
        }
    };

    private int                         validFor;
    private int                         minZeroHintSuccessCount;
    private MultiplierPowerValidityType validityType;
    private MultiplierPowerType         power;

    private MultiplierPowerRule(int validFor, int minZeroHintSuccessCount,
            MultiplierPowerValidityType validityType, MultiplierPowerType power) {

        this.validFor = validFor;
        this.minZeroHintSuccessCount = minZeroHintSuccessCount;
        this.validityType = validityType;
        this.power = power;
    }

    public int getValidFor() {

        return validFor;
    }

    public MultiplierPowerValidityType getValidityType() {

        return validityType;
    }

    public MultiplierPowerType getPower() {

        return power;
    }

    public int getMinZeroHintSuccessCount() {

        return minZeroHintSuccessCount;
    }

    private static int getContinousZeroHintSuccessChallengeTakenCount(String userId, int size) {

        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.PROCESSED, true);
        List<ChallengeTaken> challTakens = ChallengeTakenDAO.INSTANCE.getInfos(query, MongoManager
                .getFieldsDBObject(Arrays.asList(ConstantsGlobal.HINT, ConstantsGlobal.SUCCESS),
                        MongoManager.INCLUDE_FIELD), MongoManager.NO_START, size,
                new BasicDBObject(ConstantsGlobal.TIME_CREATED, SortOrder.DESC.getValue())).results;
        int continousCorrectCount = 0;

        for (ChallengeTaken challengeTaken : challTakens) {
            if (challengeTaken.hint == 0 && challengeTaken.success) {
                continousCorrectCount++;
            }
        }
        return continousCorrectCount;
    }

    private static final ALogger LOGGER = Logger.of(MultiplierPowerRule.class);

    public abstract MultiplierPower allowatMulitplierPower(String userId, String challengeId,
            SrcEntity parent);
}

package com.vedantu.content.daos.challenges;

import org.bson.types.ObjectId;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.challenges.RankType;
import com.vedantu.content.models.challenges.ChallengeUserInfo;
import com.vedantu.mongo.VedantuBasicDAO;

public class ChallengeUserInfoDAO extends VedantuBasicDAO<ChallengeUserInfo, ObjectId> {

    public final static ChallengeUserInfoDAO INSTANCE = new ChallengeUserInfoDAO();

    private ChallengeUserInfoDAO() {

        super(ChallengeUserInfo.class);
    }

    public ChallengeUserInfo addOrUpdateTotalAttempts(String userId, RankType rankType,
            SrcEntity parent) {

        ChallengeUserInfo challengeUserInfo = getDS().findAndModify(
                getQuery().filter(ConstantsGlobal.USER_ID, userId)
                        .filter(ConstantsGlobal.RANK_IDENTIFIER, rankType.identifier())
                        .filter(ConstantsGlobal.PARENT_DOT_ID, parent.id)
                        .filter(ConstantsGlobal.PARENT_DOT_TYPE, parent.type),
                getDS().createUpdateOperations(entityClazz).inc(ConstantsGlobal.TOTAL_ATTEMPTS, 1));
        if (challengeUserInfo == null) {
            challengeUserInfo = new ChallengeUserInfo(userId, rankType, parent);
            challengeUserInfo.totalAttempts = 1;
            save(challengeUserInfo);
        }
        return challengeUserInfo;
    }

    // parent == organization {id, type}
    public ChallengeUserInfo
            getChallengeUserInfo(String userId, RankType rankType, SrcEntity parent) {

        ChallengeUserInfo challengeUserInfo = getQuery().filter(ConstantsGlobal.USER_ID, userId)
                .filter(ConstantsGlobal.PARENT_DOT_ID, parent.id)
                .filter(ConstantsGlobal.PARENT_DOT_TYPE, parent.type)
                .filter(ConstantsGlobal.RANK_IDENTIFIER, rankType.identifier()).get();
        if (challengeUserInfo == null) {
            challengeUserInfo = new ChallengeUserInfo(userId, rankType, parent);
            save(challengeUserInfo);
        }
        return challengeUserInfo;
    }
}

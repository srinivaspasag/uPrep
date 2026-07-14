package com.vedantu.content.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.daos.challenges.ChallengeLeaderBoardDAO;
import com.vedantu.content.daos.challenges.ChallengeUserInfoDAO;
import com.vedantu.content.enums.challenges.RankType;
import com.vedantu.content.managers.ChallengeManager;
import com.vedantu.content.models.challenges.ChallengeLeaderBoard;
import com.vedantu.content.models.challenges.ChallengeUserInfo;
import com.vedantu.content.pojos.responses.challenges.GetChallengeGlobalLeaderBoardRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeLeaderBoardRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.pojos.UserInfo;

public class ChallengeUtils {

    private static final ALogger LOGGER = Logger.of(ChallengeUtils.class);

    public static ListResponse<GetChallengeLeaderBoardRes> getChallengeLeaderBoard(String orgId,
            String userId, String challengeId, int start, int size, boolean addMyRank) {

        LOGGER.debug("getting challenge[" + challengeId + "] leader board ");
        ListResponse<GetChallengeLeaderBoardRes> leaders = new ListResponse<GetChallengeLeaderBoardRes>();
        DBObject challengeQuery = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, challengeId);
        challengeQuery.put(ConstantsGlobal.PARENT_DOT_ID, orgId);
        challengeQuery.put(ConstantsGlobal.PARENT_DOT_TYPE, EntityType.ORGANIZATION.name());
        VedantuDBResult<ChallengeLeaderBoard> infos = ChallengeLeaderBoardDAO.INSTANCE.getInfos(
                challengeQuery, null, start, size, MongoManager.getSortQuery(
                        ConstantsGlobal.RANKER, MongoManager.SortOrder.ASC.name()));
        leaders.totalHits = infos.totalHits;

        Set<String> userIds = new HashSet<String>();
        boolean userAppeared = false;
        int i = 0;
        GetChallengeLeaderBoardRes lastLeaderRes = null;
        for (ChallengeLeaderBoard lBoard : infos.results) {
            GetChallengeLeaderBoardRes cLeaderRes = new GetChallengeLeaderBoardRes(lBoard.rank,
                    lBoard.timeTaken, lBoard.hint, lBoard.userId);
            i++;
            if (StringUtils.equals(userId, lBoard.userId)) {
                userAppeared = true;
            }
            if (i == size && !userAppeared && addMyRank) {
                lastLeaderRes = cLeaderRes;
                continue;
            } else {
                leaders.list.add(cLeaderRes);
            }
            userIds.add(lBoard.userId);
        }
        if (lastLeaderRes != null) {
            userIds.add(lastLeaderRes.userId);
        }
        if (addMyRank && !userIds.contains(userId)) {
            DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, challengeId);
            query.put(ConstantsGlobal.USER_ID, userId);
            query.put(ConstantsGlobal.PARENT_DOT_ID, orgId);
            query.put(ConstantsGlobal.PARENT_DOT_TYPE, EntityType.ORGANIZATION.name());
            ChallengeLeaderBoard leaderBoard = ChallengeLeaderBoardDAO.INSTANCE
                    .findOne(query, null);
            if (leaderBoard != null) {
                lastLeaderRes = new GetChallengeLeaderBoardRes(leaderBoard.rank,
                        leaderBoard.timeTaken, leaderBoard.hint, leaderBoard.userId);
            }
            if (lastLeaderRes != null) {
                userIds.add(lastLeaderRes.userId);
                leaders.list.add(lastLeaderRes);
            }
        }
        Map<String, ModelBasicInfo> usersMap = ChallengeManager.getUserInfoMap(orgId, userIds);
        for (GetChallengeLeaderBoardRes leader : leaders.list) {
            if (usersMap.get(leader.userId) != null) {
                leader.user = (UserInfo) usersMap.get(leader.userId);
                leader.userId = null;
            }
        }

        return leaders;
    }

    public static ListResponse<GetChallengeGlobalLeaderBoardRes> getChallengeGlobalLeaderBoard(
            String orgId, String userId, int start, int size, boolean addMyRank, RankType rankType) {

        ListResponse<GetChallengeGlobalLeaderBoardRes> leaders = new ListResponse<GetChallengeGlobalLeaderBoardRes>();

        DBObject orderQuery = new BasicDBObject(ConstantsGlobal.POINTS, SortOrder.DESC.getValue());
        orderQuery.put(ConstantsGlobal.STRIKE_RATE, SortOrder.DESC.getValue());
        DBObject selectQuery = new BasicDBObject(ConstantsGlobal.RANK_IDENTIFIER,
                rankType.identifier());
        selectQuery.put(ConstantsGlobal.PARENT_DOT_ID, orgId);
        selectQuery.put(ConstantsGlobal.PARENT_DOT_TYPE, EntityType.ORGANIZATION.name());
        selectQuery.put(ConstantsGlobal.POINTS, new BasicDBObject("$ne", 0));
        VedantuDBResult<ChallengeUserInfo> infos = ChallengeUserInfoDAO.INSTANCE.getInfos(
                selectQuery, null, start, size, orderQuery);

        leaders.totalHits = infos.totalHits;

        Set<String> userIds = new HashSet<String>();
        boolean userAppeared = false;
        GetChallengeGlobalLeaderBoardRes lastLeaderRes = null;
        int i = 0;
        for (ChallengeUserInfo rankInfo : infos.results) {
            GetChallengeGlobalLeaderBoardRes rankDetailRes = new GetChallengeGlobalLeaderBoardRes(
                    rankInfo.userId, rankInfo.points, rankInfo.totalAttempts,
                    rankInfo.correctAttempts, rankInfo.strikeRate, rankInfo.hintsCountMap,
                    rankInfo.type);
            i++;
            rankDetailRes.rank = i;
            if (StringUtils.equals(userId, rankInfo.userId)) {
                userAppeared = true;
            }
            if (i == size && !userAppeared && addMyRank) {
                lastLeaderRes = rankDetailRes;
                continue;
            }
            userIds.add(rankInfo.userId);
            leaders.list.add(rankDetailRes);
        }

        if (lastLeaderRes != null) {
            userIds.add(lastLeaderRes.userId);
        }

        if (addMyRank && !userIds.contains(userId)) {
            DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
            query.put(ConstantsGlobal.RANK_IDENTIFIER, rankType.identifier());
            query.put(ConstantsGlobal.PARENT_DOT_ID, orgId);
            query.put(ConstantsGlobal.PARENT_DOT_TYPE, EntityType.ORGANIZATION.name());
            ChallengeUserInfo uInfo = ChallengeUserInfoDAO.INSTANCE.findOne(query, null);
            if (uInfo != null) {

                userIds.add(uInfo.userId);
                lastLeaderRes = new GetChallengeGlobalLeaderBoardRes(uInfo.userId, uInfo.points,
                        uInfo.totalAttempts, uInfo.correctAttempts, uInfo.strikeRate,
                        uInfo.hintsCountMap, uInfo.type);
                lastLeaderRes.rank = getUserGlobalChallengeRank(userId, uInfo, rankType);
            }
            if (lastLeaderRes != null) {
                leaders.list.add(lastLeaderRes);
            }
        }
        Map<String, ModelBasicInfo> usersMap = ChallengeManager.getUserInfoMap(orgId, userIds);
        for (GetChallengeGlobalLeaderBoardRes leader : leaders.list) {

            if (usersMap.get(leader.userId) != null) {
                leader.user = (UserInfo) usersMap.get(leader.userId);
                leader.userId = null;
            }
        }

        return leaders;
    }

    public static long getUserGlobalChallengeRank(String userId, ChallengeUserInfo userRankDetails,
            RankType rankType) {

        DBObject query = new BasicDBObject(ConstantsGlobal.RANK_IDENTIFIER, rankType.identifier());
        query.put(ConstantsGlobal.POINTS, new BasicDBObject("$gt", userRankDetails.points));
        query.put(ConstantsGlobal.PARENT_DOT_ID, userRankDetails.parent.id);
        query.put(ConstantsGlobal.PARENT_DOT_TYPE, userRankDetails.parent.type.name());
        // this query count total no of users who has point greater than user
        long previousBucketsTotalSize = ChallengeUserInfoDAO.INSTANCE.count(query);

        query.put(ConstantsGlobal.POINTS, userRankDetails.points);
        DBObject orderQuery = new BasicDBObject(ConstantsGlobal.STRIKE_RATE,
                SortOrder.DESC.getValue());

        int globalRank = 0;
        VedantuDBResult<ChallengeUserInfo> infos = ChallengeUserInfoDAO.INSTANCE.getInfos(query,
                MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.USER_ID),
                        MongoManager.INCLUDE_FIELD), MongoManager.NO_START, MongoManager.NO_LIMIT,
                orderQuery);

        for (ChallengeUserInfo info : infos.results) {
            globalRank++;
            if (StringUtils.equals(info.userId, userId)) {
                break;
            }
        }
        globalRank += previousBucketsTotalSize;
        LOGGER.info("challenge global rank[type:" + rankType + "] of user[" + userId + "] : "
                + globalRank);
        return globalRank;
    }

}

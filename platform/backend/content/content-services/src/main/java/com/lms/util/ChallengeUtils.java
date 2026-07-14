package com.lms.util;

import com.amazonaws.services.ecs.model.SortOrder;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.RankType;
import com.lms.models.ChallengeLeaderBoard;
import com.lms.models.ChallengeUserInfo;
import com.lms.pojos.responce.GetChallengeGlobalLeaderBoardRes;
import com.lms.pojos.responce.GetChallengeLeaderBoardRes;
import com.lms.repository.ChallengeLeaderBoardRepo;
import com.lms.services.serviceImpl.CommentServiceImpl;
import com.lms.user.vedantu.user.pojo.UserInfo;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ChallengeUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChallengeUtils.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CommentServiceImpl commentServiceimpl;
    @Autowired
    private ChallengeLeaderBoardRepo challengeLeaderBoardRepo;

    public ListResponse<GetChallengeLeaderBoardRes> getChallengeLeaderBoard(String orgId,
                                                                            String userId, String challengeId, int start, int size, boolean addMyRank) {

        logger.debug("getting challenge[" + challengeId + "] leader board ");
        ListResponse<GetChallengeLeaderBoardRes> leaders = new ListResponse<GetChallengeLeaderBoardRes>();
        Query challengeQuery = new Query();
        Criteria criteria = new Criteria();
        //DBObject challengeQuery = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, challengeId);
        criteria.and(ConstantsGlobal.CHALLENGE_ID).is(challengeId);
        criteria.and(ConstantsGlobal.PARENT_DOT_ID).is(orgId);
        criteria.and(ConstantsGlobal.PARENT_DOT_TYPE).is(EntityType.ORGANIZATION);
        challengeQuery.addCriteria(criteria);
        List<ChallengeLeaderBoard> infos = mongoTemplate.find(challengeQuery, ChallengeLeaderBoard.class);
        ChallengeLeaderBoard cLeaderInfo = challengeLeaderBoardRepo.findByChallengeIdAndParentIdAndParentType(challengeId, orgId, EntityType.ORGANIZATION);
        Set<String> userIds = new HashSet<String>();
        boolean userAppeared = false;
        int i = 0;
        GetChallengeLeaderBoardRes lastLeaderRes = null;
        for (ChallengeLeaderBoard lBoard : infos) {
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
            Query query = new Query();
            Criteria criteria1 = new Criteria();
            //  DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID, challengeId);
            criteria1.and(ConstantsGlobal.USER_ID).is(userId);
            criteria1.and(ConstantsGlobal.PARENT_DOT_ID).is(orgId);
            criteria1.and(ConstantsGlobal.PARENT_DOT_TYPE).is(EntityType.ORGANIZATION.name());
            query.addCriteria(criteria);
            List<ChallengeLeaderBoard> leaderBoard = mongoTemplate.find(query, ChallengeLeaderBoard.class);
            if (leaderBoard != null && leaderBoard.size() != 0) {
                lastLeaderRes = new GetChallengeLeaderBoardRes(leaderBoard.get(0).rank,
                        leaderBoard.get(0).timeTaken, leaderBoard.get(0).hint, leaderBoard.get(0).userId);
            }
            if (lastLeaderRes != null) {
                userIds.add(lastLeaderRes.userId);
                leaders.list.add(lastLeaderRes);
            }
        }
        Map<String, ModelBasicInfo> usersMap = commentServiceimpl.getUserInfoMap(orgId, userIds);
        for (GetChallengeLeaderBoardRes leader : leaders.list) {
            if (usersMap.get(leader.userId) != null) {
                leader.user = (UserInfo) usersMap.get(leader.userId);
                leader.userId = null;
            }
        }

        return leaders;
    }

    public ListResponse<GetChallengeGlobalLeaderBoardRes> getChallengeGlobalLeaderBoard(
            String orgId, String userId, int start, int size, boolean addMyRank, RankType rankType) {

        ListResponse<GetChallengeGlobalLeaderBoardRes> leaders = new ListResponse<GetChallengeGlobalLeaderBoardRes>();
        Query orderQuery = new Query();
        Criteria criteria = new Criteria();
        //DBObject orderQuery = new BasicDBObject(ConstantsGlobal.POINTS, SortOrder.DESC.getValue());
        criteria.and(ConstantsGlobal.POINTS).is(SortOrder.DESC);
        criteria.and(ConstantsGlobal.STRIKE_RATE).is(SortOrder.DESC);
        Query selectQuery = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and(ConstantsGlobal.RANK_IDENTIFIER).is(rankType.identifier());
        criteria1.and(ConstantsGlobal.PARENT_DOT_ID).is(orgId);
        criteria1.and(ConstantsGlobal.PARENT_DOT_TYPE).is(EntityType.ORGANIZATION.name());
        criteria1.and(ConstantsGlobal.POINTS).is(0);
        selectQuery.addCriteria(criteria.andOperator(criteria1));
        // selectQuery.addCriteria(criteria1);
        List<ChallengeUserInfo> infos = mongoTemplate.find(selectQuery, ChallengeUserInfo.class);
        leaders.totalHits = infos.stream().count();

        Set<String> userIds = new HashSet<String>();
        boolean userAppeared = false;
        GetChallengeGlobalLeaderBoardRes lastLeaderRes = null;
        int i = 0;
        for (ChallengeUserInfo rankInfo : infos) {
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
            Query query = new Query();
            Criteria criteria2 = new Criteria();
            criteria2.and(ConstantsGlobal.USER_ID).is(userId);
            criteria2.and(ConstantsGlobal.RANK_IDENTIFIER).is(rankType.identifier());
            criteria2.and(ConstantsGlobal.PARENT_DOT_ID).is(orgId);
            criteria2.and(ConstantsGlobal.PARENT_DOT_TYPE).is(EntityType.ORGANIZATION.name());
            query.addCriteria(criteria2);
            List<ChallengeUserInfo> uInfo = mongoTemplate.find(query, ChallengeUserInfo.class);
            if (uInfo != null && uInfo.size() != 0) {

                userIds.add(uInfo.get(0).userId);
                lastLeaderRes = new GetChallengeGlobalLeaderBoardRes(uInfo.get(0).userId, uInfo.get(0).points,
                        uInfo.get(0).totalAttempts, uInfo.get(0).correctAttempts, uInfo.get(0).strikeRate,
                        uInfo.get(0).hintsCountMap, uInfo.get(0).type);
                lastLeaderRes.rank = getUserGlobalChallengeRank(userId, uInfo.get(0), rankType);
            }
            if (lastLeaderRes != null) {
                leaders.list.add(lastLeaderRes);
            }
        }
        Map<String, ModelBasicInfo> usersMap = commentServiceimpl.getUserInfoMap(orgId, userIds);
        for (GetChallengeGlobalLeaderBoardRes leader : leaders.list) {

            if (usersMap.get(leader.userId) != null) {
                leader.user = (UserInfo) usersMap.get(leader.userId);
                leader.userId = null;
            }
        }

        return leaders;

    }

    public long getUserGlobalChallengeRank(String userId, ChallengeUserInfo userRankDetails,
                                           RankType rankType) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.RANK_IDENTIFIER).is(rankType.identifier());
        criteria.and(ConstantsGlobal.POINTS).gt(userRankDetails.points);
        criteria.and(ConstantsGlobal.PARENT_DOT_ID).is(userRankDetails.parent.id);
        criteria.and(ConstantsGlobal.PARENT_DOT_TYPE).is(userRankDetails.parent.type.name());
        // this query count total no of users who has point greater than user
        query.addCriteria(criteria);
        // long previousBucketsTotalSize = query.c

        criteria.and(ConstantsGlobal.POINTS).is(userRankDetails.points);
        Query orderQuery = new Query();
        criteria.and(ConstantsGlobal.STRIKE_RATE).is(SortOrder.DESC);
        orderQuery.addCriteria(criteria);
        int globalRank = 0;
        List<ChallengeUserInfo> infos = mongoTemplate.find(orderQuery, ChallengeUserInfo.class);

        for (ChallengeUserInfo info : infos) {
            globalRank++;
            if (StringUtils.equals(info.userId, userId)) {
                break;
            }
        }

        Long count = infos.stream().filter(i -> i.getPoints() > userRankDetails.getPoints()).count();
        globalRank += count;
        logger.info("challenge global rank[type:" + rankType + "] of user[" + userId + "] : "
                + globalRank);
        return globalRank;
    }

}

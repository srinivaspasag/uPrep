package com.vedantu.content.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.util.TimeUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.constants.QuestionConstants;
import com.vedantu.content.daos.ChannelDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.daos.challenges.ChallengeTakenDAO;
import com.vedantu.content.daos.challenges.ChallengeUserInfoDAO;
import com.vedantu.content.daos.challenges.MultiplierPowerDAO;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.content.enums.challenges.RankType;
import com.vedantu.content.enums.search.SearchResultType;
import com.vedantu.content.models.Channel;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.content.models.challenges.ChallengeTaken;
import com.vedantu.content.models.challenges.ChallengeUserInfo;
import com.vedantu.content.models.challenges.MultiplierPower;
import com.vedantu.content.pojos.ChallengeTakenBasicInfo;
import com.vedantu.content.pojos.requests.challenges.AddChallengeReq;
import com.vedantu.content.pojos.requests.challenges.AttemptChallengeReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeGlobalLeaderBoardReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeHintReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeLeaderBoardReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeStatsReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeUserInfoReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengesReq;
import com.vedantu.content.pojos.requests.channels.AddContentToChannelReq;
import com.vedantu.content.pojos.responses.challenges.AddChallengeRes;
import com.vedantu.content.pojos.responses.challenges.AttempteChallengeRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeAttemptInfoRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeDetailsRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeGlobalLeaderBoardRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeHitRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeLeaderBoardRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeStatsRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeUserInfoRes;
import com.vedantu.content.pojos.responses.questions.GetQuestionRes;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.content.utils.ChallengeUtils;
import com.vedantu.content.utils.EntityUserActionUtils;
import com.vedantu.events.utils.EventUtil;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.search.utils.ElasticSearchUtils;
import com.vedantu.user.pojos.UserInfo;

public class ChallengeManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(ChallengeManager.class);

    public static AddChallengeRes addChallenge(AddChallengeReq addChallReq) throws VedantuException {

        Question question = QuestionDAO.INSTANCE.getQuestion(addChallReq.qid);
        addChallReq.validateRequestParams();
        Channel channel = ChannelDAO.INSTANCE.getChannel(addChallReq.channelId);

        Difficulty challengeDifficulty = StringUtils.isEmpty(addChallReq.difficulty) ? question.difficulty
                : Difficulty.valueOfKey(addChallReq.difficulty);
        Challenge challenge = ChallengeDAO.INSTANCE.addChallenge(addChallReq.userId,
                addChallReq.name, addChallReq.lifeTime, addChallReq.duration, addChallReq.maxBid,
                addChallReq.publishType, challengeDifficulty, addChallReq.scope, addChallReq.hints,
                addChallReq.initialBidPool, addChallReq.hintsDeduction, new SrcEntity(
                        EntityType.QUESTION, question._getStringId()), question.boardIds,
                question.targetIds, question.type, question.tags, addChallReq.contentSrc);

        AddContentToChannelReq addContentToChannelReq = new AddContentToChannelReq();
        addContentToChannelReq.callingUserId = addChallReq.callingUserId;
        addContentToChannelReq.userId = addChallReq.userId;
        addContentToChannelReq.id = channel._getStringId();
        addContentToChannelReq.entity = new SrcEntity(EntityType.CHALLENGE,
                challenge._getStringId());
        addContentToChannelReq.orgId = addChallReq.contentSrc.id;
        ChannelManager.addContentToChannel(addContentToChannelReq, true);
        AddChallengeRes addChallengeRes = new AddChallengeRes();
        addChallengeRes.fromMongoModel(challenge);
        ChallengeSearchIndexDetails details = new ChallengeSearchIndexDetails();
        details.fromMongoModel(challenge);
        details.channelId = channel._getStringId();
        details.setAction(EventActionType.ADD.name());
        details.setUserAction(UserActionType.ADDED);
        details.isNotificationEnabled = true;
        generateEventAysc(addChallReq.userId, details, EventType.INDEX_CHALLENGE);
        generateEndChallengeEvent(challenge);
        return addChallengeRes;
    }

    public static GetChallengeRes getChallenge(GetChallengeReq getChallReq) throws VedantuException {

        Challenge challenge = ChallengeDAO.INSTANCE.getChallenge(getChallReq.id);
        GetChallengeRes getChallRes = new GetChallengeRes();
        getChallRes.fromMongoModel(challenge);
        return getChallRes;
    }

    public static GetChallengeDetailsRes getChallengeDetails(GetChallengeReq getChallReq)
            throws VedantuException {

        DBObject query = new BasicDBObject(ConstantsGlobal._ID, new ObjectId(getChallReq.id));
        Challenge challenge = ChallengeDAO.INSTANCE.findOne(query, null);
        if (challenge == null) {
            LOGGER.error("no challenge found for challengeId: " + getChallReq.id + " in es");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_NOT_FOUND);
        }

        if (challenge.scope == Scope.PRIVATE
                && !StringUtils.equals(getChallReq.userId, challenge.userId)) {
            LOGGER.error("challenge[" + getChallReq.id + "] is not yet opened to public ");
            throw new VedantuException(VedantuErrorCode.PRIVATE_CHALLENGE);
        }
        long cTime = System.currentTimeMillis();
        if (challenge.status == ChallengeStatus.ENDED || cTime >= challenge.endTime) {
            LOGGER.info("challenge[" + getChallReq.id + "] already ended");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_ENDED);
        }
        ChallengeTaken challengeTaken = ChallengeTakenDAO.INSTANCE.getChallengeTaken(
                getChallReq.id, getChallReq.userId);
        if (challengeTaken != null) {
            LOGGER.error("user[" + getChallReq.userId + "], has already taken  challenge["
                    + getChallReq.id + "]");
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }

        // TODO: as of now it's only single question on a challenge, in future
        // if we want to add multiple question/test in challenge than rendering
        // of entities should be a list insted of single question
        if (CollectionUtils.isEmpty(challenge.entities)) {
            LOGGER.error("no entites found for challenge[" + getChallReq.id + "]");
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        long endTimeForAttemptingChallenge = challenge.duration == 0 ? challenge.endTime : (Math
                .min(cTime + TimeUtil.toMillis(challenge.duration), challenge.endTime));
        challengeTaken = ChallengeTakenDAO.INSTANCE.addChallengeTaken(getChallReq.userId,
                getChallReq.id, endTimeForAttemptingChallenge, challenge.contentSrc);
        GetChallengeDetailsRes getChallDetailsRes = new GetChallengeDetailsRes();
        getChallDetailsRes.token = challengeTaken._getStringId();

        EntityUserActionUtils.addEntityUserAction(getChallReq.userId, new SrcEntity(
                EntityType.CHALLENGE, getChallReq.id), UserActionType.ATTEMPTED, false);

        // increase the challenge attempt count for user in ChallengeUserInfo
        for (RankType rankType : RankType.values()) {
            ChallengeUserInfoDAO.INSTANCE.addOrUpdateTotalAttempts(getChallReq.userId, rankType,
                    challenge.contentSrc);
        }

        Question question = QuestionDAO.INSTANCE.getQuestion(challenge.entities.get(0).id, Arrays
                .asList(ConstantsGlobal.CONTENT, QuestionConstants.OPTIONS, ConstantsGlobal.TYPE,
                        QuestionConstants.DIFFICULTY));
        GetQuestionRes qusRes = new GetQuestionRes();
        qusRes.fromMongoModel(question);
        qusRes.addImageSrcUrl();
        getChallDetailsRes.entity = qusRes;
        getChallDetailsRes.startTime = challengeTaken.timeCreated;
        return getChallDetailsRes;
    }

    public static GetChallengeHitRes getChallengeHint(GetChallengeHintReq hintReq)
            throws VedantuException {

        ChallengeTaken challengeTaken = ChallengeTakenDAO.INSTANCE.getById(hintReq.token);
        validateChallengeTaken(challengeTaken, hintReq.token, hintReq.userId);
        Challenge challenge = ChallengeDAO.INSTANCE.getChallenge(challengeTaken.challengeId);
        LOGGER.debug("hints: " + challenge.hints);
        if (challenge == null || CollectionUtils.isEmpty(challenge.hints)
                || challengeTaken.hint >= challenge.hints.size()) {
            LOGGER.error("no hints found for challenge[" + challengeTaken.challengeId + "]");
            throw new VedantuException(VedantuErrorCode.NO_HINT);
        }

        GetChallengeHitRes hintRes = new GetChallengeHitRes();
        hintRes.hint = challenge.hints.get(challengeTaken.hint);
        hintRes.addImageSrcUrl();
        challengeTaken.hint++;
        ChallengeTakenDAO.INSTANCE.updateModel(challengeTaken, Arrays.asList(ConstantsGlobal.HINT));
        LOGGER.info("sending hint[" + hintRes.hint + "], for challenge["
                + challengeTaken.challengeId + "], to user[" + hintReq.userId + "]");
        return hintRes;
    }

    public static AttempteChallengeRes attemptChallenge(AttemptChallengeReq attemptReq)
            throws VedantuException {

        ChallengeTaken challengeTaken = ChallengeTakenDAO.INSTANCE.getById(attemptReq.token);
        validateChallengeTaken(challengeTaken, attemptReq.token, attemptReq.userId);
        if (CollectionUtils.isNotEmpty(challengeTaken.answer)) {
            LOGGER.error("user[" + attemptReq.userId
                    + "] has already submited answer for challenge[" + challengeTaken.challengeId
                    + "]");
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }
        challengeTaken.addAnswer(attemptReq.answer);
        ChallengeTakenDAO.INSTANCE.updateModel(challengeTaken, Arrays.asList(
                ConstantsGlobal.ANSWER, ConstantsGlobal.ANSWER_TIME, ConstantsGlobal.TIME_TAKEN));
        LOGGER.info("saved answer for challenge[" + challengeTaken.challengeId + "], for user["
                + attemptReq.userId + "]");
        AttempteChallengeRes attempteChallRes = new AttempteChallengeRes();
        attempteChallRes.processed = true;
        return null;
    }

    @SuppressWarnings({ "unchecked" })
    public static GetChallengeStatsRes getChallengeStats(GetChallengeStatsReq getChallStats)
            throws VedantuException {

        AbstractFacetBuilder statusFacet = FacetBuilders.termsFacet("statusFacet").field(
                ConstantsGlobal.STATUS);
        statusFacet.facetFilter(FilterBuilders.andFilter(FilterBuilders.termFilter("contentSrc.id",
                getChallStats.orgId), FilterBuilders.termFilter("contentSrc.type",
                EntityType.ORGANIZATION.name().toLowerCase())));
        Map<String, Object> result = (Map<String, Object>) ElasticSearchUtils.facetQueryResult(
                EntityType.CHALLENGE.getIndexName(), EntityType.CHALLENGE.getIndexType(),
                statusFacet).get(ConstantsGlobal.FACET);
        GetChallengeStatsRes getChallStatusRes = new GetChallengeStatsRes();
        getChallStatusRes.facet = result;
        return getChallStatusRes;
    }

    @SuppressWarnings("unchecked")
    public static SearchListResponse<GetChallengeRes> getChallenges(
            GetChallengesReq getChallengesReq) throws VedantuException {

        BoolQueryBuilder boolQuery = buildSearchQuery(getChallengesReq, EntityType.CHALLENGE);

        if (StringUtils.isNotEmpty(getChallengesReq.channelId)) {
            boolQuery.must(QueryBuilders.hasChildQuery(UserActionType.ADDED.getSearchIndexType(),
                    QueryBuilders
                            .fieldQuery(ConstantsGlobal.DST_DOT_ID, getChallengesReq.channelId)));
        }
        boolQuery.must(QueryBuilders.termQuery("contentSrc.id", getChallengesReq.orgId));
        boolQuery.must(QueryBuilders.termQuery("contentSrc.type", EntityType.ORGANIZATION.name()
                .toLowerCase()));

        BoolFilterBuilder boolFilter = buildSearchFilter(getChallengesReq, EntityType.CHALLENGE,
                true);

        if (getChallengesReq.status != null) {
            boolFilter.must(FilterBuilders.termFilter(ConstantsGlobal.STATUS,
                    getChallengesReq.status.name().toLowerCase()));
        }
        if (getChallengesReq.type != null) {
            boolFilter.must(FilterBuilders.termFilter(ConstantsGlobal.TYPE, getChallengesReq.type
                    .name().toLowerCase()));
        }
        if (StringUtils.isNotEmpty(getChallengesReq.qType)) {
            QuestionType quesType = QuestionType.valueOfKey(getChallengesReq.qType);
            boolFilter.must(FilterBuilders.termFilter(ConstantsGlobal.Q_TYPES, quesType.name()
                    .toLowerCase()));
        }

        if (getChallengesReq.difficulty != null) {
            boolFilter.must(FilterBuilders.termFilter(ConstantsGlobal.DIFFICULTY,
                    getChallengesReq.difficulty.name().toLowerCase()));
        }

        AbstractFacetBuilder pointsToWintFacet = FacetBuilders
                .statisticalFacet("pointStatFacet")
                .fields(ConstantsGlobal.MAX_BID)
                .facetFilter(
                        FilterBuilders.termFilter(ConstantsGlobal.STATUS, ChallengeStatus.ACTIVE
                                .name().toLowerCase()));
        AbstractFacetBuilder statusFacet = FacetBuilders.termsFacet("statusFacet").field(
                ConstantsGlobal.STATUS);
        statusFacet.facetFilter(FilterBuilders.andFilter(FilterBuilders.termFilter("contentSrc.id",
                getChallengesReq.orgId), FilterBuilders.termFilter("contentSrc.type",
                EntityType.ORGANIZATION.name().toLowerCase())));

        long attemptedChallengesCount = 0;
        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, getChallengesReq.userId);
        query.put(ConstantsGlobal.PARENT_DOT_ID, getChallengesReq.orgId);
        query.put(ConstantsGlobal.PARENT_DOT_TYPE, EntityType.ORGANIZATION.name());

        if (StringUtils.isNotEmpty(getChallengesReq.channelId)) {
            // if the challenId is provide then get all challengesIds belonging to this channel
            DBObject challengeQuery = new BasicDBObject(ConstantsGlobal.TARGET_DOT_ID,
                    getChallengesReq.channelId);
            challengeQuery.put(ConstantsGlobal.TARGET_DOT_TYPE, EntityType.CHANNEL.name());
            List<Object> channelChallengeIds = LibraryContentLinksDAO.INSTANCE.getDistinct(
                    "source.id", challengeQuery);
            query.put(ConstantsGlobal.CHALLENGE_ID, new BasicDBObject(MongoManager.IN_QUERY,
                    channelChallengeIds.toArray()));
        }

        if (getChallengesReq.resultType == SearchResultType.ATTEMPTED) {

            VedantuDBResult<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(
                    query,
                    MongoManager.getFieldsDBObject(Arrays.asList(ConstantsGlobal.CHALLENGE_ID),
                            MongoManager.INCLUDE_FIELD), MongoManager.NO_START,
                    MongoManager.NO_LIMIT, null);

            Set<String> challengeIds = new HashSet<String>();

            for (ChallengeTaken cTaken : challengeTakens.results) {
                challengeIds.add(cTaken.challengeId);
            }

            attemptedChallengesCount = challengeIds.size();

            boolQuery.must(QueryBuilders.termsQuery(ConstantsGlobal.ID, challengeIds.toArray()));
        } else {
            attemptedChallengesCount = ChallengeTakenDAO.INSTANCE.count(query);
        }

        Set<String> entityIds = new HashSet<String>();
        SearchListResponse<GetChallengeRes> getChallengesRes = getEntityInfos(getChallengesReq,
                EntityType.CHALLENGE, GetChallengeRes.class, boolQuery, boolFilter, null, entityIds);
        QueryBuilder facetQuery = StringUtils.isEmpty(getChallengesReq.channelId) ? QueryBuilders
                .matchAllQuery() : QueryBuilders.hasChildQuery(
                UserActionType.ADDED.getSearchIndexType(),
                QueryBuilders.fieldQuery(ConstantsGlobal.DST_DOT_ID, getChallengesReq.channelId));
        getChallengesRes.facet = (Map<String, Object>) ElasticSearchUtils.facetQueryResult(
                EntityType.CHALLENGE.getIndexName(), EntityType.CHALLENGE.getIndexType(),
                facetQuery, pointsToWintFacet, statusFacet).get(ConstantsGlobal.FACET);

        addAttemptInfo(getChallengesReq.userId, entityIds, getChallengesRes);
        Map<String, Long> attemptMap = new HashMap<String, Long>();
        attemptMap.put("count", attemptedChallengesCount);
        getChallengesRes.facet.put("attemptFacet", attemptMap);
        return getChallengesRes;
    }

    private static void addAttemptInfo(String userId, Set<String> entityIds,
            SearchListResponse<GetChallengeRes> res) {

        Set<String> topperIds = new HashSet<String>();
        for (GetChallengeRes challRes : res.list) {
            if (challRes.topperIds != null) {
                topperIds.addAll(challRes.topperIds);
            }
        }

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(null, topperIds);

        DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        query.put(ConstantsGlobal.CHALLENGE_ID,
                new BasicDBObject(MongoManager.IN_QUERY, entityIds.toArray()));
        VedantuDBResult<ChallengeTaken> challengeTakens = ChallengeTakenDAO.INSTANCE.getInfos(
                query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        Map<String, ChallengeTaken> challengeTakenMap = new HashMap<String, ChallengeTaken>();
        for (ChallengeTaken tk : challengeTakens.results) {
            challengeTakenMap.put(tk.challengeId, tk);
        }

        for (GetChallengeRes challenge : res.list) {
            ChallengeTaken challengeTaken = challengeTakenMap.get(challenge.id);
            if (challengeTaken != null) {
                challenge.attempted = true;
                challenge.info = (ChallengeTakenBasicInfo) challengeTaken.toBasicInfo();
            }

            if (challenge.topperIds != null) {
                for (String topperId : challenge.topperIds) {
                    challenge.addTopper((UserInfo) userInfos.get(topperId));
                }
            }
        }
    }

    public static GetChallengeAttemptInfoRes getChallengeAttemptInfo(
            GetChallengeReq getChallengeAttemptInfo) throws VedantuException {

        ChallengeTaken challengeTaken = ChallengeTakenDAO.INSTANCE.getChallengeTaken(
                getChallengeAttemptInfo.id, getChallengeAttemptInfo.userId);
        GetChallengeAttemptInfoRes challAttemptInfo = null;
        if (challengeTaken == null) {
            LOGGER.error("no challenge found for userId : " + getChallengeAttemptInfo.userId);
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED);
        }
        challAttemptInfo = new GetChallengeAttemptInfoRes(challengeTaken.challengeId,
                challengeTaken.userId, challengeTaken.endTime, challengeTaken.bid,
                challengeTaken.bidded, challengeTaken.hint, challengeTaken.answerTime,
                challengeTaken.timeTaken, challengeTaken.success, challengeTaken.processed,
                challengeTaken.multiplierPower, challengeTaken.basePoint,
                challengeTaken.totalPoint, challengeTaken.answer, challengeTaken.timeCreated,
                challengeTaken.lastUpdated);

        return challAttemptInfo;
    }

    public static GetChallengeUserInfoRes getChallengeUserInfo(GetChallengeUserInfoReq req)
            throws VedantuException {

        SrcEntity parentOrg = new SrcEntity(EntityType.ORGANIZATION, req.orgId);
        ChallengeUserInfo challengeUserInfo = ChallengeUserInfoDAO.INSTANCE.getChallengeUserInfo(
                req.userId, RankType.valueOfKey(req.rankType), parentOrg);
        MultiplierPowerType multiplier = MultiplierPowerType.SINGLE;

        MultiplierPower multiplierPower = MultiplierPowerDAO.INSTANCE.getActiveMultiplierPower(
                req.userId, parentOrg);
        if (multiplierPower != null) {
            multiplier = multiplierPower.type;
            LOGGER.error("multiplierPower found for user[" + req.userId + "]");
        }

        GetChallengeUserInfoRes res = new GetChallengeUserInfoRes(challengeUserInfo.points,
                challengeUserInfo.totalAttempts, challengeUserInfo.correctAttempts,
                challengeUserInfo.strikeRate, challengeUserInfo.hintsCountMap,
                challengeUserInfo.type, challengeUserInfo.rankIdentifier, multiplier);
        return res;
    }

    public static ListResponse<GetChallengeLeaderBoardRes> getChallengeLeaderBoard(
            GetChallengeLeaderBoardReq req) throws VedantuException {

        return ChallengeUtils.getChallengeLeaderBoard(req.orgId, req.userId, req.id, req.start,
                req.size, (req.start == 0));
    }

    public static ListResponse<GetChallengeGlobalLeaderBoardRes> getChallengeGlobalLeaderBoard(
            GetChallengeGlobalLeaderBoardReq req) throws VedantuException {

        return ChallengeUtils.getChallengeGlobalLeaderBoard(req.orgId, req.userId, req.start,
                req.size, (req.start == 0), req.rankType);
    }

    private static void validateChallengeTaken(ChallengeTaken challengeTaken, String token,
            String callingUserId) throws VedantuException {

        if (challengeTaken == null || !StringUtils.equals(callingUserId, challengeTaken.userId)) {
            LOGGER.error("no challenge found for token : " + token
                    + ", or this token is not valid for userId: " + callingUserId
                    + ", challengeTaken:" + challengeTaken);
            throw new VedantuException(VedantuErrorCode.INVALID_TOKEN);
        }

        if (challengeTaken.endTime < System.currentTimeMillis()) {
            LOGGER.error("challenge[" + challengeTaken.challengeId + "] token[" + token + "]");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_ENDED);
        }

    }

    private static void generateEndChallengeEvent(Challenge challenge) throws VedantuException {

        ChallengeSearchIndexDetails details = new ChallengeSearchIndexDetails();
        details.fromMongoModel(challenge);
        details.setAction(EventActionType.UPDATE.name());
        details.setUserAction(UserActionType.ENDED);
        details.enableNotifcation(true);
        LOGGER.info("endChallenge event time: " + challenge.endTime);
        String endChallengeEventId = EventUtil.generateEvent(EventType.END_CHALLENGE, null,
                challenge.userId, details, new SrcEntity(EntityType.USER, challenge.userId), null,
                challenge.endTime);
        challenge.endChallengeEventId = endChallengeEventId;
        ChallengeDAO.INSTANCE.updateModel(challenge, Arrays.asList("endChallengeEventId"));
        LOGGER.info("endChallegenEventId: " + endChallengeEventId);
    }
}

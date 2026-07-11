package com.lms.services.serviceImpl;

import com.lms.board.model.GranteeOrgProgram;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ContentLinkRelationshipDetails;
import com.lms.common.utils.EventUtil;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.components.AnalyticsComponent;
import com.lms.constants.QuestionConstants;
import com.lms.enums.*;
import com.lms.managers.AbstractTestManager;
import com.lms.models.*;
import com.lms.pojos.ChallengeTakenBasicInfo;
import com.lms.pojos.GetChallengeLeaderBoardReq;
import com.lms.pojos.GetChallengeReq;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.questions.GetQuestionRes;
import com.lms.pojos.search.details.ChallengeSearchIndexDetails;
import com.lms.repository.*;
import com.lms.services.ChallengesService;
import com.lms.util.ChallengeUtils;
import com.lms.utils.EntityUserActionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChallengesServiceImpl extends AbstractTestManager implements ChallengesService {
    private static final Logger logger = LoggerFactory.getLogger(ChallengesServiceImpl.class);
    public static final long MILLIS_PER_DAY = 86400000L;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private ChannelsRepo channelsRepo;
    @Autowired
    private ChallengeRepo challengeRepo;
    @Autowired
    private ChannelsServiceImpl channelsServiceImpl;
    @Autowired
    private ChallengeTakenRepo challengeTakenRepo;
    @Autowired
    private ModuleServiceImpl moduleServiceImpl;
    @Autowired
    private EntityUserActionUtils entityUserActionUtils;
    @Autowired
    private ChallengeUserInfoRepo challengeUserInfoRepo;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ChallengeUtils challengeUtils;
    @Autowired
    private MultiplierPowerRepo multiplierPowerRepo;
    @Autowired
    private ContentServiceImpl contentService;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Value("${learnpedia.id}")
    private String learnPediaId;
    @Autowired
    private AnalyticsComponent analyticsComponent;
    @Autowired
    private EventUtil eventUtil;

    @Override
    public VedantuResponse addchallenge(AddChallengeReq addChallengeReq) {

        if (addChallengeReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AddChallengeRes addChallRes = addChallenge(addChallengeReq);

        return new VedantuResponse(addChallRes);
    }

    @Override
    public VedantuResponse getchallengeInfo(GetChallengeReq getChallengeReq) {
        if (getChallengeReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetChallengeRes getChallRes = getChallenge(getChallengeReq);
        // we don't want to show the ui what all entities are added in this
        // challenge
        getChallRes.entities = null;
        return new VedantuResponse(getChallRes);
    }

    @Override
    public VedantuResponse getchallengeDetails(GetChallengeReq getChallengeReq) {
        if (getChallengeReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetChallengeDetailsRes getChallDetailsRes = getChallengeDetails(getChallengeReq);

        return new VedantuResponse(getChallDetailsRes);
    }

    public List<GetChallengeRes> getChallenges(
            GetChallengesReq getChallengesReq) throws VedantuException {
        Query query = new Query();
        Criteria crite = new Criteria();
        Query query9 = new Query();
        Criteria criteria9 = new Criteria();

        buildSearchQuery(getChallengesReq, EntityType.CHALLENGE, query, crite);

        /*if (!StringUtils.isEmpty(getChallengesReq.channelId)) {
            boolQuery.must(QueryBuilders.hasChildQuery(UserActionType.ADDED.getSearchIndexType(),
                    QueryBuilders
                            .fieldQuery(ConstantsGlobal.DST_DOT_ID, getChallengesReq.channelId)));
        }*/
        criteria9.and("contentSrc.id").is(getChallengesReq.orgId);
        criteria9.and("contentSrc.type").is(EntityType.ORGANIZATION.name());
        buildSearchFilter(getChallengesReq, EntityType.CHALLENGE, true, query, crite);

        if (getChallengesReq.status != null) {
            criteria9.and(ConstantsGlobal.STATUS).is(getChallengesReq.status.name());
        }
        if (getChallengesReq.type != null) {
            criteria9.and(ConstantsGlobal.TYPE).is(getChallengesReq.type.name());
        }
        if (!StringUtils.isEmpty(getChallengesReq.qType)) {
            QuestionType quesType = QuestionType.valueOfKey(getChallengesReq.qType);
            //   criteria9.and(ConstantsGlobal.Q_TYPES).is(quesType.name());
        }

        if (getChallengesReq.difficulty != null) {
            criteria9.and(ConstantsGlobal.DIFFICULTY).is(getChallengesReq.difficulty.name());
        }


        long attemptedChallengesCount = 0;
        Query query1 = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and(ConstantsGlobal.USER_ID).is(getChallengesReq.userId);
        criteria1.and(ConstantsGlobal.PARENT_DOT_ID).is(getChallengesReq.orgId);
        criteria1.and(ConstantsGlobal.PARENT_DOT_TYPE).is(EntityType.ORGANIZATION.name());

        if (!StringUtils.isEmpty(getChallengesReq.channelId)) {
            // if the challenId is provide then get all challengesIds belonging to this channel
            Query challengeQuery = new Query();
            Criteria criteria2 = new Criteria();

            criteria2.and(ConstantsGlobal.TARGET_DOT_ID).is(getChallengesReq.channelId);
            criteria2.and(ConstantsGlobal.TARGET_DOT_TYPE).is(EntityType.CHANNEL.name());
            List<LibraryContentLink> channelChallengeIds = mongoTemplate.find(challengeQuery.addCriteria(criteria2), LibraryContentLink.class);
            criteria1.and(ConstantsGlobal.CHALLENGE_ID).in(channelChallengeIds.toArray());
        }

        if (getChallengesReq.resultType == SearchResultType.ATTEMPTED) {

            List<ChallengeTaken> challengeTakens = mongoTemplate.find(query1.addCriteria(criteria1), ChallengeTaken.class);

            Set<String> challengeIds = new HashSet<String>();

            for (ChallengeTaken cTaken : challengeTakens) {
                challengeIds.add(cTaken.challengeId);
            }

            attemptedChallengesCount = challengeIds.size();

            criteria9.and(ConstantsGlobal.ID).in(challengeIds.toArray());
        } else {
            attemptedChallengesCount = challengeTakenRepo.findAll().stream().count();

        }

        Set<String> entityIds = new HashSet<String>();
        List<Challenge> challenges = mongoTemplate.find(query9.addCriteria(criteria9), Challenge.class);
        List<GetChallengeRes> getChallengeRes = new ArrayList<GetChallengeRes>();
        for (Challenge challenge : challenges) {
            GetChallengeRes getChallengeRes1 = new GetChallengeRes();
            ChallengeTakenBasicInfo challengeTakenBasicInfo = new ChallengeTakenBasicInfo(challenge);
            getChallengeRes1.setInfo(challengeTakenBasicInfo);
            getChallengeRes.add(getChallengeRes1);
        }
        //    addAttemptInfo(getChallengesReq.userId, entityIds, getChallengeRes);
        Map<String, Long> attemptMap = new HashMap<String, Long>();
        attemptMap.put("count", attemptedChallengesCount);
        // getChallengesRes.add(attemptMap);
        return getChallengeRes;

    }

    @Override
    public VedantuResponse gethint(GetChallengeHintReq getChallengeHintReq) {
        if (getChallengeHintReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        }
        GetChallengeHitRes getChallHintRes = getChallengeHint(getChallengeHintReq);

        return new VedantuResponse(getChallHintRes);
    }

    @Override
    public VedantuResponse attemptchallenge(AttemptChallengeReq attemptChallengeReq) {
        if (attemptChallengeReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        AttempteChallengeRes attemptChallRes = attemptChallenge(attemptChallengeReq);

        return new VedantuResponse(attemptChallRes);
    }

    @Override
    public VedantuResponse getchallengeStats(GetChallengeStatsReq getChallengeStatsReq) {
        if (getChallengeStatsReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        GetChallengeStatsRes getChallStatsRes = getChallengeStats(getChallengeStatsReq);

        return new VedantuResponse(getChallStatsRes);
    }

    @Override
    public VedantuResponse getchallenges(GetChallengesReq getChallengesReq) {

        if (getChallengesReq.resultType == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "please provide resultType");
        List<GetChallengeRes> getChallengesRes = getChallenges(getChallengesReq);

        return new VedantuResponse(getChallengesRes);
    }

    @Override
    public VedantuResponse getchallengeUserAttemptInfo(GetChallengeReq getChallengeReq) {
        if (getChallengeReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetChallengeAttemptInfoRes getChallAttemptInfoRes = getChallengeAttemptInfo(getChallengeReq);

        return new VedantuResponse(getChallAttemptInfoRes);
    }

    @Override
    public VedantuResponse getUserchallengeInfo(GetChallengeUserInfoReq getChallengeUserInfoReq) {
        if (getChallengeUserInfoReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        GetChallengeUserInfoRes getChallUserInfoRes = getChallengeUserInfo(getChallengeUserInfoReq);

        return new VedantuResponse(getChallUserInfoRes);
    }

    @Override
    public VedantuResponse getchallengeLeaderBoard(GetChallengeLeaderBoardReq getChallengeLeaderBoardReq) {
        if (getChallengeLeaderBoardReq == null)
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        ListResponse<GetChallengeLeaderBoardRes> getChallLeaderBoardRes = getChallengeLeaderBoard(getChallengeLeaderBoardReq);

        return new VedantuResponse(getChallLeaderBoardRes);
    }

    @Override
    public VedantuResponse getchallengeGlobalLeaderBoard(GetChallengeGlobalLeaderBoardReq getChallengeGlobalLeaderBoardReq) {
        if (getChallengeGlobalLeaderBoardReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ListResponse<GetChallengeGlobalLeaderBoardRes> getChallGlobalLeaderBoardRes = getChallengeGlobalLeaderBoard(getChallengeGlobalLeaderBoardReq);

        return new VedantuResponse(getChallGlobalLeaderBoardRes);

    }


    public AddChallengeRes addChallenge(AddChallengeReq addChallReq) throws VedantuException {

        Optional<Question> question1 = questionRepo.findById(addChallReq.getQid());
        if (!question1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND,
                    "no question found with id:" + addChallReq.getQid());
        }
        Question question = question1.get();
        addChallReq.validateRequestParams();
        Optional<Channel> channel1 = channelsRepo.findById(addChallReq.getChannelId());
        if (!channel1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.CHANNEL_NOT_FOUND);
        }
        Channel channel = channel1.get();
        Difficulty challengeDifficulty = StringUtils.isEmpty(addChallReq.difficulty) ? question.difficulty
                : Difficulty.valueOfKey(addChallReq.difficulty);
        Challenge challenge = addChallenge(addChallReq.userId,
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
        addContentToChannel(addContentToChannelReq, true);
        AddChallengeRes addChallengeRes = new AddChallengeRes();
        addChallengeRes.fromMongoModel(challenge);
        ChallengeSearchIndexDetails details = new ChallengeSearchIndexDetails();
        details.fromMongoModel(challenge);
        details.channelId = channel._getStringId();
        details.setAction(UserActionType.EventActionType.ADD.name());
        details.setUserAction(UserActionType.ADDED);
        details.isNotificationEnabled = true;
        generateEventAysc(addChallReq.userId, details, EventType.INDEX_CHALLENGE);
        generateEndChallengeEvent(challenge);
        return addChallengeRes;
    }

    public Challenge addChallenge(String userId, String name, int lifeTime,
                                  int duration, int maxBid, Scope publishType, Difficulty difficulty,
                                  Scope scope, List<String> hints, int initialBidPool,
                                  List<Integer> hintsDeduction, SrcEntity entity, Set<String> brdIds,
                                  Set<String> targetIds, QuestionType qType, Set<String> tags,
                                  SrcEntity contentSrc) {

        ChallengeType type = duration > 0 ? ChallengeType.FIXED_TIME
                : ChallengeType.NO_TIME;
        BidType bidType = maxBid <= 0 ? BidType.NON_BIDDABLE : BidType.BIDDABLE;

        Challenge challenge = new Challenge(userId, name, type, lifeTime,
                duration, scope, difficulty, bidType, maxBid, initialBidPool);
        challenge.contentSrc = contentSrc;
        challenge.hintsDeductionValues = hintsDeduction;
        challenge.qTypes = new HashSet<String>(Arrays.asList(qType.name()));
        challenge.publishType = publishType;
        challenge.tags = tags;
        challenge.addTargets(targetIds);
        challenge.addBoards(brdIds);
        challenge.addEntity(entity);
        challenge.addHint(hints);
        logger.debug("saving challenge : " + challenge);
        challengeRepo.save(challenge);
        return challenge;
    }

    public AddContentToChannelRes addContentToChannel(AddContentToChannelReq addReq,
                                                      boolean addOnIndexing) throws VedantuException {

        Optional<Channel> channel1 = channelsRepo.findById(addReq.getId());
        if (!channel1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.CHANNEL_NOT_FOUND);
        }
        Channel channel = channel1.get();

        UserActionType actionType = UserActionType.ADDED;
        LibraryContentLink link = channelsServiceImpl.addLink(addReq.entity,
                new SrcEntity(EntityType.CHANNEL, channel._getStringId()), actionType,
                addReq.userId, Scope.PUBLIC);
        AddContentToChannelRes addRes = new AddContentToChannelRes();
        addRes.processed = link != null;
        channelsServiceImpl.incContentCount(addReq.id);
        if (!addOnIndexing) {
            ContentLinkRelationshipDetails channelEntityDetails = new ContentLinkRelationshipDetails(
                    addReq.userId, addReq.entity, new SrcEntity(EntityType.CHANNEL, addReq.id),
                    Scope.PUBLIC);
            // updateUserActionMappintToEs(channelEntityDetails, addReq.entity, actionType, UserActionType.EventActionType.ADD, null);
        }
        return addRes;
    }

    private void generateEventAysc(String userId, ChallengeSearchIndexDetails details, EventType eventType) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, 0);
        });

    }

    private void generateEndChallengeEvent(Challenge challenge) throws VedantuException {

        ChallengeSearchIndexDetails details = new ChallengeSearchIndexDetails();
        details.fromMongoModel(challenge);
        details.setAction(UserActionType.EventActionType.UPDATE.name());
        details.setUserAction(UserActionType.ENDED);
        details.enableNotifcation(true);
        logger.info("endChallenge event time: " + challenge.endTime);
        String endChallengeEventId = eventUtil.generateEvent(EventType.END_CHALLENGE, null,
                challenge.userId, details, new SrcEntity(EntityType.USER, challenge.userId), null,
                challenge.endTime);
        //challenge.endChallengeEventId = endChallengeEventId;
        challengeRepo.save(challenge);
        logger.info("endChallegenEventId: " + endChallengeEventId);
    }

    public GetChallengeRes getChallenge(GetChallengeReq getChallReq) throws VedantuException {

        Optional<Challenge> challenge = challengeRepo.findById(getChallReq.getId());
        if (!challenge.isPresent()) {
            throw new VedantuException(VedantuErrorCode.CHALLENGE_NOT_FOUND);
        }
        GetChallengeRes getChallRes = new GetChallengeRes();
        getChallRes.fromMongoModel(challenge.get());
        return getChallRes;
    }

    public GetChallengeDetailsRes getChallengeDetails(GetChallengeReq getChallReq)
            throws VedantuException {

        Optional<Challenge> challenge1 = challengeRepo.findById(getChallReq.getId());
        //ChallengeDAO.INSTANCE.findOne(query, null);
        if (!challenge1.isPresent()) {
            logger.error("no challenge found for challengeId: " + getChallReq.id + " in es");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_NOT_FOUND);
        }
        Challenge challenge = challenge1.get();

        if (challenge.scope == Scope.PRIVATE
                && !getChallReq.userId.equals(challenge.userId)) {
            logger.error("challenge[" + getChallReq.id + "] is not yet opened to public ");
            throw new VedantuException(VedantuErrorCode.PRIVATE_CHALLENGE);
        }
        long cTime = System.currentTimeMillis();
        if (challenge.status == ChallengeStatus.ENDED || cTime >= challenge.endTime) {
            logger.info("challenge[" + getChallReq.id + "] already ended");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_ENDED);
        }
        ChallengeTaken challengeTaken = challengeTakenRepo.findByChallengeIdAndUserId(getChallReq.id, getChallReq.userId);
        if (challengeTaken != null) {
            logger.error("user[" + getChallReq.userId + "], has already taken  challenge["
                    + getChallReq.id + "]");
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }

        // TODO: as of now it's only single question on a challenge, in future
        // if we want to add multiple question/test in challenge than rendering
        // of entities should be a list insted of single question
        if (CollectionUtils.isEmpty(challenge.entities)) {
            logger.error("no entites found for challenge[" + getChallReq.id + "]");
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        long endTimeForAttemptingChallenge = challenge.duration == 0 ? challenge.endTime : (Math
                .min(cTime + TimeUnit.MILLISECONDS.toMillis(challenge.duration), challenge.endTime));
        challengeTaken = addChallengeTaken(getChallReq.userId,
                getChallReq.id, endTimeForAttemptingChallenge, challenge.contentSrc);
        GetChallengeDetailsRes getChallDetailsRes = new GetChallengeDetailsRes();
        getChallDetailsRes.token = challengeTaken._getStringId();

        entityUserActionUtils.addEntityUserAction(getChallReq.userId, new SrcEntity(
                EntityType.CHALLENGE, getChallReq.id), UserActionType.ATTEMPTED, false);
        // increase the challenge attempt count for user in ChallengeUserInfo
        for (RankType rankType : RankType.values()) {
            addOrUpdateTotalAttempts(getChallReq.userId, rankType,
                    challenge.contentSrc);
        }

        Question question = questionRepo.findByContentAndContentTypeIn(challenge.getEntities().get(0).getId(), Arrays.asList(ConstantsGlobal.CONTENT, QuestionConstants.OPTIONS, ConstantsGlobal.TYPE, QuestionConstants.DIFFICULTY));
        GetQuestionRes qusRes = new GetQuestionRes();
        qusRes.fromMongoModel(question);
        qusRes.addImageSrcUrl();
        getChallDetailsRes.entity = qusRes;
        getChallDetailsRes.startTime = challengeTaken.timeCreated;
        return getChallDetailsRes;

    }

    public ChallengeTaken addChallengeTaken(String userId, String challengeId, long endTime,
                                            SrcEntity parent) {

        ChallengeTaken challengeTaken = new ChallengeTaken(challengeId, userId, endTime, parent);
        challengeTakenRepo.save(challengeTaken);
        logger.debug("saving challengeTaken info : " + challengeTaken);
        return challengeTaken;
    }

    public ChallengeUserInfo addOrUpdateTotalAttempts(String userId, RankType rankType,
                                                      SrcEntity parent) {


        ChallengeUserInfo challengeUserInfo = challengeUserInfoRepo.findByUserIdAndTypeAndParentId(userId, rankType, parent);
        if (challengeUserInfo == null) {
            challengeUserInfo = new ChallengeUserInfo(userId, rankType, parent);
            challengeUserInfo.totalAttempts = 1;
            challengeUserInfoRepo.save(challengeUserInfo);
        } else {
            challengeUserInfo.setTotalAttempts(challengeUserInfo.getTotalAttempts() + 1);
        }

        return challengeUserInfo;
    }

    public GetChallengeHitRes getChallengeHint(GetChallengeHintReq hintReq)
            throws VedantuException {

        Optional<ChallengeTaken> challengeTaken1 = challengeTakenRepo.findById(hintReq.token);
        if (!challengeTaken1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "challengeTaken should not found");
        }
        ChallengeTaken challengeTaken = challengeTaken1.get();
        validateChallengeTaken(challengeTaken, hintReq.token, hintReq.userId);
        Challenge challenge = getChallenge(challengeTaken.getChallengeId());
        logger.debug("hints: " + challenge.hints);
        if (challenge == null || CollectionUtils.isEmpty(challenge.hints)
                || challengeTaken.hint >= challenge.hints.size()) {
            logger.error("no hints found for challenge[" + challengeTaken.challengeId + "]");
            throw new VedantuException(VedantuErrorCode.NO_HINT);
        }

        GetChallengeHitRes hintRes = new GetChallengeHitRes();
        hintRes.hint = challenge.hints.get(challengeTaken.hint);
        hintRes.addImageSrcUrl();
        challengeTaken.hint++;
        challengeTakenRepo.save(challengeTaken);
        logger.info("sending hint[" + hintRes.hint + "], for challenge["
                + challengeTaken.challengeId + "], to user[" + hintReq.userId + "]");
        return hintRes;
    }

    private void validateChallengeTaken(ChallengeTaken challengeTaken, String token,
                                        String callingUserId) throws VedantuException {

        if (challengeTaken == null || callingUserId.equals(challengeTaken.userId)) {
            logger.error("no challenge found for token : " + token
                    + ", or this token is not valid for userId: " + callingUserId
                    + ", challengeTaken:" + challengeTaken);
            throw new VedantuException(VedantuErrorCode.INVALID_TOKEN);
        }

        if (challengeTaken.endTime < System.currentTimeMillis()) {
            logger.error("challenge[" + challengeTaken.challengeId + "] token[" + token + "]");
            throw new VedantuException(VedantuErrorCode.CHALLENGE_ENDED);
        }

    }

    public Challenge getChallenge(String id) throws VedantuException {
        Optional<Challenge> challenge = challengeRepo.findById(id);
        if (!challenge.isPresent()) {
            throw new VedantuException(VedantuErrorCode.CHALLENGE_NOT_FOUND);
        }
        return challenge.get();
    }

    public AttempteChallengeRes attemptChallenge(AttemptChallengeReq attemptReq)
            throws VedantuException {

        Optional<ChallengeTaken> challengeTaken1 = challengeTakenRepo.findById(attemptReq.token);
        if (!challengeTaken1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "challengeTaken should not found");
        }
        ChallengeTaken challengeTaken = challengeTaken1.get();
        validateChallengeTaken(challengeTaken, attemptReq.token, attemptReq.userId);
        if (!CollectionUtils.isEmpty(challengeTaken.answer)) {
            logger.error("user[" + attemptReq.userId
                    + "] has already submited answer for challenge[" + challengeTaken.challengeId
                    + "]");
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }
        challengeTaken.addAnswer(attemptReq.answer);
        challengeTakenRepo.save(challengeTaken);
        logger.info("saved answer for challenge[" + challengeTaken.challengeId + "], for user["
                + attemptReq.userId + "]");
        AttempteChallengeRes attempteChallRes = new AttempteChallengeRes();
        attempteChallRes.processed = true;
        return null;
    }

    public GetChallengeStatsRes getChallengeStats(GetChallengeStatsReq getChallStats)
            throws VedantuException {

       /* AbstractFacetBuilder statusFacet = FacetBuilders.termsFacet("statusFacet").field(
                ConstantsGlobal.STATUS);
        statusFacet.facetFilter(FilterBuilders.andFilter(FilterBuilders.termFilter("contentSrc.id",
                getChallStats.orgId), FilterBuilders.termFilter("contentSrc.type",
                EntityType.ORGANIZATION.name().toLowerCase())));*/
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                + ConstantsGlobal.ID).is(getChallStats.getOrgId());
        criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                + ConstantsGlobal.TYPE).is(EntityType.ORGANIZATION.name().toLowerCase());

        List<Challenge> challenge = mongoTemplate.find(query.addCriteria(criteria), Challenge.class);
        Map<String, Object> result = new HashMap<>();
        result.put("challenge", challenge);
        GetChallengeStatsRes getChallStatusRes = new GetChallengeStatsRes();
        getChallStatusRes.facet = result;
        return getChallStatusRes;

    }

    public GetChallengeAttemptInfoRes getChallengeAttemptInfo(
            GetChallengeReq getChallengeAttemptInfo) throws VedantuException {

        ChallengeTaken challengeTaken = challengeTakenRepo.findByChallengeIdAndUserId(getChallengeAttemptInfo.id, getChallengeAttemptInfo.userId);
        GetChallengeAttemptInfoRes challAttemptInfo = null;
        if (challengeTaken == null) {
            logger.error("no challenge found for userId : " + getChallengeAttemptInfo.userId);
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

    public GetChallengeUserInfoRes getChallengeUserInfo(GetChallengeUserInfoReq req)
            throws VedantuException {

        SrcEntity parentOrg = new SrcEntity(EntityType.ORGANIZATION, req.getOrgId());
        ChallengeUserInfo challengeUserInfo = getChallengeUserInfo(
                req.userId, RankType.valueOfKey(req.rankType), parentOrg);
        MultiplierPowerType multiplier = MultiplierPowerType.SINGLE;

        MultiplierPower multiplierPower = getActiveMultiplierPower(req.userId, parentOrg);
        if (multiplierPower != null) {
            multiplier = multiplierPower.type;
            logger.error("multiplierPower found for user[" + req.userId + "]");
        }

        GetChallengeUserInfoRes res = new GetChallengeUserInfoRes(challengeUserInfo.points,
                challengeUserInfo.totalAttempts, challengeUserInfo.correctAttempts,
                challengeUserInfo.strikeRate, challengeUserInfo.hintsCountMap,
                challengeUserInfo.type, challengeUserInfo.rankIdentifier, multiplier);
        return res;
    }

    public ChallengeUserInfo
    getChallengeUserInfo(String userId, RankType rankType, SrcEntity parent) {

        ChallengeUserInfo challengeUserInfo = challengeUserInfoRepo.findByUserIdAndParentIdAndParentTypeAndRankIdentifier(userId, parent.getId(), parent.getType(), rankType.identifier());

        if (challengeUserInfo == null) {
            challengeUserInfo = new ChallengeUserInfo(userId, rankType, parent);
            challengeUserInfoRepo.save(challengeUserInfo);
        }
        return challengeUserInfo;
    }

    public MultiplierPower getActiveMultiplierPower(String userId, SrcEntity parent) {

        MultiplierPower activeMultiplierPower = null;
        Query query = new Query();
        Criteria criteria = new Criteria();
        //   DBObject query = new BasicDBObject(ConstantsGlobal.USER_ID, userId);
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.PARENT_DOT_ID).is(parent.id);
        criteria.and(ConstantsGlobal.PARENT_DOT_TYPE).is(parent.type.name());
        for (MultiplierPowerType powerType : MultiplierPowerType.values()) {
            // criteria.andOperator(Criteria.where(ConstantsGlobal.TYPE).is(powerType.name()));
            // query.addCriteria(criteria);
            List<MultiplierPower> results = multiplierPowerRepo.findByUserIdAndTypeAndParentIdAndParentType(userId, powerType.name(), parent.id, parent.type);
            // mongoTemplate.find(query, MultiplierPower.class);

            for (MultiplierPower multiplierPower : results) {
                activeMultiplierPower = getSuperMultiplierPower(activeMultiplierPower,
                        multiplierPower);
            }
        }
        return activeMultiplierPower;
    }

    public MultiplierPower getSuperMultiplierPower(MultiplierPower activePower,
                                                   MultiplierPower comparablePower) {

        // comparablePower can not be null
        if (activePower == null) {
            return isActivePower(comparablePower) ? comparablePower : null;
        }
        return isActivePower(comparablePower)
                && comparablePower.type.getMultiplier() > activePower.type.getMultiplier() ? comparablePower
                : activePower;
    }

    public boolean isActivePower(MultiplierPower multiplierPower) {

        boolean active = false;
        if (multiplierPower.validityType == MultiplierPowerValidityType.CHALLENGE) {
            active = multiplierPower.useCount < multiplierPower.validFor;
        } else if (multiplierPower.validityType == MultiplierPowerValidityType.DAYS) {
            long activeTime = multiplierPower.validFor * MILLIS_PER_DAY;
            active = System.currentTimeMillis() < (multiplierPower.timeCreated + activeTime);
        }
        return active;
    }

    public ListResponse<GetChallengeLeaderBoardRes> getChallengeLeaderBoard(
            GetChallengeLeaderBoardReq req) throws VedantuException {

        return challengeUtils.getChallengeLeaderBoard(req.orgId, req.userId, req.id, req.start,
                req.size, (req.start == 0));
    }

    public ListResponse<GetChallengeGlobalLeaderBoardRes> getChallengeGlobalLeaderBoard(
            GetChallengeGlobalLeaderBoardReq req) throws VedantuException {

        return challengeUtils.getChallengeGlobalLeaderBoard(req.orgId, req.userId, req.start,
                req.size, (req.start == 0), req.rankType);
    }

    protected void buildSearchQuery(AbstractContentSearchReq searchReq,
                                    EntityType entityType, Query query, Criteria criteria) {

        AtomicLong totalProgramHits = new AtomicLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = contentService.getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgs.add(granteeOrgProgram.providerOrgId);
        }
        buildSearchQuery(searchReq.resultType,
                searchReq._getResultForUserId(), searchReq.contentSrc, searchReq.includeTypes,
                searchReq.excludeTypes, searchReq.excludeIds, searchReq.query, entityType,
                grantedOrgs, query, criteria);
        try {
            addOrgStructureFilter(searchReq, query, criteria);
        } catch (VedantuException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void buildSearchQuery(SearchResultType resultType, String userId,
                                 SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
                                 List<String> excludeIds, String query, EntityType entityType, List<String> grantorOrgIds, Query query2, Criteria criteria) {

        logger.debug(" Getting " + entityType + " for user  :" + userId + " with query " + query);


        if (contentSrc != null) {
            if (contentSrc.type.name() != EntityType.ORGANIZATION.toString()) {
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).is(contentSrc.id);

            } else if (entityType == EntityType.DISCUSSION) {
                Optional<Organization> org1 = organizationRepo.findById(contentSrc.id);
                Organization org = org1.get();
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                switch (org.doubtsForumMode) {
                    case PRIVATE:
                        break;
                    case PUBLIC:
                        allOrgs.add(learnPediaId);
                        break;
                    default:
                        break;
                }
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).in(allOrgs.toArray());

            } else {
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                if (grantorOrgIds != null && grantorOrgIds.size() > 0) {
                    allOrgs.addAll(grantorOrgIds);
                }
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).in(allOrgs.toArray());

            }
            criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE).is(contentSrc.type.name());


        }

        // resultType.addSearchQueryFlter(boolQuery, userId);

        if (!CollectionUtils.isEmpty(excludeIds)) {
            criteria.and(ConstantsGlobal.ID).nin(excludeIds.toArray());
        }
        if (!CollectionUtils.isEmpty(includeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).in(includeTypes.toArray());

        }
        if (!CollectionUtils.isEmpty(excludeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).nin(VedantuStringUtils
                    .toLowerCase(excludeTypes).toArray());

        }

    }

    private void addOrgStructureFilter(AbstractContentSearchReq searchReq, Query query, Criteria criteria
    ) throws VedantuException {

        // checking if current OrgId and  programId are not empty
        if (!StringUtils.isEmpty(searchReq.orgId) && !StringUtils.isEmpty(searchReq.programId)) {
            //Marking current orgId to programOrgId
            String programOrgId = searchReq.orgId;
            AtomicLong totalProgramHits = new AtomicLong(0L);
            logger.debug("Current orgId" + searchReq);
            // getting all the rows of the current orgId as OrgId as key
            List<GranteeOrgProgram> granteeOrgPrograms = contentService.getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                // if programId from search request matches the programId from the list above assign the granteeId as the programId
                if (granteeOrgProgram.programId.equalsIgnoreCase(searchReq.programId)) {
                    programOrgId = granteeOrgProgram.providerOrgId;
                }
            }

            Collection<String> sectionIds = !StringUtils.isEmpty(searchReq.sectionId) ? Arrays
                    .asList(searchReq.sectionId) : analyticsComponent.getProgramSections(
                    programOrgId,
                    searchReq.programId,
                    StringUtils.isEmpty(searchReq.centerId) ? null : Arrays
                            .asList(searchReq.centerId));
            String childType = UserActionType.ADDED.getSearchIndexType();
            criteria.and(childType).in(sectionIds.toArray());
          /* boolQuery.must(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.inQuery(childType + ".dst.id", sectionIds.toArray())));*/
        }
    }

}

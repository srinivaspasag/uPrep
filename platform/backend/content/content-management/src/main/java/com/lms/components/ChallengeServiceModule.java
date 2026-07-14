package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ContentLinkRelationshipDetails;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.utils.EventUtil;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.BidType;
import com.lms.enums.ChallengeType;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionType;
import com.lms.models.Challenge;
import com.lms.models.Channel;
import com.lms.models.LibraryContentLink;
import com.lms.models.Question;
import com.lms.pojos.requests.AddChallengeReq;
import com.lms.pojos.requests.AddContentToChannelReq;
import com.lms.pojos.responce.AddChallengeRes;
import com.lms.pojos.responce.AddContentToChannelRes;
import com.lms.pojos.search.details.ChallengeSearchIndexDetails;
import com.lms.repository.*;
import com.lms.utils.EntityUserActionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChallengeServiceModule {
    private static final Logger logger = LoggerFactory.getLogger(ChallengeServiceModule.class);
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private ChannelsRepo channelsRepo;
    @Autowired
    private ChallengeRepo challengeRepo;
    @Autowired
    private ChallengeTakenRepo challengeTakenRepo;

    @Autowired
    private EntityUserActionUtils entityUserActionUtils;
    @Autowired
    private ChallengeUserInfoRepo challengeUserInfoRepo;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MultiplierPowerRepo multiplierPowerRepo;

    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Value("${learnpedia.id}")
    private String learnPediaId;
    @Autowired
    private AnalyticsComponent analyticsComponent;
    @Autowired
    private EventUtil eventUtil;

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
        questionComponent.generateEventAysc(addChallReq.userId, details, EventType.INDEX_CHALLENGE);
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
        challengeRepo.save(challenge);
        return challenge;
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

    public AddContentToChannelRes addContentToChannel(AddContentToChannelReq addReq,
                                                      boolean addOnIndexing) throws VedantuException {

        Optional<Channel> channel1 = channelsRepo.findById(addReq.getId());
        if (!channel1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.CHANNEL_NOT_FOUND);
        }
        Channel channel = channel1.get();

        UserActionType actionType = UserActionType.ADDED;
        LibraryContentLink link = addLink(addReq.entity,
                new SrcEntity(EntityType.CHANNEL, channel._getStringId()), actionType,
                addReq.userId, Scope.PUBLIC);
        AddContentToChannelRes addRes = new AddContentToChannelRes();
        addRes.processed = link != null;
        incContentCount(addReq.id);
        if (!addOnIndexing) {
            ContentLinkRelationshipDetails channelEntityDetails = new ContentLinkRelationshipDetails(
                    addReq.userId, addReq.entity, new SrcEntity(EntityType.CHANNEL, addReq.id),
                    Scope.PUBLIC);
            // updateUserActionMappintToEs(channelEntityDetails, addReq.entity, actionType, UserActionType.EventActionType.ADD, null);
        }
        return addRes;
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, EncryptionLevel.NA, false);
    }

    /* TODO Added by Shivank */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, updatedScope, scheduleInfo,
                downloadble, encLevel, allowDuplicates, null, -1);
    }

    /**
     * It will add active link to library
     *
     * @param content
     * @param targetEntity
     * @param linkType
     * @param actorId
     * @param updatedScope
     * @param scheduleInfo
     * @param downloadble
     * @param encLevel
     * @return
     * @throws VedantuException
     */
    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope updatedScope, ScheduleInfo scheduleInfo,
                                      Boolean downloadble, EncryptionLevel encLevel, boolean allowDuplicates,
                                      List<SrcEntity> downloadableEntities, long position) throws VedantuException {

        logger.debug("...................Inside function add link..............");

        AtomicLong totalHits = new AtomicLong(0L);
        List<LibraryContentLink> links = getLibraryContentLinks(content, targetEntity, linkType,
                actorId, VedantuRecordState.ACTIVE, 0, 1, totalHits);

        if (totalHits.longValue() > 1 && !allowDuplicates) {
            logger.error("content:" + content + ", already added to  targetEntity:" + targetEntity);
            throw new VedantuException(VedantuErrorCode.ALREADY_ADDED, "content:" + content
                    + ", already added to  targetEntity:" + targetEntity);

        }

        LibraryContentLink contentLinkage = null;

        if (!links.isEmpty() || links.size() != 0) {
            contentLinkage = links.get(0);
            logger.debug("Updating  content Link" + contentLinkage);

        } else {
            logger.debug("Creating new content Link");
            contentLinkage = new LibraryContentLink(targetEntity, content);

        }
        if (position != -1) {
            contentLinkage.position = position;
        }
        if (scheduleInfo != null) {
            contentLinkage.setSchedule(scheduleInfo);
        }
        if (actorId != null) {
            contentLinkage.userId = actorId;
        }
        if (linkType != null) {
            contentLinkage.linkType = linkType;
        }
        if (updatedScope != null && updatedScope != Scope.UNKNOWN) {
            logger.debug("New scope" + updatedScope);
            contentLinkage.setScope(updatedScope);
        }
        if (downloadble != null) {
            logger.debug("New downloadable state" + downloadble);

            if (contentLinkage.getScope() == Scope.PRIVATE && downloadble) {
                logger.debug("Can not make downloadable" + downloadble);
                throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE, "content:"
                        + content + ",is not visible" + targetEntity);
            }
            contentLinkage.setDownloadable(downloadble);
        }
        if (encLevel != null
                && (contentLinkage.getEncLevel() != EncryptionLevel.NA || encLevel != EncryptionLevel.NA)) {
            contentLinkage.setEncLevel(encLevel);
        }

        if (content.type == EntityType.MODULE) {
            contentLinkage.setDownloadableEntities(downloadableEntities);
        }

        logger.debug("Saving content link in ILE library"
                + contentLinkage.getDownloadableEntities());
        libraryContentLinksRepo.save(contentLinkage);

        return contentLinkage;
    }

    public List<LibraryContentLink> getLibraryContentLinks(SrcEntity content,
                                                           SrcEntity targetEntity, UserActionType linkType, String actorId,
                                                           VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        if (targetEntity != null) {
            if (targetEntity.type != null) {
                criteria.and("target.type").is(targetEntity.type);
                if (targetEntity.id != null) {
                    criteria.and("target.id").is(targetEntity.id);
                }
            }
        }
        if (content != null) {
            if (content.type != null) {
                criteria.and("source.type").is(content.getType());
                if (content.id != null) {
                    criteria.and("source.id").is(content.getId());
                }
            }

        }
        if (linkType != null) {
            criteria.and("linkType").is(linkType);
        }

        logger.debug("Querying for " + LibraryContentLink.class);

        if (recordState != null) {
            criteria.and("recordState").is(recordState);
        }
        query.addCriteria(criteria);
        logger.debug("Query: " + query.toString());
        List<LibraryContentLink> libraryLinks = mongoTemplate.find(query, LibraryContentLink.class);
        if (totalHits != null) {
            long count = libraryLinks.size();
            totalHits.set(count);
            logger.debug("Total matched results " + totalHits);
        }

        return libraryLinks;
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope scope) throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null);
    }

    public LibraryContentLink addLink(SrcEntity content, SrcEntity targetEntity,
                                      UserActionType linkType, String actorId, Scope scope, ScheduleInfo scheduleInfo)
            throws VedantuException {

        return addLink(content, targetEntity, linkType, actorId, scope, null, null);
    }

    public boolean incContentCount(String id) {
        String FIELD_ID = "_id";

        if (ObjectIdUtils.hasInvalidId(id)) {
            return false;
        }
        Criteria criteria = new Criteria();
        Query query = new Query();
        Update update = new Update();
        criteria.and(FIELD_ID).is(new ObjectId(id));
        update.set("contentCount", 1);
        query.addCriteria(criteria);
        mongoTemplate.updateFirst(query, update, Channel.class);
        return true;
    }
}

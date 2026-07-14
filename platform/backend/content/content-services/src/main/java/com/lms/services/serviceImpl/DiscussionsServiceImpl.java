package com.lms.services.serviceImpl;

import com.lms.billing.model.TeacherAnalytics;
import com.lms.billing.repository.TeacherAnalyticsRepo;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.QuestionComponent;
import com.lms.enums.AcceptanceState;
import com.lms.enums.DoubtState;
import com.lms.event.details.DoubtsProcessingDetails;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.managers.AbstractTestManager;
import com.lms.models.*;
import com.lms.models.tests.Assignment;
import com.lms.pojos.DoubtAssignmentDetails;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.file.GetFileRes;
import com.lms.pojos.responce.questions.GetQuestionRes;
import com.lms.pojos.responce.tests.GetAssignmentInfoRes;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import com.lms.repository.*;
import com.lms.services.DiscussionsService;
import com.lms.utils.IAttemptableEntity;
import com.lms.utils.ISocialEntity;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DiscussionsServiceImpl extends AbstractTestManager implements DiscussionsService {
    private final static Logger logger = LoggerFactory.getLogger(DiscussionsServiceImpl.class);
    @Value("${doubt.max.response.time}")
    private long DOUBT_MAX_RESPONSE_TIME;
    private final long DOUBT_MAX_RESPONSE_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(DOUBT_MAX_RESPONSE_TIME, TimeUnit.HOURS);
    @Value("${doubt.max.solution.time}")
    private long DOUBT_MAX_SOLUTION_TIME;
    @Value("${doubt.exhaust.time}")
    private final long DOUBT_MAX_SOLUTION_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(DOUBT_MAX_SOLUTION_TIME, TimeUnit.HOURS);
    private long DOUBT_EXHAUSTION_TIME;
    private final long DOUBT_EXHAUSTION_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(DOUBT_EXHAUSTION_TIME, TimeUnit.HOURS);


    @Autowired
    private DiscussionRepo discussionRepo;
    @Autowired
    private DoubtTransactionRepo doubtTransactionRepo;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private CommentServiceImpl commentServiceImpl;
    @Autowired
    private ContentServiceImpl contentServiceImpl;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private UserEntityAttemptRepo userEntityAttemptRepo;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private EntityUserActionMappingRepo EntityUserActionMappingRepo;
    @Autowired
    private TeacherAnalyticsRepo teacherAnalyticsRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Value("${learnpedia.id}")
    private String learnPediaid;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ChallengesServiceImpl challengesService;


    @Override
    public VedantuResponse adddiscussion(AddDiscussionReq addDiscussionReq) {
        if (addDiscussionReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing feilds");
        }
        AddDiscussionRes addDissRes = addDiscussion(addDiscussionReq);

        return new VedantuResponse(addDissRes);
    }

    @Override
    public VedantuResponse removediscussion(RemoveDiscussionReq removeDiscussionReq) {
        if (removeDiscussionReq==null) {
           throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        RemoveDiscussionRes removeDissRes = removeDiscussion(removeDiscussionReq);

        return new VedantuResponse(removeDissRes);
    }

    @Override
    public VedantuResponse getdiscussionInfo(GetDiscussionReq getDiscussionReq) {
        if (getDiscussionReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetDiscussionRes getDissRes = getDiscussionInfo(getDiscussionReq);

        return new VedantuResponse(getDissRes);
    }

    protected static void addSocialActionInfo(String userId, String entityOwnerId,
                                              Map<String, Boolean> entityVoteMap, Map<String, FollowType> followTypeMap,
                                              ISocialEntity entity) {

        entity._setVoted(entityVoteMap != null && entityVoteMap.get(entity._getEntityId()) != null && entityVoteMap
                .get(entity._getEntityId()).booleanValue());
        FollowType followType = StringUtils.equals(userId, entityOwnerId) ? FollowType.OWNER
                : (followTypeMap != null && followTypeMap.get(entity._getEntityId()) != null ? followTypeMap
                .get(entity._getEntityId()) : FollowType.NONE);
        entity._setFollowType(followType);
    }

    @Override
    public VedantuResponse getdiscussions(GetDiscussionsReq getDiscussionsReq) {
        if (getDiscussionsReq.resultType == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "please proved resultType");
        }
        ListResponse<GetDiscussionRes> getDissListRes = getDiscussions(getDiscussionsReq);
        return new VedantuResponse(getDissListRes);
    }

    @Override
    public VedantuResponse fixdiscussions(GetDiscussionReq getDiscussionReq) {
        if (getDiscussionReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetDiscussionRes getDissListRes = fixDiscussions(getDiscussionReq);
        return new VedantuResponse(getDissListRes);
    }

    @Override
    public VedantuResponse recordteacherResponse(RecordTeacherResponseReq recordTeacherResponseReq) {
        if (recordTeacherResponseReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);

        }
        RecordTeacherResponseRes recordTeacherResponseRes = recordTeacherResponse(recordTeacherResponseReq);
        return new VedantuResponse(recordTeacherResponseRes);
    }

    @Override
    public VedantuResponse getSimilardiscussions(GetSimilarEntities getSimilarEntities) {
        if (getSimilarEntities == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        ListResponse<GetDiscussionRes> getDissListRes = getSimilarDiscussions(getSimilarEntities);
        return new VedantuResponse(getDissListRes);
    }

    private GetDiscussionRes getDiscussionInfo(GetDiscussionReq getDiscussionReq) {

        Discussion diss = discussionRepo.findByIdAndRecordState(getDiscussionReq.getId(), VedantuRecordState.ACTIVE);

        if (diss == null) {
            throw new VedantuException(VedantuErrorCode.DISCUSSION_NOT_FOUND);
        }
        GetDiscussionRes getDissRes = new GetDiscussionRes();
        getDissRes.fromMongoModel(diss);
        return (GetDiscussionRes) annotateExtraInfo(getDiscussionReq.userId, diss.contentSrc != null
                        && diss.contentSrc.type == EntityType.ORGANIZATION ? diss.contentSrc.id : null,
                EntityType.DISCUSSION, getDissRes);
    }

    public  RemoveDiscussionRes removeDiscussion(RemoveDiscussionReq removeDissReq)
            throws VedantuException {

        Optional<Discussion> diss1 = discussionRepo.findById(removeDissReq.getId());
        if(!diss1.isPresent())
            throw new VedantuException(VedantuErrorCode.DISCUSSION_NOT_FOUND);
        Discussion diss=diss1.get();
        OrgMember orgMember = getMemberByUserId(removeDissReq.orgId,
                removeDissReq.userId);

        if (StringUtils.equals(removeDissReq.userId, diss.userId)
                || (orgMember != null && diss.contentSrc != null
                && checkIfOrgIdAllowedForRemoval(diss, orgMember)
                && orgMember.profile.isAllowedInstContentRemoval())) {
            RemoveDiscussionRes removeDissRes = new RemoveDiscussionRes();
            removeDissRes.deleted = delete(removeDissReq.userId, EventType.INDEX_DISCUSSION,
                    new SrcEntity(EntityType.DISCUSSION, removeDissReq.id));
            return removeDissRes;
        } else {

            String errorMsg = "removeDiscussion action not allowed for userId:"
                    + removeDissReq.userId + " for discussion[" + removeDissReq.id + "] ";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }
    }

    public  AddDiscussionRes addDiscussion(AddDiscussionReq addDissReq)
            throws VedantuException {

        validateBoardIds(addDissReq._getAllBoardIds());
        try {
            addDissReq.removeImageSrc(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Discussion diss = addDiscussion(addDissReq.userId, addDissReq.name,
                addDissReq.content, addDissReq.brdIds, addDissReq.targetIds, addDissReq.tags,
                Scope.ORG, addDissReq.contentSrc);

        AddDiscussionRes addDiscussionRes = new AddDiscussionRes();
        addDiscussionRes.fromMongoModel(diss);

        //questionComponent.generateEventAysc(addDissReq.userId, diss, UserActionType.EventActionType.ADD, EventType.INDEX_DISCUSSION, UserActionType.ADDED, true);

        DoubtTransaction doubtTransaction = new DoubtTransaction(diss._getStringId());
        doubtTransactionRepo.save(doubtTransaction);
        DoubtsProcessingDetails details = new DoubtsProcessingDetails(diss._getStringId());
        generateEventAysc(addDissReq.userId, details, EventType.PROCESS_DOUBTS);
        generateEventAysc(addDissReq.userId, details, EventType.PROCESS_DOUBTS, System.currentTimeMillis() + DOUBT_EXHAUSTION_TIME_MILLIS);

        return (AddDiscussionRes) annotateExtraInfo(addDissReq.userId, diss.contentSrc != null
                        && diss.contentSrc.type == EntityType.ORGANIZATION ? diss.contentSrc.id : null,
                EntityType.DISCUSSION, addDiscussionRes);

    }

    public Discussion addDiscussion(String userId, String name, String content,
                                    List<String> brdIds, List<String> targetIds, List<String> tags,
                                    Scope scope, SrcEntity contentSrc) throws VedantuException {
        Discussion diss = new Discussion(name, content, userId);
        diss.scope = scope;
        diss.contentSrc = contentSrc;
        diss.addBoards(brdIds);
        diss.addTargets(targetIds);
        diss.addTags(tags);
        logger.debug("saving discussion : " + diss);
        discussionRepo.save(diss);

        return diss;
    }
    protected  void validateBoardIds(Set<String> allBordIds) throws VedantuException {
      /*  ObjectIdUtils.hasInvalidId();
        if (CollectionUtils.isNotEmpty(allBordIds)) {
            int count = (int) BoardDAO.INSTANCE.count(new BasicDBObject(ConstantsGlobal._ID,
                    new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils.toObjectIds(
                            new ArrayList<String>(allBordIds), true).toArray())));
            if (count != allBordIds.size()) {
                logger.error("some boards ids from the list [" + allBordIds + "] are not valid");
                throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
            }
        }*/
    }
    public OrgMember getMemberByUserId(String orgId, String userId) {
        logger.debug("getMemberByUserId orgId: " + orgId + ", userId: " + userId);

        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);


        if (orgMember == null) {
            logger.error("cannot find orgMember for orgId: " + orgId + ", userId: " + userId);
        }
        return orgMember;
    }

    private boolean checkIfOrgIdAllowedForRemoval(Discussion diss, OrgMember orgMember) {
        // Only Learnpedia Admin or Admin of the institute to which doubt belongs; can delete it.
        return StringUtils.equals(orgMember.orgId, diss.contentSrc.id) ||
                StringUtils.equals(orgMember.orgId, learnPediaid);
    }

    protected boolean delete(final String userId, EventType indexEventType, SrcEntity content)
            throws VedantuException {


        Optional<Discussion> discussion = discussionRepo.findById(content.id);

        if (!discussion.isPresent()) {
            return false;
        }
        discussion.get().recordState = VedantuRecordState.DELETED;
        discussionRepo.save(discussion.get());
        //TODO implement generateEventAsynch
       /* NewsRemoveDetails newsRemoveDetails = new NewsRemoveDetails();
        newsRemoveDetails.content = content;
        AbstractVedantuEventManager.generateEventAysc(userId, newsRemoveDetails,
                EventType.REMOVE_NEWS);*/
        return true;
    }

    public  AbstractBoardSearchEntityTagDetails annotateExtraInfo(String userId,
                                                                        String orgId, EntityType entityType, AbstractBoardSearchEntityTagDetails entity) {

        List<? extends AbstractBoardSearchEntityTagDetails> entities = Arrays.asList(entity);
        annotateExtraInfo(userId, orgId, entityType, entities);
        return entities.get(0);
    }
    public  void annotateExtraInfo(String userId, String orgId, EntityType entityType,
                                         List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(HardCodedConstants.emptyString, userId, orgId, entityType, entities, false);
    }

    public  void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
                                         List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(secId, userId, orgId, entityType, entities, false);
    }

    public  void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
                                         List<? extends AbstractBoardSearchEntityTagDetails> entities,
                                         boolean excludeOrgMappingInfo) {

        Set<String> brdIds = new HashSet<String>();

        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            userIds.add(details.userId);
            entityIds.add(details.id);
            brdIds.addAll(details._getBoardsIds());
        }
        Map<String, BoardBasicInfo> boardsInfoMap = getInfosMap(brdIds);
        // trying to get user details without orgId
        Map<String, ModelBasicInfo> userInfos = commentServiceImpl.getUserInfoMap(null, userIds,
                excludeOrgMappingInfo);

        Map<String, Boolean> entityVoteMap = null;//EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(
        //userId, entityIds);

        Map<String, Boolean> entityAttemptMap = null;
        Map<String, Map<String, Long>> entityStartEndTime = null;
        if (entityType == EntityType.QUESTION || entityType == EntityType.TEST) {
            entityAttemptMap = getEntityAttemptsMap(entityIds, userId);
        }
        if(!secId.isEmpty() && entityType == EntityType.TEST){
            entityStartEndTime = getEntityStartEndTime(secId, entityIds);
        }
        Map<String, FollowType> followTypeMap = commentServiceImpl.getEntityFollowTypeMap(userId, entityType, entityIds);

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            logger.debug("user info : " + userInfos.get(details.userId) + ", userId: "
                    + details.userId);
            details.boardTree = details.fetchBoardTree(boardsInfoMap);
            logger.debug("details boardTree info : " + details.boardTree);
            details.boards = null;
            if(!secId.isEmpty() && entityType == EntityType.TEST){
                details.startTime = entityStartEndTime.get(details.id).get("startTime");
                details.endTime = entityStartEndTime.get(details.id).get("endTime");
                details.closeTime = entityStartEndTime.get(details.id).get("closeTime");
            }
            if (details instanceof IAttemptableEntity) {
                addAttemptInfo(userId, entityAttemptMap, (IAttemptableEntity) details);
            }
            if (details instanceof ISocialEntity) {
                addSocialActionInfo(userId, details.userId, entityVoteMap, followTypeMap,
                        (ISocialEntity) details);
            }
            if (details instanceof IReverseImageMapperProcessor) {
                ((IReverseImageMapperProcessor) details).addImageSrcUrl();
            }
            details.user = userInfos.get(details.userId);
        }
    }
    public  Map<String, BoardBasicInfo> getInfosMap(Set<String> brdIds) {

        return contentServiceImpl.getBasicInfosByIds(brdIds);
    }
    public  Map<String, Boolean> getEntityAttemptsMap(Set<String> entityIds, String userId) {

        Map<String, Boolean> entityAttemptsMap = new HashMap<String, Boolean>();
        if (userId.isEmpty() || CollectionUtils.isEmpty(entityIds)) {
            logger.error("empty entityIds : " + entityIds);
            return entityAttemptsMap;
        }
        List<UserEntityAttempt> results= userEntityAttemptRepo.findByIdInAndUserId(entityIds.toArray(),userId);

        for (UserEntityAttempt entityAttempt : results) {
            entityAttemptsMap.put(entityAttempt.entity.id, Boolean.valueOf(true));
        }
        logger.debug("returning attempts entity map : " + entityAttemptsMap);
        return entityAttemptsMap;
    }
    public Map<String, Map<String, Long>> getEntityStartEndTime(String secId, Set<String> entityIds) {
        // TODO Auto-generated method stub
        Map<String, Map<String, Long>> entityStartTimeMap = new HashMap<String, Map<String, Long>>();
        if (secId.isEmpty()|| CollectionUtils.isEmpty(entityIds)) {
            logger.error("empty secId or entityIds : ");
            return entityStartTimeMap;
        }
        List<LibraryContentLink> results=libraryContentLinksRepo.findBySourceIdInAndTargetIdAndRecordState(entityIds.toArray(),secId,VedantuRecordState.ACTIVE.name());

        for (LibraryContentLink entityStartTime : results) {
            Map<String, Long> time = new HashMap<String, Long>();
            if(entityStartTime.getSchedule() != null){
                time.put("startTime", entityStartTime.getSchedule().startTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().startTime.getTime());
                time.put("endTime", entityStartTime.getSchedule().endTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().endTime.getTime());
                time.put("closeTime", entityStartTime.getSchedule().closeTime == null ? Long.MIN_VALUE: entityStartTime.getSchedule().closeTime.getTime());
            } else {
                time.put("startTime", Long.MIN_VALUE);
                time.put("endTime", Long.MIN_VALUE);
                time.put("closeTime", Long.MIN_VALUE);
            }
            entityStartTimeMap.put(entityStartTime.source.id, time);
        }
        logger.debug("returning attempts entity map : " + entityStartTimeMap);
        return entityStartTimeMap;
    }

    public GetDiscussionRes fixDiscussions(GetDiscussionReq getDissReq) {
        GetDiscussionRes res = new GetDiscussionRes();
        try {
            Discussion doubt = getDiscussion(getDissReq.id);
            OrgMember orgMember = orgMemberRepo.findByUserId(doubt.getUserId());
            String orgId = orgMember.getOrgId();
            if (!doubt.contentSrc.id.equals(orgId)) {
                doubt.contentSrc.id = orgId;
                discussionRepo.save(doubt);
                //TODO:generate AsynchREq
                //  generateEventAysc(doubt.userId, doubt, EventActionType.UPDATE, EventType.INDEX_DISCUSSION, UserActionType.UPDATED, false);
                res.voted = true;
            } else {
                res.voted = false;
            }
        } catch (VedantuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    /*protected void addAttemptInfo(String userId, Map<String, Boolean> entityAttemptMap,
                                  IAttemptableEntity entity) {

        entity._setAttempted(entityAttemptMap != null
                && entityAttemptMap.get(entity._getEntityId()) != null && entityAttemptMap.get(
                entity._getEntityId()).booleanValue());
    }*/

    public ListResponse<GetDiscussionRes> getDiscussions(GetDiscussionsReq getDissReq) {

        ListResponse<GetDiscussionRes> results = getEntityInfos(getDissReq, EntityType.DISCUSSION,
                GetDiscussionRes.class, null);
        annotateExtraInfo(getDissReq.userId, getDissReq.orgId, EntityType.DISCUSSION, results.list);
        return results;
    }

    public Discussion getDiscussion(String id) throws VedantuException {
        Optional<Discussion> diss = discussionRepo.findById(id);
        if (!diss.isPresent()) {
            throw new VedantuException(VedantuErrorCode.DISCUSSION_NOT_FOUND);
        }
        return diss.get();
    }

    public RecordTeacherResponseRes recordTeacherResponse(RecordTeacherResponseReq request) {
        DoubtTransaction doubtTransaction = doubtTransactionRepo.findByDiscussionId(request.discussionId);

        DoubtAssignmentDetails doubtDetails = doubtTransaction
                .getDoubtAssignmentDetailsForTeacherId(request.teacherId);

        DoubtsProcessingDetails details = new DoubtsProcessingDetails();
        details.discussionId = request.discussionId;

        RecordTeacherResponseRes response = new RecordTeacherResponseRes();

        switch (request.response) {
            case ACCEPTED:
                if (!doubtTransaction.state.equals(AcceptanceState.ACCEPTED)) {
                    doubtTransaction.acceptedBy.add(request.teacherId);
                    doubtTransaction.state = DoubtState.ACCEPTED;
                    doubtDetails.state = AcceptanceState.ACCEPTED;
                    doubtTransactionRepo.save(doubtTransaction);

                    TeacherAnalytics teacher = teacherAnalyticsRepo.findByTeacherOrgMemberId(request.teacherId);
                    teacher.lastAcceptedTime = System.currentTimeMillis();
                    teacherAnalyticsRepo.save(teacher);

                    long processTime = System.currentTimeMillis() + DOUBT_MAX_SOLUTION_TIME_MILLIS;
                    //TODO:need to generate EventSynch
                    // generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS, processTime);
                } else {
                    response.success = false;
                    return response;
                }
                break;
            case REJECTED:
                doubtTransaction.rejectedBy.add(request.teacherId);
                doubtTransaction.state = DoubtState.REJECTED;
                doubtDetails.state = AcceptanceState.REJECTED;
                doubtTransactionRepo.save(doubtTransaction);
                //TODO:need generate Email
                //generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS);
                break;
            case MAYBE_LATER:
                doubtTransaction.mayBeLater.add(request.teacherId);
                doubtTransaction.state = DoubtState.UNASSIGNED;
                doubtDetails.state = AcceptanceState.MAYBE_LATER;
                doubtTransactionRepo.save(doubtTransaction);
                long processTime = doubtDetails.timeAssignedAt + DOUBT_MAX_RESPONSE_TIME_MILLIS;
                //TODO:need to generate event
                //generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS,processTime);
                break;
            default:
                break;
        }
        response.success = true;
        return response;
    }

    public ListResponse<GetDiscussionRes> getSimilarDiscussions(GetSimilarEntities getDissReq) {

        ListResponse<GetDiscussionRes> results = getSimilarEntityInfos(getDissReq,
                GetDiscussionRes.class, null);
        //   annotateExtraInfo(getDissReq.userId, getDissReq.orgId, EntityType.DISCUSSION, results.list);
        return results;
    }

    public <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq searchReq, EntityType entityType, Class<T> respObj,
            Set<String> returnedEntityIds) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        challengesService.getBoardsTagFacets(searchReq.size, query, criteria);
        challengesService.buildSearchQuery(searchReq, entityType, query, criteria);
        buildSearchFilter(searchReq, entityType, query, criteria);
        if (!org.springframework.util.CollectionUtils.isEmpty(searchReq.includeModes)) {
            criteria.and(ConstantsGlobal.MODE).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeModes).toArray());

        }
       /* if (!CollectionUtils.isEmpty(searchReq.includeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeTypes).toArray());

        }*/
        if (!org.springframework.util.CollectionUtils.isEmpty(searchReq.includeDifficulty)) {
            criteria.and(ConstantsGlobal.DIFFICULTY).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeDifficulty).toArray());

        }
        if (!org.springframework.util.CollectionUtils.isEmpty(searchReq.scope)) {
            criteria.and(ConstantsGlobal.SCPOE).in(searchReq.scope.toArray());

        }
        query.addCriteria(criteria);
       /* return getEntityInfos(searchReq.orderBy, searchReq.sortOrder, searchReq.start,
                searchReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);*/
        SearchListResponse<T> listResponse = new SearchListResponse<T>();
        if (entityType == EntityType.QUESTION) {
            List<Question> questions = mongoTemplate.find(query, Question.class);
            listResponse.totalHits = questions.size();
            logger.debug("totalHits: " + listResponse.totalHits);
            List<GetQuestionRes> resList = questions.stream().map(temp -> {
                GetQuestionRes res = new GetQuestionRes();
                res.setScope(temp.getScope());
                res.setContent(temp.getContent());
                res.setAttempts(temp.getAttempts());
                return res;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) resList);

        } else if (entityType == EntityType.ASSIGNMENT) {
            List<Assignment> assignments = mongoTemplate.find(query, Assignment.class);
            List<GetAssignmentInfoRes> assignmentInfoResList = assignments.stream().map(assignment -> {
                GetAssignmentInfoRes assignmentInfoRes = new GetAssignmentInfoRes();
                assignmentInfoRes.setId(assignment._getStringId());
                assignmentInfoRes.setAttempts(assignment.getAttempts());
                assignmentInfoRes.setDesc(assignment.getDesc());
                assignmentInfoRes.setCode(assignment.getCode());
                assignmentInfoRes.setComments(assignment.getComments());
                assignmentInfoRes.setRecordState(assignment.getRecordState());
                return assignmentInfoRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = assignments.size();
            listResponse.list.addAll((Collection<? extends T>) assignmentInfoResList);
        } else if (entityType == EntityType.VIDEO) {
            List<Video> videos = mongoTemplate.find(query, Video.class);
            List<GetVideoRes> getVideoResList = videos.stream().map(video -> {
                GetVideoRes getVideoRes = new GetVideoRes();
                getVideoRes.setId(video._getStringId());
                getVideoRes.setComments(video.getComments());
                getVideoRes.setAvgRating(video.getAverage());
                getVideoRes.setDifficulty(video.getDifficulty());
                getVideoRes.setDescription(video.getDescription());
                getVideoRes.setOriginalFileName(video.getOriginalFileName());
                getVideoRes.setUserId(video._getUserId());
                getVideoRes.setTimeCreated(video.getTimeCreated());
                getVideoRes.setThumbnail(video.getThumbnail());
                getVideoRes.setLinkType(video.getLinkType());
                getVideoRes.setExtension(video.getExtension());
                return getVideoRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = videos.size();
            listResponse.list.addAll((Collection<? extends T>) getVideoResList);
        } else if (entityType == EntityType.DOCUMENT) {
            List<Documents> documents = mongoTemplate.find(query, Documents.class);
            List<GetDocumentRes> getDocumentResList = documents.stream().map(video -> {
                GetDocumentRes getDocumentRes = new GetDocumentRes();
                return getDocumentRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = documents.size();
            listResponse.list.addAll((Collection<? extends T>) getDocumentResList);
        } else if (entityType == EntityType.FILE) {
            List<Files> files = mongoTemplate.find(query, Files.class);
            List<GetFileRes> getFileResList = files.stream().map(file -> {
                GetFileRes getFileRes = new GetFileRes();
                getFileRes.setAvgRating(file.average);
                getFileRes.setCmdsFileId(file.getCMDSFileId());
                getFileRes.setDifficulty(file.getDifficulty());
                getFileRes.setDescription(file.getDescription());
                getFileRes.setId(file._getStringId());
                return getFileRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = files.size();
            listResponse.list.addAll((Collection<? extends T>) getFileResList);
        } else if (entityType == EntityType.DISCUSSION) {
            List<Discussion> discussions = mongoTemplate.find(query, Discussion.class);
            List<GetDocumentRes> getDocumentResList = discussions.stream().map(video -> {
                GetDocumentRes getDocumentRes = new GetDocumentRes();
                return getDocumentRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = discussions.size();
            listResponse.list.addAll((Collection<? extends T>) getDocumentResList);
        }

        return listResponse;

    }


}

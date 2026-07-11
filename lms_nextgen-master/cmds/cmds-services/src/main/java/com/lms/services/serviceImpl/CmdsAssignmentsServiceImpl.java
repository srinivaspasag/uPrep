package com.lms.services.serviceImpl;

import com.lms.board.model.Board;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.*;
import com.lms.component.CMDSLibraryManager;
import com.lms.component.CMDSModuleManager;
import com.lms.components.CMDSResourcesManager;
import com.lms.enums.CmdsContentLinkType;
import com.lms.enums.EnumBasket;
import com.lms.enums.TestMode;
import com.lms.enums.TestResultVisibility;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSAssignment;
import com.lms.models.CMDSFolder;
import com.lms.models.CMDSQuestion;
import com.lms.models.event.search.details.ReIndexDetails;
import com.lms.pojos.*;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.FinishCMDSTestEditRes;
import com.lms.pojos.responce.GetCMDSAssignmentsRes;
import com.lms.pojos.responses.CreateCMDSTestRes;
import com.lms.pojos.responses.GetCMDSAssignmentRes;
import com.lms.pojos.responses.GetCMDSTestQuestionsRes;
import com.lms.pojos.responses.ModifyCMDSAssignmentQuestionsRes;
import com.lms.pojos.search.details.BoardSearchEntity;
import com.lms.pojos.tests.BoardQus;
import com.lms.repo.CMDSAssignmentRepo;
import com.lms.repo.CMDSFolderRepo;
import com.lms.repository.CMDSQuestionRepo;
import com.lms.services.CmdsAssignmentsService;
import com.mongodb.DuplicateKeyException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class CmdsAssignmentsServiceImpl extends AbstractContentManager implements CmdsAssignmentsService {

    private static final Logger logger = LoggerFactory.getLogger(CmdsAssignmentsServiceImpl.class);
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private CMDSFolderRepo cmdsFolderRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CMDSResourcesManager cmdsResourcesManager;
    @Autowired
    private CMDSModuleManager cmdsModuleManager;
    @Autowired
    private CMDSAssignmentRepo cmdsAssignmentRepo;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;
    @Autowired
    private CMDSLibraryManager cmdsLibraryManager;


    @Override
    public VedantuResponse createAssignment(CreateCMDSTestReq req) {
        if (StringUtils.isEmpty(req.orgId)) {
            logger.error("missing orgId: " + req.orgId);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing orgId: "
                    + req.orgId);
        }
        // validate test metadata
        List<TestMetadata> metadata = req.metadata;
        Set<String> brdIds = new HashSet<String>();
        for (TestMetadata mdata : metadata) {
            logger.debug(" Metadata " + mdata);
            if (StringUtils.isEmpty(mdata.id) || StringUtils.isEmpty(mdata.name)) {
                String errorMsg = "invalid metadata [name:" + mdata.name + ", id:" + mdata.id + "]";
                logger.error(errorMsg);
                throw new VedantuException(VedantuErrorCode.INVALID_METADATA, errorMsg);
            }
            if (mdata.qIds == null) {
                mdata.qIds = new ArrayList<String>();
            }
            brdIds.add(mdata.id);
            if (mdata.children != null) {
                for (BoardQus topic : mdata.children) {
                    brdIds.add(topic.id);
                    if (topic.qIds == null) {
                        topic.qIds = new ArrayList<String>();
                    }
                }
            }
            if (mdata.details != null) {
                for (TestDetails detail : mdata.details) {
                    if (detail.qIds == null) {
                        detail.qIds = new ArrayList<String>();
                    }
                }
            }
        }

        int availableBoardsCount = (int) count(brdIds);
        if (availableBoardsCount != brdIds.size()) {
            logger.error("some boardIds provided in metadata object are not valid brdIds: "
                    + brdIds);
            throw new VedantuException(VedantuErrorCode.INVALID_METADATA);
        }
        CMDSFolder folder = StringUtils.isEmpty(req.folderId)
                || ObjectIdUtils.hasInvalidId(req.folderId) ? cmdsResourcesManager.getRootFolder(
                req.userId, req.orgId) : cmdsFolderRepo.findByIdAndOrganizationId(req.folderId, req.orgId);

        CMDSAssignment cmdsAssignment = addAssignment(req.userId,
                req.name, req.code, req.type, req.targetId, req.desc, req.metadata, req.duration,
                new SrcEntity(EntityType.ORGANIZATION, req.orgId), Scope.ORG,
                req.getResultVisibility());
        CreateCMDSTestRes createCmdsAssignmentRes = new CreateCMDSTestRes();
        createCmdsAssignmentRes.id = cmdsAssignment._getStringId();
        logger.info("generating index cmds assignment event ");
        generateEventAysc(req.userId, cmdsAssignment, UserActionType.EventActionType.ADD,
                EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.ADDED, false);

        SrcEntity cmdsAssignmentEntity = new SrcEntity(EntityType.CMDSASSIGNMENT,
                cmdsAssignment._getStringId());

        String parentESId = cmdsModuleManager.addAsCMDSResource(cmdsAssignmentEntity,
                UserActionType.EventActionType.ADD, cmdsAssignment);

        // add this test to the root folder
        cmdsResourcesManager.addToFolder(req.orgId, req.userId, cmdsAssignmentEntity,
                folder._getStringId(), CmdsContentLinkType.ADDED, parentESId);

        return new VedantuResponse(createCmdsAssignmentRes);
    }

    @Override
    public VedantuResponse addQuestion(ModifyCMDSAssignmentQuestionsReq req) {

        return new VedantuResponse(modifyTestQuestions(req, false));
    }

    @Override
    public VedantuResponse removeQuestion(ModifyCMDSAssignmentQuestionsReq req) {
        return new VedantuResponse(modifyTestQuestions(req, true));

    }

    @Override
    public VedantuResponse getAssignmentInfos(GetCMDSTestReq req)
    {
        CMDSAssignment assignment = getAssignment(req.id);
        GetCMDSAssignmentRes getAssignmentRes = new GetCMDSAssignmentRes();
        getAssignmentRes.fromMongoModel(assignment);
        getUserInfoMap(req.orgId, Arrays.asList(assignment.userId));
        getAssignmentRes.user = getUserInfoMap(req.orgId, Arrays.asList(assignment.userId)).get(
                assignment.userId);
        return new VedantuResponse(getAssignmentRes);

    }

    @Override
    public VedantuResponse getAssignmentQuestions(GetCMDSAssignmentQuestionsReq req) {
        GetCMDSTestQuestionsRes questions = new GetCMDSTestQuestionsRes();

        CMDSAssignment assignment = getAssignment(req.assignmentId);

        List<String> qIds = assignment.__getAllQIds(req.brdId);
        if (CollectionUtils.isEmpty(qIds)) {
            logger.error("no question found for brdId: " + req.brdId);
            return new VedantuResponse(questions);
        }
        Map<String, CMDSQuestionInfo> questionsMap = toBasicInfosMap(cmdsAssignmentRepo.findAllById(ObjectIdUtils.toObjectIds(qIds,
                        true)));
        logger.debug("cmds question map : " + questionsMap);
        questions.totalHits = qIds.size();
        for (String qid : qIds) {
            questions.list.add(questionsMap.get(qid));
        }
        return new VedantuResponse(questions);
    }

    @Override
    public VedantuResponse finishAssignmentEditing(FinishCMDSAssignmentEditReq req) {
        CMDSAssignment cmdsAssignment = cmdsAssignmentRepo.findById(req.assignmentId).get();
        cmdsAssignment._finishEditing();
        cmdsAssignment.completed =cmdsModuleManager.isReadyToPublished(cmdsAssignment);
        cmdsAssignmentRepo.save(cmdsAssignment);
        generateEventAysc(cmdsAssignment.userId, cmdsAssignment, UserActionType.EventActionType.UPDATE,
                EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);

        ReIndexDetails details = new ReIndexDetails();
        details.type = EntityType.CMDSASSIGNMENT;
        details.userId = req.userId;
        details.ids = Arrays.asList(new String[] { req.assignmentId });
        generateEventAysc(req.userId, details, EventType.REINDEX_CMDS_RESOURCE);

        FinishCMDSTestEditRes res = new FinishCMDSTestEditRes(cmdsAssignment._getStringId(), true);
        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getAssignments(GetTestsReq request) {

        GetCMDSAssignmentsRes response = new GetCMDSAssignmentsRes();
        AtomicLong totalHits = new AtomicLong(0L);
        List<CMDSAssignment> assignments = getCMDSAssignments(
                request.query, request.orgId, request.includeTypes, request.excludeTypes,
                request.start, request.size, request.published, totalHits);
        if (CollectionUtils.isNotEmpty(assignments)) {
            for (CMDSAssignment assignment : assignments) {

                int programsAddedTo = cmdsLibraryManager.getAllProgramsAddedTo(new SrcEntity(
                                EntityType.CMDSASSIGNMENT, assignment._getStringId()),
                        CmdsContentLinkType.ADDED);
                CMDSAssignmentBasicInfo assignmentInfo = new CMDSAssignmentBasicInfo(
                        assignment._getStringId(), assignment.name,
                        assignment.contentSrc != null ? assignment.contentSrc.id : null,
                        assignment.timeCreated, assignment.lastUpdated, assignment.userId,
                        programsAddedTo, assignment.published, assignment.completed,
                        assignment.globalId, assignment.recordState);
                assignmentInfo.subType = assignment.type.name();

                response.list.add(assignmentInfo);

            }
            response.totalHits = totalHits.longValue();
        }
        return new VedantuResponse(response);
    }
    public List<CMDSAssignment> getCMDSAssignments(String query, String orgId,
                                                   List<String> includeTypes, List<String> excludeTypes, int start, int size,
                                                   boolean published, AtomicLong totalHits) {

        Criteria criteria=new Criteria();
        Query query1=new Query();
        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        logger.debug("orgId :" + orgId);
        criteria.and("contentSrc.id").is(orgId);
        criteria.and("scope").is(Scope.ORG);
        if (CollectionUtils.isNotEmpty(includeTypes)) {
            criteria.and("type").in(includeTypes);
        }
        if (CollectionUtils.isNotEmpty(excludeTypes)) {
            criteria.and("type").not().in(excludeTypes);
        }
        criteria.and("published").in(published);
        if (!StringUtils.isEmpty(query)) {
            criteria.and("name").is(query);
        }
        logger.debug(query1.toString());
        query1.addCriteria(criteria);
        List<CMDSAssignment> cmdsAssignments= mongoTemplate.find(query1,CMDSAssignment.class);
        totalHits.set(cmdsAssignments.size());

        return cmdsAssignments;

    }

    public Map<String, CMDSQuestionInfo> toBasicInfosMap(Collection<CMDSAssignment> results) {

        Map<String, CMDSQuestionInfo> infosMap = new LinkedHashMap<String, CMDSQuestionInfo>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (CMDSAssignment t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (CMDSQuestionInfo) t.toBasicInfo());
            }
        }
        return infosMap;
    }


    private  ModifyCMDSAssignmentQuestionsRes modifyTestQuestions(
            ModifyCMDSAssignmentQuestionsReq req, boolean remove) throws VedantuException {

        CMDSAssignment assignment = getAssignment(req.assignmentId);

        if (assignment.published || assignment.scope == Scope.PUBLIC) {
            String errorMsg = "test[" + req.assignmentId
                    + "] can not be edited as it's being published or shared with some users";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED, errorMsg);
        }
        CMDSQuestion cmdsquestion = cmdsQuestionRepo.findById(req.qId).get();
        CMDSQuestionSearchIndexDetails question = new CMDSQuestionSearchIndexDetails();
        question.fromMongoModel(cmdsquestion);
        if (!(req.userId).equals(question.userId) && question.scope == Scope.PRIVATE) {
            logger.error("question [id:" + req.qId + "] with Scope[" + question.scope
                    + "] only visible to user[" + question.userId + "] as owner");
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        com.lms.pojos.search.details.BoardSearchEntity courseBoard = question.__getBoard(BoardType.COURSE);
        if (courseBoard == null) {
            String errorMsg = "no course is being tagged on question [" + req.qId + "]";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND, errorMsg);
        }
        logger.info("testMetadata : " + assignment.metadata);
        TestMetadata metadata = assignment.__getTestMetadata(courseBoard.id);
        if (metadata == null) {
            logger.error("metadata for [brdId:" + courseBoard.id + ",type: " + courseBoard.type
                    + "] for testId [" + req.assignmentId + "] not found");
            throw new VedantuException(VedantuErrorCode.METADATA_NOT_FOUND);
        }

        boolean updated = false;
        List<BoardSearchEntity> topicBoards = question.__getBoards(BoardType.TOPIC);

        try {
            logger.info("child board for question : " + topicBoards);
            boolean addOnlyToBoard = false;
            for (BoardSearchEntity topicBoard : topicBoards) {
                updated = remove ? metadata.removeQuestion(question.id, question.type, topicBoard,
                        req.assignmentId, assignment.type) : metadata.addQuestion(question.id,
                        question.type, topicBoard, req.assignmentId, assignment.type,
                        addOnlyToBoard);
                addOnlyToBoard = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.QUESTION_MAX_COUNT_EXCEED);
        }

        ModifyCMDSAssignmentQuestionsRes res = new ModifyCMDSAssignmentQuestionsRes(
                courseBoard.name, courseBoard.id, courseBoard.type, question.id, question.type);
        res.success = updated;
        if (!topicBoards.isEmpty()) {
            res.child = topicBoards.get(0);
        }
        if (res.success) {
            assignment.completed = cmdsModuleManager.isReadyToPublished(assignment);
            cmdsAssignmentRepo.save(assignment);
            generateEventAysc(assignment.userId, assignment, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);
        }
        logger.info("returning modify question res : " + res + ", remove:" + remove);
        return res;
    }


    public CMDSAssignment addAssignment(String userId, String name, String code, EnumBasket.TestType type,
                                        String targetId, String desc, List<TestMetadata> metadata, long duration,
                                        SrcEntity contentSrc, Scope scope, TestResultVisibility resultVisibility)
            throws VedantuException {

        CMDSAssignment assignment = null;
        try {
            if (isContentByContentSrcAndCodeExists(contentSrc, code)) {
                throw new VedantuException(VedantuErrorCode.ASSIGNMENT_ALREAY_PRESENT,
                        "a assignment with code:" + code + " for contentSrc:" + contentSrc
                                + " already present");
            }
            assignment = new CMDSAssignment(userId, name, desc, duration, metadata, type,
                    TestMode.ONLINE, code, scope, targetId, resultVisibility);
            assignment.contentSrc = contentSrc;
            assignment.computeTotalQusAndMarks();
            /* Added by Shivank */
            assignment.completed = cmdsModuleManager.isReadyToPublished(assignment);
            /* Added by Shivank */
            cmdsAssignmentRepo.save(assignment);
        } catch (DuplicateKeyException e) {
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_ALREAY_PRESENT,
                    "a assignment with code:" + code + " for contentSrc:" + contentSrc
                            + " already present", e);
        }
        return assignment;
    }

    public boolean isContentByContentSrcAndCodeExists(SrcEntity contentSrc, String code) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        code = code.trim().toLowerCase();
        criteria.orOperator(Criteria.where(ConstantsGlobal.CODE).is(code), Criteria.where(ConstantsGlobal.CODE).is(code.toUpperCase()));
        criteria.and("contentSrc.id").is(contentSrc.id);
        criteria.and("contentSrc.type").is(contentSrc.type);
        query.addCriteria(criteria);
        CMDSAssignment model = mongoTemplate.findOne(query, CMDSAssignment.class);

        return model != null;
    }

    public long count(Set<String> brdIds) {
        List<Board> boards = boardRepo.findByIdIn(ObjectIdUtils
                .toObjectIds(new ArrayList<String>(brdIds)));
        int count = boards.size();
        return count;

    }

    public GetCMDSAssignmentRes getAssignmentInfo(GetCMDSTestReq req)
            throws VedantuException {

        CMDSAssignment assignment = getAssignment(req.id);
        GetCMDSAssignmentRes getAssignmentRes = new GetCMDSAssignmentRes();
        getAssignmentRes.fromMongoModel(assignment);
        getUserInfoMap(req.orgId, Arrays.asList(assignment.userId));
        getAssignmentRes.user = getUserInfoMap(req.orgId, Arrays.asList(assignment.userId)).get(
                assignment.userId);
        return getAssignmentRes;
    }

    public CMDSAssignment getAssignment(String id) throws VedantuException {

        CMDSAssignment assignment = cmdsAssignmentRepo.findById(id).get();
        if (assignment == null) {
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND,
                    "no assignment found with id:" + id);
        }
        return assignment;
    }

}

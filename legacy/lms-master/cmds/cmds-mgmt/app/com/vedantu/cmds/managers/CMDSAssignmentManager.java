package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.cmds.daos.CMDSAssignmentDAO;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.cmds.models.CMDSFolder;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails;
import com.vedantu.cmds.models.event.search.details.ReIndexDetails;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.tests.CMDSAssignmentBasicInfo;
import com.vedantu.cmds.pojos.content.tests.GetCMDSAssignmentQuestionsReq;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.FinishCMDSAssignmentEditReq;
import com.vedantu.cmds.pojos.requests.tests.GetCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.ModifyCMDSAssignmentQuestionsReq;
import com.vedantu.cmds.pojos.responses.tests.CreateCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.FinishCMDSTestEditRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSAssignmentRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSAssignmentsRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.ModifyCMDSAssignmentQuestionsRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.managers.AssignmentManager;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;
import com.vedantu.content.pojos.tests.BoardQus;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.search.details.boards.BoardSearchEntity;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class CMDSAssignmentManager extends AbstractCMDSContentManager {

    private static final ALogger        LOGGER   = Logger.of(CMDSAssignmentManager.class);
    public static CMDSAssignmentManager INSTANCE = new CMDSAssignmentManager();

    public static CreateCMDSTestRes createAssignment(CreateCMDSTestReq req) throws VedantuException {

        if (StringUtils.isEmpty(req.orgId)) {
            LOGGER.error("missing orgId: " + req.orgId);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing orgId: "
                    + req.orgId);
        }
        // validate test metadata
        List<TestMetadata> metadata = req.metadata;
        Set<String> brdIds = new HashSet<String>();
        for (TestMetadata mdata : metadata) {
            LOGGER.debug(" Metadata " + mdata);
            if (StringUtils.isEmpty(mdata.id) || StringUtils.isEmpty(mdata.name)) {
                String errorMsg = "invalid metadata [name:" + mdata.name + ", id:" + mdata.id + "]";
                LOGGER.error(errorMsg);
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

        int availableBoardsCount = (int) BoardDAO.INSTANCE.count(new BasicDBObject(
                ConstantsGlobal._ID, new BasicDBObject(MongoManager.IN_QUERY, ObjectIdUtils
                        .toObjectIds(new ArrayList<String>(brdIds), true).toArray())));
        if (availableBoardsCount != brdIds.size()) {
            LOGGER.error("some boardIds provided in metadata object are not valid brdIds: "
                    + brdIds);
            throw new VedantuException(VedantuErrorCode.INVALID_METADATA);
        }
        CMDSFolder folder = StringUtils.isEmpty(req.folderId)
                || ObjectIdUtils.hasInvalidId(req.folderId) ? CMDSResourcesManager.getRootFolder(
                req.userId, req.orgId) : CMDSFolderDAO.INSTANCE.findById(req.orgId, req.folderId);

        CMDSAssignment cmdsAssignment = CMDSAssignmentDAO.INSTANCE.addAssignment(req.userId,
                req.name, req.code, req.type, req.targetId, req.desc, req.metadata, req.duration,
                new SrcEntity(EntityType.ORGANIZATION, req.orgId), Scope.ORG,
                req.getResultVisibility());
        CreateCMDSTestRes createCmdsAssignmentRes = new CreateCMDSTestRes();
        createCmdsAssignmentRes.id = cmdsAssignment._getStringId();
        LOGGER.info("generating index cmds assignment event ");
        generateEventAysc(req.userId, cmdsAssignment, EventActionType.ADD,
                EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.ADDED, false);

        SrcEntity cmdsAssignmentEntity = new SrcEntity(EntityType.CMDSASSIGNMENT,
                cmdsAssignment._getStringId());

        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(cmdsAssignmentEntity,
                EventActionType.ADD, cmdsAssignment);

        // add this test to the root folder
        CMDSResourcesManager.addToFolder(req.orgId, req.userId, cmdsAssignmentEntity,
                folder._getStringId(), CmdsContentLinkType.ADDED, parentESId);

        return createCmdsAssignmentRes;
    }

    public static GetCMDSAssignmentRes getAssignmentInfo(GetCMDSTestReq req)
            throws VedantuException {

        CMDSAssignment assignment = CMDSAssignmentDAO.INSTANCE.getAssignment(req.id);
        GetCMDSAssignmentRes getAssignmentRes = new GetCMDSAssignmentRes();
        getAssignmentRes.fromMongoModel(assignment);
        getUserInfoMap(req.orgId, Arrays.asList(assignment.userId));
        getAssignmentRes.user = getUserInfoMap(req.orgId, Arrays.asList(assignment.userId)).get(
                assignment.userId);
        return getAssignmentRes;
    }

    public static GetCMDSTestQuestionsRes getAssignmentQuestions(GetCMDSAssignmentQuestionsReq req)
            throws VedantuException {

        GetCMDSTestQuestionsRes questions = new GetCMDSTestQuestionsRes();

        CMDSAssignment assignment = CMDSAssignmentDAO.INSTANCE.getAssignment(req.assignmentId);

        List<String> qIds = assignment.__getAllQIds(req.brdId);
        if (CollectionUtils.isEmpty(qIds)) {
            LOGGER.error("no question found for brdId: " + req.brdId);
            return questions;
        }
        Map<String, CMDSQuestionInfo> questionsMap = CMDSQuestionDAO.INSTANCE
                .toBasicInfosMap(CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(qIds,
                        true)));
        LOGGER.debug("cmds question map : " + questionsMap);
        questions.totalHits = qIds.size();
        for (String qid : qIds) {
            questions.list.add(questionsMap.get(qid));
        }
        return questions;
    }

    public static FinishCMDSTestEditRes finishAssignmentEditing(FinishCMDSAssignmentEditReq req)
            throws VedantuException {

        CMDSAssignment cmdsAssignment = CMDSAssignmentDAO.INSTANCE.getById(req.assignmentId);
        cmdsAssignment._finishEditing();
        cmdsAssignment.completed = CMDSAssignmentDAO.INSTANCE.isReadyToPublished(cmdsAssignment);
        CMDSAssignmentDAO.INSTANCE.save(cmdsAssignment);
        generateEventAysc(cmdsAssignment.userId, cmdsAssignment, EventActionType.UPDATE,
                EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);

        ReIndexDetails details = new ReIndexDetails();
        details.type = EntityType.CMDSASSIGNMENT;
        details.userId = req.userId;
        details.ids = Arrays.asList(new String[] { req.assignmentId });
        generateEventAysc(req.userId, details, EventType.REINDEX_CMDS_RESOURCE);

        FinishCMDSTestEditRes res = new FinishCMDSTestEditRes(cmdsAssignment._getStringId(), true);
        return res;
    }

    /**
     *
     * @param assignmentId
     * @param qid
     * @param assignmentId
     *            --> {@linkplain brdId is the id of courseId(i.e id of Physics)}
     * @param childBrdId
     *            {@linkplain childBrdId is the id of topic this question belongs too}
     */
    public static ModifyCMDSAssignmentQuestionsRes
            addQuestion(ModifyCMDSAssignmentQuestionsReq req) throws VedantuException {

        return modifyTestQuestions(req, false);
    }

    public static ModifyCMDSAssignmentQuestionsRes removeQuestion(
            ModifyCMDSAssignmentQuestionsReq req) throws VedantuException {

        return modifyTestQuestions(req, true);
    }

    private static ModifyCMDSAssignmentQuestionsRes modifyTestQuestions(
            ModifyCMDSAssignmentQuestionsReq req, boolean remove) throws VedantuException {

        CMDSAssignment assignment = CMDSAssignmentDAO.INSTANCE.getAssignment(req.assignmentId);

        if (assignment.published || assignment.scope == Scope.PUBLIC) {
            String errorMsg = "test[" + req.assignmentId
                    + "] can not be edited as it's being published or shared with some users";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED, errorMsg);
        }
        CMDSQuestion cmdsquestion = CMDSQuestionDAO.INSTANCE.getQuestionById(req.qId);
        CMDSQuestionSearchIndexDetails question = new CMDSQuestionSearchIndexDetails();
        question.fromMongoModel(cmdsquestion);
        if (!StringUtils.equals(req.userId, question.userId) && question.scope == Scope.PRIVATE) {
            LOGGER.error("question [id:" + req.qId + "] with Scope[" + question.scope
                    + "] only visible to user[" + question.userId + "] as owner");
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        BoardSearchEntity courseBoard = question.__getBoard(BoardType.COURSE);
        if (courseBoard == null) {
            String errorMsg = "no course is being tagged on question [" + req.qId + "]";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND, errorMsg);
        }
        LOGGER.info("testMetadata : " + assignment.metadata);
        TestMetadata metadata = assignment.__getTestMetadata(courseBoard.id);
        if (metadata == null) {
            LOGGER.error("metadata for [brdId:" + courseBoard.id + ",type: " + courseBoard.type
                    + "] for testId [" + req.assignmentId + "] not found");
            throw new VedantuException(VedantuErrorCode.METADATA_NOT_FOUND);
        }

        boolean updated = false;
        List<BoardSearchEntity> topicBoards = question.__getBoards(BoardType.TOPIC);

        try {
            LOGGER.info("child board for question : " + topicBoards);
            boolean addOnlyToBoard = false;
            for (BoardSearchEntity topicBoard : topicBoards) {
                updated = remove ? metadata.removeQuestion(question.id, question.type, topicBoard,
                        req.assignmentId, assignment.type) : metadata.addQuestion(question.id,
                        question.type, topicBoard, req.assignmentId, assignment.type,
                        addOnlyToBoard);
                addOnlyToBoard = true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.QUESTION_MAX_COUNT_EXCEED);
        }

        ModifyCMDSAssignmentQuestionsRes res = new ModifyCMDSAssignmentQuestionsRes(
                courseBoard.name, courseBoard.id, courseBoard.type, question.id, question.type);
        res.success = updated;
        if (!topicBoards.isEmpty()) {
            res.child = topicBoards.get(0);
        }
        if (res.success) {
            assignment.completed = CMDSAssignmentDAO.INSTANCE.isReadyToPublished(assignment);
            CMDSAssignmentDAO.INSTANCE.save(assignment);
            generateEventAysc(assignment.userId, assignment, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);
        }
        LOGGER.info("returning modify question res : " + res + ", remove:" + remove);
        return res;
    }

    public static GetCMDSAssignmentsRes getAssignments(GetTestsReq request) throws VedantuException {

        GetCMDSAssignmentsRes response = new GetCMDSAssignmentsRes();
        MutableLong totalHits = new MutableLong(0L);
        List<CMDSAssignment> assignments = CMDSAssignmentDAO.INSTANCE.getCMDSAssignments(
                request.query, request.orgId, request.includeTypes, request.excludeTypes,
                request.start, request.size, request.published, totalHits);
        if (CollectionUtils.isNotEmpty(assignments)) {
            for (CMDSAssignment assignment : assignments) {

                int programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(new SrcEntity(
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
        return response;
    }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        CMDSAssignment content = CMDSAssignmentDAO.INSTANCE.getById(request.entity.id);

        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(AbstractContentModel.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.desc = request.description;
            updateList.add(AbstractTestCommonModel.DESC);

        }

        boolean updated = true;
        if (content.globalId != null) {
            request.entity = new SrcEntity(EntityType.ASSIGNMENT, content.globalId);
            updated = AssignmentManager.INSTANCE.update(request);
        }

        if (updated) {
            CMDSAssignmentDAO.INSTANCE.updateModel(content, updateList);

            generateEventAysc(request.userId, content, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_ASSIGNMENT, UserActionType.UPDATED, false);

            AbstractCMDSContentManager.addAsCMDSResource(request.entity, EventActionType.UPDATE,
                    content);
        }

        return true;
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSAssignment> assignments = new ArrayList<CMDSAssignment>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSAssignment question = CMDSAssignmentDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            assignments.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSAssignment) {
                    assignments.add((CMDSAssignment) content);
                }
            }
        }

        // calculate question image size;

        for (CMDSAssignment assignment : assignments) {
            if( assignment.size.isFinalized() && !recalculate){
                continue;
            }
            assignment.size.reset();
            LOGGER.debug(" question ids" + assignment.__getAllQIds());
            List<CMDSQuestion> questions = CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils
                    .toObjectIds(assignment.__getAllQIds()));
            if(CollectionUtils.isEmpty(questions)){
                continue;
            }
            for (CMDSQuestion question : questions) {
                if (!question.size.isFinalized()) {
                    CMDSQuestionManager.INSTANCE.calculate(null,true, question);
                }
                assignment.size.add(question.size);
            }
            assignment.size.finalize();
            CMDSAssignmentDAO.INSTANCE.updateModel(assignment, Arrays.asList(CMDSAssignment.SIZE));
            if (assignment.globalId != null) {
                AssignmentManager.INSTANCE.calculate(assignment.globalId,true );
            }
        }
        return true;
    }

}

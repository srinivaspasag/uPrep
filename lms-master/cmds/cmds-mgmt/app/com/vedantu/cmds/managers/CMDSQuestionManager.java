package com.vedantu.cmds.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.json.JSONException;

import play.Logger;
import play.Play;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.daos.BoardMappingsDAO;
import com.vedantu.board.models.BoardMapping;
import com.vedantu.board.pojos.BoardMappings;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.mgmt.interfaces.IContainable;
import com.vedantu.cmds.mgmt.publishers.QuestionPublisher;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.question.HintFormat;
import com.vedantu.cmds.pojos.content.question.HintInfo;
import com.vedantu.cmds.pojos.content.question.QuestionFormat;
import com.vedantu.cmds.pojos.content.question.SolutionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.NumericSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.TextSolutionInfo;
import com.vedantu.cmds.pojos.requests.DeleteMappingReq;
import com.vedantu.cmds.pojos.requests.GetUsageReq;
import com.vedantu.cmds.pojos.requests.questions.AddQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.AddSolutionReq;
import com.vedantu.cmds.pojos.requests.questions.EditQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.GetCMDSQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.PublishQuestionAsChallengeReq;
import com.vedantu.cmds.pojos.responses.GetMultiUsageRes;
import com.vedantu.cmds.pojos.responses.GetUsageRes;
import com.vedantu.cmds.pojos.responses.ShareMappingResponse;
import com.vedantu.cmds.pojos.responses.questions.AddQuestionRes;
import com.vedantu.cmds.pojos.responses.questions.AddSolutionRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionSearchRes;
import com.vedantu.cmds.pojos.responses.questions.GetSolutionsRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.enums.EntityEditType;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.managers.ChallengeManager;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.content.pojos.AttachmentInfo;
import com.vedantu.content.pojos.SrcEntityEdit;
import com.vedantu.content.pojos.SrcEntityPublishableState;
import com.vedantu.content.pojos.requests.challenges.AddChallengeReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionsReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.responses.challenges.AddChallengeRes;
import com.vedantu.content.search.details.AbstractBoardSearchEntityTagDetails;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.search.utils.ElasticSearchUtils;

public class CMDSQuestionManager extends AbstractCMDSContentManager {

    private static ALogger            LOGGER     = Logger.of(CMDSQuestionManager.class);

    public static CMDSQuestionManager INSTANCE   = new CMDSQuestionManager();
    private static final Integer      ZERO_VALUE = new Integer(0);
    public String                     tempQuestionDir;

    public CMDSQuestionManager() {

        tempQuestionDir = "questions";
    }

    public String getQuestionUploadDirectory() {

        return tempQuestionDir;
    }

    public GetSolutionsRes getSolutions(GetSolutionsReq req) throws VedantuException {

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(req.qId);
        GetSolutionsRes response = new GetSolutionsRes();
        if (question != null && question.solutionInfo != null) {
            response.solutions = question.solutionInfo.solutions;
        }

        for (SolutionFormat solution : response.solutions) {
            if (solution.attachments != null) {
                List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
                for (Attachment attachment : solution.attachments) {
                    AttachmentInfo attachmentInfo = new AttachmentInfo();
                    attachmentInfo.entity = attachment.entity;
                    // attachmentInfo.info
                    VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(attachment.entity.type);
                    attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                    attachmentsInfo.add(attachmentInfo);
                }
                solution.attachmentsInfo = attachmentsInfo;

            }
        }

        return response;
    }

    public AddQuestionRes addQuestion(AddQuestionReq request) throws VedantuException {

        if (StringUtils.isEmpty(request.status)) {
            request.status = QuestionStatus.INCOMPLETE.name();
        }

        AddQuestionRes addQuestionRes = null;

        CMDSQuestion question = new CMDSQuestion();

        // ----- starting immutable fields addition ------
        // validation code
        question.userId = request.userId;

        // checking orgId
        question.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

        // ------Finsihed mutable entities-------------

        Logger.debug("Received difficulty" + request.difficulty);
        question.difficulty = request.difficulty;

        // validating hints
        question.hints = new HintInfo();
        List<HintFormat> formattedHints = new ArrayList<HintFormat>();

        List<Integer> deductions = new ArrayList<Integer>();
        if (CollectionUtils.isNotEmpty(request.hints)) {
            for (String hintText : request.hints) {
                if (StringUtils.isNotEmpty(hintText)) {
                    HintFormat hintFormat = new HintFormat();
                    hintFormat.newText = hintText;

                    formattedHints.add(hintFormat);
                    deductions.add(ZERO_VALUE);

                } else {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_HINT_FORMAT);
                }
            }

            question.hints.deductions = deductions;
            question.hints.hints = formattedHints;

        }
        // adding type
        if (request.type == QuestionType.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_TYPE);
        }
        question.type = request.type;

        // check if boardIds are provided
        Logger.debug("Re0ceived board ids" + request.brdIds);
        if (CollectionUtils.isNotEmpty(request.brdIds)) {
            question.boardIds = new HashSet<String>();
            question.boardIds.addAll(request.brdIds);
        }
        // check if targetIds
        if (CollectionUtils.isNotEmpty(request.targetIds)) {
            question.targetIds = new HashSet<String>();
            question.targetIds.addAll(request.targetIds);
        }

        SolutionInfo solInfo = null;
        // parse answers
        if (question.type == QuestionType.SCQ) {
            // validations?
            SCQSolutionInfo scqSolInfo = new SCQSolutionInfo();
            scqSolInfo.answer = request.answers.get(0);
            scqSolInfo.optionBody.newOptions = request.options;
            solInfo = scqSolInfo;
        }
        if (question.type == QuestionType.MCQ || question.type == QuestionType.PARA || question.type == QuestionType.MATRIX) {
            LOGGER.debug("Inside CMDSQuestionManager MCQ || PARA || MATRIX type: "+question.type);
            MCQsolutionInfo mcqSolInfo = new MCQsolutionInfo();
            // validations?
            mcqSolInfo.answer = request.answers;
            mcqSolInfo.optionBody.newOptions = request.options;
            solInfo = mcqSolInfo;

        }
        if (question.type == QuestionType.TEXT ) {
            LOGGER.debug("Inside CMDSQuestionManager TEXT type: "+question.type );
            TextSolutionInfo textSolInfo = new TextSolutionInfo();
            textSolInfo.answer = request.answers != null ? request.answers.get(ZERO_VALUE) : null;
            solInfo = textSolInfo;

        }
        if (question.type == QuestionType.SUBJECTIVE) {
            LOGGER.debug("Inside CMDSQuestionManager Subjective type: "+question.type );
            TextSolutionInfo textSolInfo = new TextSolutionInfo();
            textSolInfo.answer = request.answers != null ? request.answers.get(ZERO_VALUE) : null;
            solInfo = textSolInfo;

        }
//        if (question.type == QuestionType.MATRIX) {
//            GridSolutionInfo gridSOLInfo = new GridSolutionInfo();
//            gridSOLInfo.cola = request.columnA;
//            gridSOLInfo.colb = request.columnB;
//            gridSOLInfo.gridAnswer = request.gridAnswer;
//            solInfo = gridSOLInfo;
//        }
        if (question.type == QuestionType.NUMERIC) {
            NumericSolutionInfo numSOLInfo = new NumericSolutionInfo();
            numSOLInfo.answer = request.answers.get(0);
            solInfo = numSOLInfo;
        }

        if (request.solution != null && solInfo != null) {
            if (CollectionUtils.isEmpty(solInfo.solutions)) {
                solInfo.solutions = new ArrayList<SolutionFormat>();
            }
            SolutionFormat solutionFormat = new SolutionFormat();
            solutionFormat.newText = request.solution.content;
            solutionFormat.attachments = request.solution.attachments;
            solInfo.solutions.add(solutionFormat);
        }

        question.solutionInfo = solInfo;

        if (question.questionBody == null && StringUtils.isNotEmpty(request.content)) {
            question.questionBody = new QuestionFormat();
            question.questionBody.newText = request.content;
        }

        if (CollectionUtils.isNotEmpty(request.tags)) {
            question.tags.addAll(request.tags);
        }
        question.status = QuestionStatus.INCOMPLETE;

        if(request.type == QuestionType.PARA){
            question.paragraphId = request.paraId;
        }

        try {

            if (question != null) {
                question.addHook();

            }
            question.removeImageSrc(true);

            // CMDSImageUtil.convertImageUrlToUuidAndSaveImage(question, true, false);
            if(question.type == QuestionType.PARA){
                if(request.folderId == null || request.folderId.isEmpty() || request.folderId.equals("null")){
                    List<String> folderIds = getFolderIds(question.paragraphId);
                    request.folderId = (folderIds.isEmpty() || folderIds == null) ? "" : folderIds.get(0);
                }
            }
            LOGGER.debug("FolderId is: "+request.folderId);
            addQuestion(request.orgId, request.userId, request.folderId, question);
            addQuestionRes = new AddQuestionRes(question._getStringId());
            if(request.type == QuestionType.TEXT){
                addQuestionRes.paraId = question.id + "";
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            if (addQuestionRes == null) {
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
        }

        return addQuestionRes;
    }

    public static CMDSQuestion addQuestion(String orgId, String userId, String folderId,
            CMDSQuestion question) throws VedantuException, Exception {

        /* Added by Shivank */
        question.completed = CMDSQuestionDAO.INSTANCE.isReadyToPublished(question);
        /* Added by Shivank */
        question = CMDSQuestionDAO.INSTANCE.addQuestion(question);
        // Adding question ID of type PARA to list of question type TEXT
        if(question.type == QuestionType.PARA){
            addParaIdToTextQuestion(question);
        }
        SrcEntity questionEntity = new SrcEntity(EntityType.CMDSQUESTION, question._getStringId());

        //if(question.type != QuestionType.PARA){
            generateEventAysc(userId, question, EventActionType.ADD, EventType.INDEX_CMDS_QUESTION,
                    UserActionType.ADDED, true);
        //}

        String parentESId = AbstractCMDSContentManager.addAsCMDSResource(questionEntity,
                EventActionType.ADD, question);
        LOGGER.debug("parentEsID is: "+parentESId);
        if (StringUtils.isEmpty(question.questionSetId)) {
            CMDSResourcesManager.addToFolder(orgId, userId, questionEntity, folderId,
                    CmdsContentLinkType.ADDED, parentESId);
        }
        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        if(orgId.equals(learnpediaId)){
            LOGGER.debug("addQuestion : About share "+question.type+" Question");
            //shareQuestion(question,orgId,userId);
            LOGGER.debug("addQuestion : Shared "+question.type+" Question");
        }
        return question;
    }

    @SuppressWarnings("unused")
    private static void shareQuestion(CMDSQuestion question, String parentOrgId, String parentUserId) throws JSONException, VedantuException {
        // TODO Auto-generated method stub
        List<BoardMapping> sharedOrgMappings = BoardMappingsDAO.INSTANCE.getByParentOrgId(parentOrgId);
        LOGGER.debug("shareQuestion : Number of organisations to share this question is "+sharedOrgMappings.size());
        String parentCourseId = getQuestionCourseId(question,parentOrgId);
        if(parentCourseId == null){
            LOGGER.debug("shareQuestion : NO Board Id Found to Share");
            return;
        }
        for(BoardMapping sharedOrgMapping : sharedOrgMappings){
            LOGGER.debug("shareQuestion : Sharing to Organisation "+sharedOrgMapping.sharedToOrgId);
            List<BoardMappings> boardmappings = sharedOrgMapping.boardMappings;
            LOGGER.debug("shareQuestion : This organisation has board mappings of length "+boardmappings.size());
            for(BoardMappings board : boardmappings){
                if(board.boardType.equalsIgnoreCase("course") && board.parentBoardId.equals(parentCourseId) && board.status){
                    LOGGER.debug("shareQuestion : Found board mapping with type COURSE and parent course Id "+parentCourseId+" and whose status is TRUE");
                    DeleteMappingReq req = new DeleteMappingReq();
                    if(question.type == QuestionType.PARA){
                        req.addNewPara = true;
                    }
                    req.reSync = true;
                    req.parentOrgId = sharedOrgMapping.parentOrgId;
                    req.sharedToOrgId = sharedOrgMapping.sharedToOrgId;
                    req.parentBoardId = board.parentBoardId;
                    req.sharedToBoardId = board.sharedToBoardId;
                    LOGGER.debug("shareQuestion : Before calling shareBoardMapping");
                    List<ShareMappingResponse> res = CMDSResourcesManager.shareBoardMapping(req);
                    LOGGER.debug("shareQuestion : After calling shareBoardMapping");
                    LOGGER.debug("shareQuestion : Number of jobs created W.R.T parentOrgId "+req.parentOrgId+", sharedToOrgId "+req.sharedToOrgId
                            +", parentBoardId "+req.parentBoardId+" and sharedToBoardId "+req.sharedToBoardId+" is "+res.size());
                }
            }
        }
    }

    private static String getQuestionCourseId(CMDSQuestion question, String orgId) {
        List<String> parentCourseBoards = BoardDAO.INSTANCE.getAllCoursesIds(orgId);
        Set<String> boardIds = question.boardIds;
        for(String boardId: boardIds){
            if(parentCourseBoards.contains(boardId)){
                return boardId;
            }
        }
        return null;
    }

    private static void addParaIdToTextQuestion(CMDSQuestion question) {
        try {
            CMDSQuestion textQuestion = CMDSQuestionDAO.INSTANCE.getQuestionById(question.paragraphId);
            List<String> paraIds = textQuestion.paraIds;
            LOGGER.debug("ParaIds are " + paraIds.toString());
            paraIds.add(String.valueOf(question.id));
            textQuestion.paraIds = paraIds;
            CMDSQuestionDAO.INSTANCE.save(textQuestion);
        } catch (VedantuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public AddSolutionRes addSolution(AddSolutionReq request) throws VedantuException {

        AddSolutionRes response = new AddSolutionRes();
        response.isUpdated = addSolution(request.userId, request.questionId, request.solution,
                request.delete, request.newSolution, request.edit, request.attachments);
        if (response.isUpdated && request.edit) {
            response.updatedSolution = request.newSolution;
        } else {
            response.updatedSolution = StringUtils.EMPTY;
        }
        return response;

    }

    public boolean addSolution(String userId, String questionId, String solution, boolean delete,
            String newSolution, boolean edit, List<Attachment> attachments) throws VedantuException {

        if (edit && StringUtils.isEmpty(newSolution)) {
            LOGGER.error("empty target solution");
            throw new VedantuException(VedantuErrorCode.NEW_SOLUTION_IS_MISSING);
        }

        CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getQuestionById(questionId);
        if (question == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
        }
        boolean isUpdated = false;

        if (delete || edit) {

            if (question.solutionInfo != null) {
                SolutionInfo solutionInfo = question.solutionInfo;
                List<SolutionFormat> solutions = solutionInfo.solutions;
                if (CollectionUtils.isNotEmpty(solutions)) {

                    for (int i = 0; i < solutions.size(); i++) {
                        if (StringUtils.equalsIgnoreCase(solutions.get(i).newText, solution)) {
                            isUpdated = true;
                            if (delete) {
                                solutions.remove(i);

                            } else if (edit) {
                                SolutionFormat sf = solutions.get(i);
                                sf.originalText = newSolution;
                                sf.newText = newSolution;
                                sf.attachments = attachments;
                            }
                            break;
                        }
                    }// Looking solution to be deleted or updated

                }// solutions are not empty
            }

        } else {
            if (StringUtils.isEmpty(solution)) {
                LOGGER.info("empty solution!");
                throw new VedantuException(VedantuErrorCode.SOLUTION_EMPTY);
            }
            SolutionInfo solutionInfo = question.solutionInfo;
            List<SolutionFormat> solutions = solutionInfo.solutions;
            if (solutions == null) {
                solutions = new ArrayList<SolutionFormat>();
            }
            SolutionFormat solutionFormat = new SolutionFormat();
            solutionFormat.originalText = solution;
            solutionFormat.newText = solution;
            solutionFormat.attachments = attachments;
            solutions.add(solutionFormat);
            isUpdated = true;
        }
        if (isUpdated) {
            LOGGER.info("empty solution!");
            CMDSQuestionDAO.INSTANCE.save(question);
            return true;
        }
        return false;

    }

    // /**
    // * Publishing has some prerequisites 1) Solution can not be empty 2) Options are specified in
    // * correct way based on type
    // *
    // * @param request
    // * @return
    // * @throws VedantuException
    // */
    // @Deprecated
    // public PublishQuestionRes publishCMDSQuestion(PublishQuestionReq request)
    // throws VedantuException {
    //
    // PublishQuestionRes response = new PublishQuestionRes();
    // for (SrcEntity questionEntity : request.entities) {
    // try {
    // if (questionEntity.type == EntityType.CMDSQUESTION) {
    // Logger.debug("Publishing question" + questionEntity.id);
    // CMDSQuestion cmdsQuestion = CMDSQuestionDAO.INSTANCE
    // .getQuestionById(questionEntity.id);
    // QuestionPublisher.INSTANCE.publish(request.userId, request.orgId, cmdsQuestion);
    // if (StringUtils.isNotEmpty(cmdsQuestion.questionSetId)) {
    // CMDSQuestionSet cmdsQuestionSet = CMDSQuestionSetDAO.INSTANCE
    // .getById(cmdsQuestion.questionSetId);
    //
    // if (cmdsQuestionSet != null) {
    //
    // if (CollectionUtils.isNotEmpty(cmdsQuestionSet.questionIds)) {
    // cmdsQuestionSet.numberOfQuestionsComplete++;
    // if (cmdsQuestionSet.numberOfQuestionsComplete == cmdsQuestionSet.questionIds
    // .size()) {
    // cmdsQuestionSet.status = QuestionStatus.COMPLETE;
    // }
    // }
    // }
    // }
    // }
    // } catch (VedantuException exception) {
    // response.info.put(questionEntity.id, exception.errorCode.name());
    // }
    // }
    // return response;
    // }

    public GetCMDSQuestionRes getQuestion(GetCMDSQuestionReq request) throws VedantuException {

        GetCMDSQuestionRes response = new GetCMDSQuestionRes();

        response.info = (CMDSQuestionInfo) CMDSQuestionDAO.INSTANCE.getBasicInfo(request.id);
        if (response.info == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
        }

        if (response.info.detail != null && response.info.detail.solutionInfo != null
                && CollectionUtils.isNotEmpty(response.info.detail.solutionInfo.solutions)) {
            for (SolutionFormat solution : response.info.detail.solutionInfo.solutions) {
                if (solution.attachments != null) {
                    List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
                    for (Attachment attachment : solution.attachments) {
                        AttachmentInfo attachmentInfo = new AttachmentInfo();
                        attachmentInfo.entity = attachment.entity;
                        // attachmentInfo.info
                        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE
                                .get(attachment.entity.type);
                        attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                        attachmentsInfo.add(attachmentInfo);
                    }
                    solution.attachmentsInfo = attachmentsInfo;

                }

            }
        }
        return response;

    }

    public List<SrcEntityPublishableState> getAssociatedContent(String globalQid) {

        LOGGER.info(".............Entered Update function.............." + globalQid);
        List<SrcEntityPublishableState> entitiesPublishableState = new ArrayList<SrcEntityPublishableState>();
        if (globalQid != null) {
            entitiesPublishableState = CMDSQuestionDAO.INSTANCE.getAssociatedContent(globalQid);
        }
        LOGGER.info(".............Exited Update function..............");
        return entitiesPublishableState;
    }

    private List<String> getFolderIds(String globalQid) {

        LOGGER.info(".............Entered getFolderIds function.............." + globalQid);
        List<String> folderIds = new ArrayList<String>();
        if (globalQid != null) {
            folderIds = CMDSQuestionDAO.INSTANCE.getFolderIds(globalQid);
        }
        LOGGER.info(".............Exited getFolderIds function..............");
        return folderIds;
    }

    private void createCopy(CMDSQuestion questionOriginal, CMDSQuestion question) {

        question.boardIds = questionOriginal.boardIds;
        question.completed = questionOriginal.completed;
        question.contentSrc = questionOriginal.contentSrc;
        question.globalQid = questionOriginal.globalQid;
        question.hints = questionOriginal.hints;
        question.difficulty = questionOriginal.difficulty;
        question.name = questionOriginal.name;
        question.targetIds = questionOriginal.targetIds;
        question.lastUpdated = System.currentTimeMillis();
        question.origRefNo = questionOriginal.origRefNo;
        question.published = questionOriginal.published;

        /*
         * Shankar: publishedBy,publishedOn not needed as the question is not published
         */
        // question.publishedBy = questionOriginal.publishedBy;
        // question.publishedOn = questionOriginal.publishedOn;

        question.questionBody = questionOriginal.questionBody;
        question.questionSetId = questionOriginal.questionSetId;
        question.questionSetName = questionOriginal.questionSetName;
        question.recordState = questionOriginal.recordState;
        question.scope = questionOriginal.scope;
        question.solutionInfo = questionOriginal.solutionInfo;
        question.source = questionOriginal.source;
        question.status = questionOriginal.status;
        question.tags = questionOriginal.tags;
        question.timeCreated = System.currentTimeMillis();
        question.type = questionOriginal.type;
        question.userId = questionOriginal.userId;
    }

    public EditContentRes update(EditQuestionReq request) throws VedantuException {

        LOGGER.info(".............Entered Manager Update function" + request.solution + "...");
        CMDSQuestion questionOriginal = CMDSQuestionDAO.INSTANCE.getById(request.entity.id);
        CMDSQuestion question = new CMDSQuestion();
        boolean updateAll = CollectionUtils.isEmpty(request.updateList);
        if (questionOriginal == null) {
            LOGGER.error("question[" + request.entity.id + "] not found");
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
        }
        List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(questionOriginal
                ._getStringId());
        Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                associatedContentsList);

        Set<SrcEntityEdit> editEntities = new HashSet<SrcEntityEdit>(
                request.editEntities == null ? new ArrayList<SrcEntityEdit>()
                        : request.editEntities);

        EditContentRes questionUpdateRes = new EditContentRes();
        questionUpdateRes.id = questionOriginal._getStringId();
        questionUpdateRes.contentLists = associatedContentsList;
        questionUpdateRes.isUpdated = true;

        LOGGER.debug("..........AssociatedContentsList.........." + associatedContentsList);
        LOGGER.debug("..........AssociatedContents.........." + associatedContents);
        LOGGER.info("AssociatedContentCount" + editEntities);

        if (associatedContents.size() > 0 && editEntities.size() == 0) {
            LOGGER.info(".............Both the collections are different..............");
            questionUpdateRes.isUpdated = false;
        }
        //
        else

        {
            LOGGER.info(".............Both the collections are same or null..............");

            boolean createCopy = false;
            boolean updateRightAway = false;

            for (SrcEntityEdit editEntity : editEntities) {
                LOGGER.info("............Inside for loop..............");
                if (editEntity.editType == EntityEditType.CREATE_COPY) {
                    LOGGER.info("............Create copy..............");
                    createCopy = true;
                }
                if (editEntity.editType == EntityEditType.RIGHT_AWAY) {
                    LOGGER.info("............Update right away..............");
                    updateRightAway = true;
                }
            }

            if (createCopy) {
                createCopy(questionOriginal, question);
            } else {
                question = questionOriginal;
            }

            // if (question.published) {
            //     LOGGER.debug("question [id: " + request.entity.id
            //             + " has been attempted multiple times!");
            //     throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
            // }

            LOGGER.debug("Received difficulty" + request.difficulty);
            // question.difficulty = request.difficulty;

            // validating hints

            List<Integer> deductions = new ArrayList<Integer>();

            if (updateAll || request.updateList.contains("hints")) {
                LOGGER.info(".............Checking for hints..............");
                if (request.hints == null) {
                    question.hints = new HintInfo();
                } else {
                    question.hints = new HintInfo();
                    List<HintFormat> formattedHints = new ArrayList<HintFormat>();
                    for (String hintText : request.hints) {
                        if (StringUtils.isNotEmpty(hintText)) {
                            HintFormat hintFormat = new HintFormat();
                            hintFormat.newText = hintText;

                            formattedHints.add(hintFormat);
                            deductions.add(ZERO_VALUE);

                        } else {
                            throw new VedantuException(VedantuErrorCode.INCORRECT_HINT_FORMAT);
                        }
                    }

                    question.hints.deductions = deductions;
                    question.hints.hints = formattedHints;
                }
            }
            // adding type
            if(question.published == false){
                if (updateAll || request.updateList.contains("type")) {
                    if (request.type != QuestionType.UNKNOWN) {
                        question.type = request.type;
                    }
                }
            }


            // check if boardIds are provided
            Logger.debug("Received board ids" + request.brdIds);
            if(question.published ==  false){
                if (updateAll || request.updateList.contains("brdIds")) {

                    // if (updateRightAway == true) {
                    //     LOGGER.debug(".....before error.......");
                    //     throw new VedantuException(VedantuErrorCode.QUESTION_SUBJECT_CANNOT_BE_EDITED,
                    //             "Question subject cannot be edited because it is bounded to test");
                    // }
                    LOGGER.debug(".....after error.......");
                    question.boardIds = new HashSet<String>();
                    if (request.brdIds != null) {
                        question.boardIds.addAll(request.brdIds);
                    }
                }
            }
            // check if targetIds
            // TODO: confirm targetIds
            if(question.published == false){
                if (updateAll || request.updateList.contains("brdIds")) {
                    // if (updateRightAway == true) {
                    //     throw new VedantuException(VedantuErrorCode.QUESTION_SUBJECT_CANNOT_BE_EDITED);
                    // }
                    question.targetIds = new HashSet<String>();
                    if (request.targetIds != null) {
                        question.targetIds.addAll(request.targetIds);
                    }
                }
            }

            if (updateAll
                    || request.updateList.contains("answers")) {
                LOGGER.info(".............Checking for answers..............");
                if (question.type == QuestionType.SCQ || question.type == QuestionType.MCQ || question.type == QuestionType.PARA || question.type == QuestionType.MATRIX) {

                    if (CollectionUtils.isEmpty(request.options)) {
                        throw new VedantuException(VedantuErrorCode.OPTIONS_NOT_SPECIFIED);
                    }

                    if (question.type == QuestionType.SCQ) {
                        // validations?

                        if (request.answers != null) {
//                            if(question.published == false){
                                question.solutionInfo = new SCQSolutionInfo();
                                ((SCQSolutionInfo) question.solutionInfo).answer = request.answers.get(0);
//                            }else{
//                                ((SCQSolutionInfo) question.solutionInfo).answer = ((SCQSolutionInfo)questionOriginal.solutionInfo).answer;
//                            }

                            ((SCQSolutionInfo) question.solutionInfo).optionBody.newOptions = request.options;
                        }
                    }

                    if (question.type == QuestionType.MCQ || question.type == QuestionType.PARA || question.type == QuestionType.MATRIX) {


                        if (request.answers != null) {
//                            if(question.published == false){
                                question.solutionInfo = new MCQsolutionInfo();
                                ((MCQsolutionInfo) question.solutionInfo).answer = request.answers;
//                            }else{
//                                ((MCQsolutionInfo) question.solutionInfo).answer = ((MCQsolutionInfo) questionOriginal.solutionInfo).answer;
//                            }

                            ((MCQsolutionInfo) question.solutionInfo).optionBody.newOptions = request.options;
                        }

                    }

                } else if (question.type == QuestionType.NUMERIC) {

                    if (request.answers != null) {
//                        if(question.published == false){
                            question.solutionInfo = new NumericSolutionInfo();
                            ((NumericSolutionInfo) question.solutionInfo).answer = request.answers.get(0);
//                        }else{
//                            ((NumericSolutionInfo) question.solutionInfo).answer = ((NumericSolutionInfo) questionOriginal.solutionInfo).answer;
//                        }
                    }
                } else if (question.type == QuestionType.TEXT) {

                    if (request.answers != null) {
//                        if(question.published == false){
                            question.solutionInfo = new TextSolutionInfo();
                            ((TextSolutionInfo) question.solutionInfo).answer = request.answers.get(0);
//                        }else{
//                            ((TextSolutionInfo) question.solutionInfo).answer = ((TextSolutionInfo) questionOriginal.solutionInfo).answer;
//                        }
                    }
                } else if (question.type == QuestionType.SUBJECTIVE) {

                    if (request.answers != null) {
//                        if(question.published == false){
                            question.solutionInfo = new TextSolutionInfo();
                            ((TextSolutionInfo) question.solutionInfo).answer = request.answers.get(0);
//                        }else{
//                            ((TextSolutionInfo) question.solutionInfo).answer = ((TextSolutionInfo) questionOriginal.solutionInfo).answer;
//                        }
                    }
                }
//                else if (question.type == QuestionType.MATRIX) {
//                    if (question.solutionInfo == null
//                            || !(question.solutionInfo instanceof GridSolutionInfo)) {
//                        question.solutionInfo = new GridSolutionInfo();
//                    }
//
//                    if (updateAll || request.updateList.contains("columnA")) {
//                        ((GridSolutionInfo) question.solutionInfo).cola = request.columnA;
//                    }
//
//                    if (updateAll || request.updateList.contains("columnB")) {
//                        ((GridSolutionInfo) question.solutionInfo).colb = request.columnB;
//                    }
//                    if (updateAll || request.updateList.contains("gridAnswer")) {
//                        ((GridSolutionInfo) question.solutionInfo).gridAnswer = request.gridAnswer;
//                    }
//
//                }

            }// other types where options are not needed

            if (updateAll || request.updateList.contains("solution")) {
                if (question.solutionInfo == null) {
                    throw new VedantuException(VedantuErrorCode.NO_SOLUTIONINFO_PROVIDED);
                }

                if (CollectionUtils.isEmpty(question.solutionInfo.solutions)) {
                    question.solutionInfo.solutions = new ArrayList<SolutionFormat>();
                }

                question.solutionInfo.solutions.clear();
                if (request.solution != null) {
                    SolutionFormat solutionFormat = new SolutionFormat();
                    solutionFormat.newText = request.solution.content;
                    solutionFormat.attachments = request.solution.attachments;
                    question.solutionInfo.solutions.add(solutionFormat);
                }
            }

            if (updateAll || request.updateList.contains("content")) {

                question.questionBody = new QuestionFormat();
                if (request.content == null) {
                    throw new VedantuException(VedantuErrorCode.QUESTION_CANNOT_BE_EDITED);
                }
                question.questionBody.newText = request.content;
            }

            if (updateAll || request.updateList.contains("tags")) {
                LOGGER.info(".............Update list contains tags..............");
                question.tags = new HashSet<String>();
                if (request.tags != null) {
                    question.tags.addAll(request.tags);
                }

            }

            if (updateAll || request.updateList.contains("difficulty")) {
                LOGGER.info(".............Update list contains difficulty..............");
                if (request.difficulty != null) {
                    question.difficulty = request.difficulty;

                }
            }

            if (updateAll || request.updateList.contains("source")) {
                LOGGER.info(".............Update list contains source..............");
                if (request.difficulty != null) {
                    question.source = request.origRefNo;
                }

            }

            question.status = QuestionStatus.INCOMPLETE;

            try {

                if (question != null) {
                    question.addHook();

                }
                question.removeImageSrc(true);
            } catch (IOException e) {
                throw new VedantuException(VedantuErrorCode.QUESTION_CANNOT_BE_EDITED);
            } catch (EntityFileStorageException e) {

                throw new VedantuException(VedantuErrorCode.QUESTION_CANNOT_BE_EDITED);
            }

            LOGGER.info(".............Question about to update..............");

            questionUpdateRes.isUpdated = true;
            question.completed = CMDSQuestionDAO.INSTANCE.isReadyToPublished(question);
            CMDSQuestionDAO.INSTANCE.save(question);
            if (question.published) {
                QuestionPublisher.INSTANCE.publish(request.userId, request.orgId, question);
            }
            AbstractCMDSContentManager.addAsCMDSResource(new SrcEntity(EntityType.CMDSQUESTION,
                    question._getStringId()), EventActionType.UPDATE, question);

            generateEventAysc(request.userId, question, EventActionType.UPDATE,
                    EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
            SrcEntity questionEntity = new SrcEntity(EntityType.CMDSQUESTION,
                    question._getStringId());

            String parentESId = AbstractCMDSContentManager.addAsCMDSResource(questionEntity,
                    EventActionType.ADD, question);

            List<String> folderIds = getFolderIds(question._getStringId());
            LOGGER.debug(".............Displaying folder ids..." + folderIds + "..............");
            for (String folderId : folderIds) {
                LOGGER.debug(".............Adding to folder..." + folderId + "..............");
                CMDSResourcesManager.addToFolder(request.orgId, request.userId, questionEntity,
                        folderId, CmdsContentLinkType.ADDED, parentESId);
            }
            // if (StringUtils.isEmpty(question.questionSetId)) {
            // CMDSResourcesManager.addToFolder(request.orgId, request.userId, questionEntity,
            // question.folderId,
            // CmdsContentLinkType.ADDED, parentESId);
            // }

            LOGGER.info(".............Question Successfully updated..............");
            if(question.scope == Scope.PRIVATE){
                LOGGER.info(".............   Sending Email  (Not Implemented Yet)  .............");
            }

//            if (editEntities != null) {
//                CMDSQuestionSearchIndexDetails questionIndex = new CMDSQuestionSearchIndexDetails();
//                questionIndex.fromMongoModel(question);
//
//                for (SrcEntityEdit editEntity : editEntities) {
//                    // SrcEntityEdit editEntity = editEntities.get(0);
//                    BoardSearchEntity courseBoard = questionIndex.__getBoard(BoardType.COURSE);
//                    AbstractTestCommonModel test = (AbstractTestCommonModel) EntityTypeDAOFactory.INSTANCE
//                            .get(editEntity.entity.type).getById(editEntity.entity.id);
//                    TestMetadata metadata = test.__getTestMetadata(courseBoard.id);
//
//                    // questionUpdateRes
//                    try {
//                        List<BoardSearchEntity> topicBoards = questionIndex
//                                .__getBoards(BoardType.TOPIC);
//                        LOGGER.info("child board for question : " + topicBoards);
//                        boolean addOnlyToBoard = false;
//                        for (BoardSearchEntity topicBoard : topicBoards) {
//                            // BoardSearchEntity topicBoard = topicBoards.get(0);
//                            metadata.removeQuestion(questionOriginal._getStringId(),
//                                    questionOriginal.type, topicBoard, test._getStringId(),
//                                    test.type);
//                            if (editEntity.editType == EntityEditType.RIGHT_AWAY) {
//                                LOGGER.info("..........EditType is right away................");
//                                metadata.addQuestion(question._getStringId(),
//                                        questionOriginal.type, topicBoard, test._getStringId(),
//                                        test.type, addOnlyToBoard);
//                            }
//
//                            else if (editEntity.editType == EntityEditType.CREATE_COPY) {
//                                LOGGER.info("..........EditType is create copy................");
//                                metadata.addQuestion(questionOriginal._getStringId(),
//                                        question.type, topicBoard, test._getStringId(), test.type,
//                                        addOnlyToBoard);
//                            }
//
//                            addOnlyToBoard = true;
//                            // test.completed = CMDSTestDAO.INSTANCE.isReadyToPublished(test);
//
//                            VedantuBasicDAO basicDAO = EntityTypeDAOFactory.INSTANCE
//                                    .get(editEntity.entity.type);
//                            if (basicDAO instanceof CmdsContentDAO) {
//                                test.completed = ((CmdsContentDAO) basicDAO)
//                                        .isReadyToPublished(test);
//                                ((CmdsContentDAO) basicDAO).save(test);// deleteContent(baseModel);
//                            }
//
//                            // CMDSTestDAO.INSTANCE.save(test);
//
//                            // String parentESId =
//                            // AbstractCMDSContentManager.addAsCMDSResource(questionEntity,
//                            // EventActionType.ADD, question);
//                            // if (StringUtils.isEmpty(question.questionSetId)) {
//                            // CMDSResourcesManager.addToFolder(orgId, userId, questionEntity,
//                            // folderId,
//                            // CmdsContentLinkType.ADDED, parentESId);
//                            // }
//
//                        }
//                        generateEventAysc(test.userId, test, EventActionType.UPDATE,
//                                EntityIndexEventMapper.INSTANCE.get(editEntity.entity.type),
//                                UserActionType.UPDATED, false);
//
//                    } catch (Exception e) {
//                        LOGGER.error(e.getMessage(), e);
//                        throw new VedantuException(VedantuErrorCode.QUESTION_MAX_COUNT_EXCEED);
//
//                    }
//                }
//            }

//            String learnpediaId = Play.application().configuration().getString("learnpedia.id");
//            if(request.orgId.equals(learnpediaId)){
//                LOGGER.debug("EDIT : About to edit shared LP Questions");
//                editSharedQuestion(request,question);
//                LOGGER.debug("EDIT : Edited shared LP Questions");
//            }
        }
        return questionUpdateRes;
    }

    @SuppressWarnings("unused")
    private void editSharedQuestion(EditQuestionReq request, CMDSQuestion question) {
        // TODO Auto-generated method stub
        Set<String> sharedQIds = question.sharedCMDSQuesIds;
        for(String qId : sharedQIds){
            CMDSQuestion que = CMDSQuestionDAO.INSTANCE.getById(qId);
            request.entity.id = qId;
            List<SrcEntityEdit> editEntities = new ArrayList<SrcEntityEdit>();
            // Get edit entities
            List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(que
                    ._getStringId());
            Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                    associatedContentsList);
            LOGGER.debug("..........AssociatedContentsList.........." + associatedContentsList);
            LOGGER.debug("..........AssociatedContents.........." + associatedContents);

            for(SrcEntityPublishableState associatedContent : associatedContentsList){
                SrcEntityEdit editEntity = new SrcEntityEdit();
                editEntity.entity = associatedContent.entity;
                editEntity.editType = EntityEditType.RIGHT_AWAY;
                editEntities.add(editEntity);
            }
            LOGGER.info("AssociatedContentCount" + editEntities);
            request.editEntities = editEntities;
            request.orgId = que.contentSrc.id;
            request.userId = que.userId;
            request.brdIds.clear();
            request.brdIds.addAll(que.boardIds);
            request.callingUserId = que.userId;
            request.callingApp = "cmds-app";
            request.callingAppId = "cmds-app";
            try {
                LOGGER.debug("Editing shared LP question "+qId);
                CMDSQuestionManager.INSTANCE.update(request);
                LOGGER.debug("Editing shared LP question "+qId);
            } catch (VedantuException e) {
                LOGGER.debug("Error while Editing shared LP Question "+qId+" with message "+e.getMessage());
            }
        }
    }

    public AddChallengeRes publishCMDSQuestionAsChallenge(PublishQuestionAsChallengeReq request)
            throws VedantuException {

        AddChallengeRes response = new AddChallengeRes();

        for (SrcEntity questionEntity : request.entities) {
            if (questionEntity.type == EntityType.CMDSQUESTION) {
                CMDSQuestion cmdsQuestion = CMDSQuestionDAO.INSTANCE
                        .getQuestionById(questionEntity.id);
                Question globalQuestion = QuestionPublisher.INSTANCE.publish(request.userId,
                        request.orgId, cmdsQuestion);

                AddChallengeReq addChallReq = new AddChallengeReq();
                addChallReq.callingApp = request.callingApp;
                addChallReq.callingAppId = request.callingAppId;
                addChallReq.callingUserId = request.callingUserId;
                addChallReq.channelId = request.channelId;
                addChallReq.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
                addChallReq.difficulty = request.difficulty;
                addChallReq.duration = request.duration;
                List<String> hints = new ArrayList<String>();
                if (cmdsQuestion.hints != null) {
                    for (HintFormat hInfo : cmdsQuestion.hints.hints) {
                        hints.add(StringUtils.isNotEmpty(hInfo.newText) ? hInfo.newText
                                : hInfo.originalText);

                    }
                }
                addChallReq.hints = hints;
                addChallReq.hintsDeduction = request.hintsDeduction;
                addChallReq.initialBidPool = request.initialBidPool;
                addChallReq.lifeTime = request.lifeTime;
                addChallReq.maxBid = request.maxBid;
                addChallReq.name = request.name;
                addChallReq.publishType = Scope.valueOfKey(request.publishType);
                addChallReq.qid = globalQuestion._getStringId();
                addChallReq.scope = request.scope;
                if (globalQuestion.tags != null) {
                    addChallReq.tags = new ArrayList<String>(globalQuestion.tags);
                }
                addChallReq.userId = request.userId;
                AddChallengeRes addChallRes = ChallengeManager.addChallenge(addChallReq);
                return addChallRes;
            }
        }
        return response;
    }

    public void annotateExtraInfo(String userId, String orgId,
            List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(userId, orgId, EntityType.CMDSQUESTION, entities);
    }

    public static GetCMDSQuestionSearchRes getQuestions(GetQuestionsReq getQuestionsReq)
            throws VedantuException {

        LOGGER.debug("......inside getQuestions.........");
        GetCMDSQuestionSearchRes response = new GetCMDSQuestionSearchRes();

        boolean showParaCount = false;
        boolean showNonParaCount = false;
        List<String> types = new ArrayList<String>();
        List<String> difficulty = new ArrayList<String>();
        List<String> scope = new ArrayList<String>();
        SearchListResponse<CMDSQuestionSearchIndexDetails> results = null ;
        if(CollectionUtils.isEmpty(getQuestionsReq.includeDifficulty)){
            difficulty.add("EASY");
            difficulty.add("MODERATE");
            difficulty.add("TOUGH");
            getQuestionsReq.includeDifficulty = difficulty;
        }
        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        if(!getQuestionsReq.orgId.equals(learnpediaId)){
            if(!CMDSResourcesManager.includeLearnpediaQuestions(learnpediaId,getQuestionsReq.orgId)){
                scope.add("org");
                getQuestionsReq.scope = scope;
            }
        }
        if(getQuestionsReq.quesType.equals("PARA_QUES")){
            results = getParagraphQuestionsInfo(getQuestionsReq);
            showParaCount = true;
            showNonParaCount = true;
        }else{
            if (getQuestionsReq.quesType.equals("NOT_PARA")) {
                if(CollectionUtils.isEmpty(getQuestionsReq.includeTypes)){
                    types.add("SCQ");
                    types.add("MCQ");
                    types.add("NUMERIC");
                    types.add("MATRIX");
                    getQuestionsReq.includeTypes = types;
                }
                showParaCount = true;
            } else if (getQuestionsReq.quesType.equals("PARA")) {
                types.add("TEXT");
                getQuestionsReq.includeTypes = types;
                showNonParaCount = true;
            }
            results = getEntityInfos(
                    getQuestionsReq, EntityType.CMDSQUESTION, CMDSQuestionSearchIndexDetails.class,
                    null);
        }

        response.totalHits = results.totalHits;
        if(showParaCount){
            types.clear();
            types.add("TEXT");
            getQuestionsReq.includeTypes = types;
            SearchListResponse<CMDSQuestionSearchIndexDetails> paraResults = getEntityInfos(
                    getQuestionsReq, EntityType.CMDSQUESTION, CMDSQuestionSearchIndexDetails.class,
                    null);
            response.paraHits = paraResults.totalHits;
        }
        if(showNonParaCount){
            types.clear();
            types.add("SCQ");
            types.add("MCQ");
            types.add("NUMERIC");
            types.add("MATRIX");
            getQuestionsReq.includeTypes = types;
            SearchListResponse<CMDSQuestionSearchIndexDetails> nonParaResults = getEntityInfos(
                    getQuestionsReq, EntityType.CMDSQUESTION, CMDSQuestionSearchIndexDetails.class,
                    null);
            response.nonParaHits = nonParaResults.totalHits;
        }

        List<CMDSQuestionInfo> cmdsQuestionInfos = new ArrayList<CMDSQuestionInfo>();
        for (CMDSQuestionSearchIndexDetails details : results.list) {
            LOGGER.debug("......inside outer for loop.........");
            CMDSQuestionInfo questionInfo = ((CMDSQuestionInfo) CMDSQuestionDAO.INSTANCE
                    .getBasicInfo(details.id));

            // questionInfo = (CMDSQuestionInfo) CMDSQuestionDAO.INSTANCE.getBasicInfo(details.id);
            if(questionInfo == null)
                continue;
            if (questionInfo.detail != null && questionInfo.detail.solutionInfo != null
                    && CollectionUtils.isNotEmpty(questionInfo.detail.solutionInfo.solutions)) {
                for (SolutionFormat solution : questionInfo.detail.solutionInfo.solutions) {
                    if (solution.attachments != null) {
                        List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
                        for (Attachment attachment : solution.attachments) {
                            LOGGER.debug("......inside for loop.........");
                            AttachmentInfo attachmentInfo = new AttachmentInfo();
                            attachmentInfo.entity = attachment.entity;
                            // attachmentInfo.info
                            VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE
                                    .get(attachment.entity.type);
                            attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                            attachmentsInfo.add(attachmentInfo);
                        }
                        solution.attachmentsInfo = attachmentsInfo;

                    }

                }
            }
            cmdsQuestionInfos.add(questionInfo);
        }
        response.list.addAll(cmdsQuestionInfos);
        return response;
    }

    @SuppressWarnings("unchecked")
    private static <T extends IListResponseObj> SearchListResponse<T> getParagraphQuestionsInfo(
            GetQuestionsReq getQuestionsReq) {
        LOGGER.debug("Inside getParagraphQuestionsInfo which retrieves para questions of particular paragraph");

        CMDSQuestion paragraphQuestion = new CMDSQuestion();
        try {
            paragraphQuestion = CMDSQuestionDAO.INSTANCE.getQuestionById(getQuestionsReq.paraId);
        } catch (VedantuException e) {
            LOGGER.debug("Exception while retreiveing data : "+e.getMessage());
        }
        List<String> paraQuesIds = paragraphQuestion.paraIds;
        if(paraQuesIds.size() > 0){
            AbstractFacetBuilder[] facets = ElasticSearchUtils.getBoardsTagFacets(getQuestionsReq.size);
            return (SearchListResponse<T>) getBasicInfoOFParaQuestionFromESSearch(paraQuesIds,
                            CMDSQuestionSearchIndexDetails.class,facets);
        }else{
            return new SearchListResponse<T>();
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T extends IListResponseObj> SearchListResponse<T> getBasicInfoOFParaQuestionFromESSearch(
            List<String> qIds,Class<T> respObj,AbstractFacetBuilder[] facets) {

        if (qIds == null || qIds.size() == 0) {
            LOGGER.error("empty search response for getting paragraph questions : ");
            return new SearchListResponse<T>();
        }
        LOGGER.debug("Ready to get Para question for ES resources");
        // ES query
        TermsQueryBuilder query = QueryBuilders.inQuery("content.id",
                qIds.toArray(new String[qIds.size()]));

        SearchResponse response = ElasticSearchUtils.getSearchResponse(query, "timeCreated",
                "desc", 0, qIds.size(), EntityType.CMDSRESOURCE.getIndexName(),
                EntityType.CMDSRESOURCE.getIndexType().toLowerCase(), null, false,
                (AbstractFacetBuilder[]) null);
        if (response == null || response.getHits().getTotalHits() == 0) {
            LOGGER.error("empty search response for ES query : ");
            return new SearchListResponse<T>();
        }
        LOGGER.debug(" Search responses " + response.getHits());
        SearchListResponse<T> listResponse = new SearchListResponse<T>();
        SearchHits allHits = response.getHits();
        long totalHits = allHits.getTotalHits();
        LOGGER.debug("totalHits: " + totalHits);
        for (SearchHit hits : allHits.getHits()) {
            LOGGER.trace("hits : " + hits.sourceAsString());
            Map<String,Object> sourceAsMap = hits.sourceAsMap();
            LOGGER.debug("source as MAp : "+sourceAsMap.getClass().getName());
            CMDSQuestion paraQuestion = null;
            try {
                paraQuestion = CMDSQuestionDAO.INSTANCE.getQuestionById(((Map<String,Object>)sourceAsMap.get("content")).get("id").toString());
            } catch (VedantuException e) {
                LOGGER.debug("Exception while retreiveing data : "+e.getMessage());
            }
            catch (Exception ex){
                LOGGER.debug("Exception while converting MAP : "+ex.getMessage());
            }
            if(paraQuestion != null){
                sourceAsMap.put("published", paraQuestion.published);
                sourceAsMap.put("soultionInfo", paraQuestion.solutionInfo);
                sourceAsMap.put("questionBody", paraQuestion.questionBody);
                sourceAsMap.put("type", paraQuestion.type);
                sourceAsMap.put("status", paraQuestion.status);
                sourceAsMap.put("hints", paraQuestion.hints);
                sourceAsMap.put("size", paraQuestion.size);
                sourceAsMap.put("id", paraQuestion._getStringId());
                sourceAsMap.put("questionSetName", paraQuestion.questionSetName);
                sourceAsMap.put("questionSetId", paraQuestion.questionSetId);
                sourceAsMap.put("source", paraQuestion.source);
                sourceAsMap.put("globalQid", paraQuestion.globalQid);
                sourceAsMap.put("addBoardInfo", true);
            }
            T model = ObjectMapperUtils.convertValue(sourceAsMap, respObj);
            listResponse.list.add(model);
        }
        if (facets != null && facets.length > 0) {
            ElasticSearchUtils.addCommonFacetDetails(listResponse.facet, response);
        }
        listResponse.totalHits = qIds.size();
        return listResponse;
    }

    public GetMultiUsageRes getUsages(GetUsageReq request) throws VedantuException {

        GetUsageReq internalRequest = new GetUsageReq();
        GetMultiUsageRes response = new GetMultiUsageRes();

        if (request.containerType != null) {
            internalRequest.containerType = request.containerType;

            GetUsageRes containerResponse = getUsage(request.id, request.containerType,
                    request.start, request.size, VedantuRecordState.ACTIVE);
            response.usages.add(containerResponse);
            response.isUsed = CollectionUtils.isNotEmpty(containerResponse.data.list)
                    || response.isUsed;
        } else {

            GetUsageRes containerResponse = getUsage(request.id, EntityType.CMDSTEST,
                    request.start, request.size, VedantuRecordState.ACTIVE);
            response.usages.add(containerResponse);

            containerResponse = getUsage(request.id, EntityType.CMDSASSIGNMENT, request.start,
                    request.size, VedantuRecordState.ACTIVE);
            response.isUsed = CollectionUtils.isNotEmpty(containerResponse.data.list)
                    || response.isUsed;

            response.usages.add(containerResponse);
            response.isUsed = CollectionUtils.isNotEmpty(containerResponse.data.list)
                    || response.isUsed;
        }
        return response;
    }

    /**
     * This API is under development as of now will cater for usage of current content in respective
     * location
     *
     * @param request
     * @return
     * @throws VedantuException
     */
    private static GetUsageRes getUsage(String id, EntityType containerType, int start, int size,
            VedantuRecordState state) throws VedantuException {

        MutableLong totalHits = new MutableLong();
        GetUsageRes response = new GetUsageRes();

        VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(containerType);
        if (basicDAO instanceof IContainable) {
            @SuppressWarnings("unchecked")
            IContainable<? extends VedantuBaseMongoModel> containable = (IContainable<? extends VedantuBaseMongoModel>) basicDAO;
            List<? extends VedantuBaseMongoModel> modelList = containable.getContainers(id, start,
                    size, state, totalHits);
            response.type = containerType;

            for (VedantuBaseMongoModel model : modelList) {

                response.data.list.add(model.toBasicInfo());
            }
            response.data.totalHits = totalHits.longValue();
        }

        return response;
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<CMDSQuestion> questions = new ArrayList<CMDSQuestion>();

        if (StringUtils.isNotEmpty(id)) {
            CMDSQuestion question = CMDSQuestionDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            questions.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof CMDSQuestion) {
                    questions.add((CMDSQuestion) content);
                }
            }
        }
        // calculate question image size;

        for (CMDSQuestion question : questions) {
            if( question.size.isFinalized() && !recalculate){
                continue;
            }
            question.size.reset();

            // question body image sizes

            if (CollectionUtils.isNotEmpty(question.questionBody.uuidImages)) {
                updateSizes(question, question.questionBody.uuidImages);
            }

            if (question._hasOptions()) {

                if (question.solutionInfo.optionBody != null
                        && CollectionUtils.isNotEmpty(question.solutionInfo.optionBody.uuidImages)) {
                    updateSizes(question, question.solutionInfo.optionBody.uuidImages);
                }
            }

            if (question._hasSolutions()) {
                for (SolutionFormat solutionFormat : question.solutionInfo.solutions) {

                    if (CollectionUtils.isNotEmpty(solutionFormat.uuidImages)) {
                        updateSizes(question, solutionFormat.uuidImages);
                    }

                }
            }

            if (question._hasHints()) {
                for (HintFormat hintFormat : question.hints.hints) {

                    if (CollectionUtils.isNotEmpty(hintFormat.uuidImages)) {
                        updateSizes(question, hintFormat.uuidImages);
                    }
                }
            }
            question.size.finalize();
            CMDSQuestionDAO.INSTANCE.updateModel(question, Arrays.asList(CMDSQuestion.SIZE));
            if (question.globalQid != null) {
                (new QuestionManager()).calculate(question.globalQid,true );
            }
        }

        return true;

    }

    private void updateSizes(CMDSQuestion question, Set<String> uuidImages) {

        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.CMDSQUESTION);
        for (String optionImageId : uuidImages) {

            long originalSize = defs.size(optionImageId, EntityType.CMDSQUESTION,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.ORIGINAL);
            long thumbnailSize = defs.size(optionImageId, EntityType.CMDSQUESTION,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.MEDIUM);
            if (originalSize != -1) {
                question.size.addOriginal(originalSize);
            }
            if (thumbnailSize != -1) {
                question.size.addThumbnail(thumbnailSize);
            }

        }
    }

    // If you have come here, It means admin/prakash had mapped Learnpedia course boards to B2B-client course boards incorrectly
    // and also he might have shared questions with wrong board mappings. So before executing this code, make sure you fix/update boardmappings in DB.
    // Then call this API from postman by passing orgId and corrupted boardId.

    // Here orgId is the B2B organisation to which we have shared corrupted boardId questions
    // Here boardId is the corrupted course board which we have mapped in boardmappings table. This boardId is what we need to update
    public void fixBoards(String orgId, String boardId) {
//        List<String> types = new ArrayList<String>();
//        types.add("SCQ");
//        types.add("MCQ");
//        types.add("NUMERIC");
//        LOGGER.debug("fixBoards : types are "+types.toString());
        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        DBObject query = new BasicDBObject();
        query.put(ConstantsGlobal.SCOPE, Scope.PRIVATE.name());
        query.put(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE.name());
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_ID, orgId);
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_TYPE, EntityType.ORGANIZATION.name());
//        query.put(ConstantsGlobal.TYPE, new BasicDBObject(MongoManager.IN_QUERY, types));
        List<String> courseBoardsTofix = new ArrayList<String>();
        Map<String,String> parentToSharedBoardMap = new HashMap<String,String>();
        Map<String,String> sharedToParentBoardMap = new HashMap<String,String>();
        BoardMapping boardMapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(learnpediaId, orgId);
        if(boardMapping == null){
            LOGGER.debug("fixBoards : There are no board mappings found for this orgId");
            return;
        }
        List<BoardMappings> boardMappings = boardMapping.boardMappings;
        for(BoardMappings boardMap : boardMappings){
            if(boardMap.boardType.equals(BoardType.COURSE.name())){
                //parentCourseBoards.add(boardMap.parentBoardId);
            }
            parentToSharedBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
            sharedToParentBoardMap.put(boardMap.sharedToBoardId, boardMap.parentBoardId);
        }
        courseBoardsTofix.add(boardId);
        LOGGER.debug("fixBoards : parent course boards are "+courseBoardsTofix.toString());
        query.put(ConstantsGlobal.BOARD_IDS, new BasicDBObject(MongoManager.IN_QUERY, courseBoardsTofix));
        LOGGER.debug("fixBoards : Before getting count of all corrupted questions, Query");
        VedantuDBResult<CMDSQuestion> cmdsQuestions = CMDSQuestionDAO.INSTANCE.getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        LOGGER.debug("fixBoards : After getting count of all corrupted questions, Query");
        LOGGER.debug("fixBoards : Total count of all corrupted questions from Query is "+cmdsQuestions.totalHits);
        Iterator<CMDSQuestion> cmdsQuestionsIterator = cmdsQuestions.results.iterator();
        EditQuestionReq request = new EditQuestionReq();
        request.updateList.add("brdIds");
        SrcEntity entity = new SrcEntity();
        entity.type = EntityType.CMDSQUESTION;
        int count = 1;
        while(cmdsQuestionsIterator.hasNext()){
            CMDSQuestion cmdsQuestion = cmdsQuestionsIterator.next();
            entity.id = cmdsQuestion._getStringId();
            LOGGER.debug("fixBoards : Fixing "+count+" cmdsQuestion "+cmdsQuestion._getStringId());
            request.entity = entity;
            List<SrcEntityEdit> editEntities = new ArrayList<SrcEntityEdit>();
            // Get edit entities
            List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(cmdsQuestion._getStringId());
            Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                    associatedContentsList);
            LOGGER.debug("fixBoards : ..........AssociatedContentsList.........." + associatedContentsList);
            LOGGER.debug("fixBoards : ..........AssociatedContents.........." + associatedContents);

            for(SrcEntityPublishableState associatedContent : associatedContentsList){
                SrcEntityEdit editEntity = new SrcEntityEdit();
                editEntity.entity = associatedContent.entity;
                editEntity.editType = EntityEditType.RIGHT_AWAY;
                editEntities.add(editEntity);
            }
            LOGGER.info("fixBoards : AssociatedContentCount" + editEntities);

            request.editEntities = editEntities;
            request.orgId = cmdsQuestion.contentSrc.id;
            request.userId = cmdsQuestion.userId;
            request.brdIds.clear();
            request.brdIds.addAll(getFixedBoardIds(cmdsQuestion.boardIds,parentToSharedBoardMap));
            request.callingUserId = cmdsQuestion.userId;
            request.callingApp = "cmds-app";
            request.callingAppId = "cmds-app";
            try {
                LOGGER.debug("fixBoards : Editing shared "+orgId+" question "+cmdsQuestion._getStringId());
                CMDSQuestionManager.INSTANCE.update(request);
                LOGGER.debug("fixBoards : Edited shared "+orgId+" question "+cmdsQuestion._getStringId());
            } catch (VedantuException e) {
                LOGGER.debug("Error while Editing shared "+orgId+" Question "+cmdsQuestion._getStringId()+" with message "+e.getMessage());
            }
            count++;
        }
    }

    public void fixBoardMappings(String orgId) {

        String learnpediaId = Play.application().configuration().getString("learnpedia.id");
        DBObject query = new BasicDBObject();
        query.put(ConstantsGlobal.SCOPE, Scope.PRIVATE.name());
        query.put(ConstantsGlobal.RECORD_STATE, VedantuRecordState.ACTIVE.name());
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_ID, orgId);
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_TYPE, EntityType.ORGANIZATION.name());
        query.put(ConstantsGlobal.BOARD_IDS, new BasicDBObject(MongoManager.SIZE, 1));
        LOGGER.debug("fixBoardMappings  :: Before Getting Quextions Count to Fix ");
        VedantuDBResult<CMDSQuestion> cmdsQuestions = CMDSQuestionDAO.INSTANCE.getInfos(query, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null);
        LOGGER.debug("fixBoardMappings  :: After Getting Quextions Count to Fix ");
        LOGGER.debug("fixBoardMappings Total Questions to Fix : "+cmdsQuestions.totalHits);
        LOGGER.debug("fixBoardMappings Getting all parentToShared board mappings...");
        Map<String,String> parentToSharedBoardMap = new HashMap<String,String>();
        BoardMapping boardMapping = BoardMappingsDAO.INSTANCE.getBySharedToOrgId(learnpediaId, orgId);
        if(boardMapping == null){
            LOGGER.debug("fixBoards : There are no board mappings found for this orgId");
            return;
        }
        List<BoardMappings> boardMappings = boardMapping.boardMappings;
        for(BoardMappings boardMap : boardMappings){
            parentToSharedBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
        }
        LOGGER.debug("fixBoardMappings Got all parentToShared board mappings. Whoz count is "+parentToSharedBoardMap.size());
        Iterator<CMDSQuestion> cmdsQuestionsIterator = cmdsQuestions.results.iterator();
        EditQuestionReq request = new EditQuestionReq();
        request.updateList.add("brdIds");
        SrcEntity entity = new SrcEntity();
        entity.type = EntityType.CMDSQUESTION;
        int count = 1;
        while(cmdsQuestionsIterator.hasNext()){
            CMDSQuestion cmdsQuestion = cmdsQuestionsIterator.next();
            entity.id = cmdsQuestion._getStringId();
            LOGGER.debug("fixBoardMappings : Fixing "+count+" cmdsQuestion "+cmdsQuestion._getStringId());
            request.entity = entity;
            List<SrcEntityEdit> editEntities = new ArrayList<SrcEntityEdit>();
            // Get edit entities
            List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(cmdsQuestion._getStringId());
            Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                    associatedContentsList);
            LOGGER.debug("fixBoardMappings : ..........AssociatedContentsList.........." + associatedContentsList);
            LOGGER.debug("fixBoardMappings : ..........AssociatedContents.........." + associatedContents);

            for(SrcEntityPublishableState associatedContent : associatedContentsList){
                SrcEntityEdit editEntity = new SrcEntityEdit();
                editEntity.entity = associatedContent.entity;
                editEntity.editType = EntityEditType.RIGHT_AWAY;
                editEntities.add(editEntity);
            }
            LOGGER.info("fixBoardMappings : AssociatedContentCount" + editEntities);
            request.editEntities = editEntities;
            request.orgId = cmdsQuestion.contentSrc.id;
            request.userId = cmdsQuestion.userId;
            request.brdIds.clear();
            request.brdIds.addAll(getFixedBoardIds(cmdsQuestion.parentQId,parentToSharedBoardMap));
            request.callingUserId = cmdsQuestion.userId;
            request.callingApp = "cmds-app";
            request.callingAppId = "cmds-app";
            try {
                LOGGER.debug("fixBoardMappings : Editing shared "+orgId+" question "+cmdsQuestion._getStringId());
                LOGGER.debug("fixBoardMappings : LOGGING REQUEST Before : "+cmdsQuestion.boardIds.toString()+" After : "+request.brdIds.toString());
                if(!request.brdIds.isEmpty()){
                    CMDSQuestionManager.INSTANCE.update(request);
                }else{
                    LOGGER.error("fixBoardMappings : Corresponding Board Id Is Empty");
                }
                LOGGER.debug("fixBoardMappings : Edited shared "+orgId+" question "+cmdsQuestion._getStringId());
            } catch (VedantuException e) {
                LOGGER.debug("fixBoardMappings : Error while Editing shared "+orgId+" Question "+cmdsQuestion._getStringId()+" with message "+e.getMessage());
            }
            count++;
        }
    }

    private Set<String> getFixedBoardIds(Set<String> boardIds,
            Map<String, String> parentToSharedBoardMap) {
        // TODO Auto-generated method stub
        Set<String> newBoards = new HashSet<String>();
        for(String brdId:boardIds){
            newBoards.add(parentToSharedBoardMap.get(brdId));
        }
        return newBoards;
    }

    private Set<String> getFixedBoardIds(String parentCmdsQuestionId,
            Map<String, String> parentToSharedBoardMap) {
        // TODO Auto-generated method stub
        Set<String> newBoards = new HashSet<String>();
        CMDSQuestion cmdsQuestion = CMDSQuestionDAO.INSTANCE.getById(parentCmdsQuestionId);
        for(String brdId:cmdsQuestion.boardIds){
            newBoards.add(parentToSharedBoardMap.get(brdId));
        }
        return newBoards;
    }

    public void fixMissingSolutions(String orgId, String scope, String cmdsQId) throws VedantuException {
        // TODO Auto-generated method stub
        List<CMDSQuestion> cmdsQuestions = CMDSQuestionDAO.INSTANCE.getQuestionsByOrgIdAndScope(orgId, scope.toUpperCase(), cmdsQId);
        LOGGER.debug("fixMissingSolutions : Total questions "+cmdsQuestions.size());
        int count = 1;
        boolean edited;
        Gson gson = new Gson();
        for(CMDSQuestion cmdsQuestion : cmdsQuestions){
            LOGGER.debug("fixMissingSolutions : Current cmdsQuestion count "+count);
            edited = false;
            if(cmdsQuestion.published){
                if(CollectionUtils.isNotEmpty(cmdsQuestion.solutionInfo.solutions)){
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has solutions");
                    Question que = QuestionDAO.INSTANCE.getById(cmdsQuestion.globalQid);
                    for(SolutionFormat sol : cmdsQuestion.solutionInfo.solutions){
                        if(!StringUtils.isEmpty(sol.globalSolId)){
                            LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has globalSolId");
                            //Implement validation
                            Solution solu = SolutionDAO.INSTANCE.getById(sol.globalSolId);
                            if(!solu.qId.equals(cmdsQuestion.globalQid)){
                                LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has INVALID globalSolId");
                                String jsonString = gson.toJson(solu);
                                Solution newSolution = gson.fromJson(jsonString, Solution.class);
                                newSolution.qId = cmdsQuestion.globalQid;
                                newSolution.userId = cmdsQuestion.userId;
                                newSolution.id = null;
                                newSolution.timeCreated = que.timeCreated;
                                newSolution.lastUpdated = que.timeCreated;
                                SolutionDAO.INSTANCE.save(newSolution);
                                sol.globalSolId = newSolution._getStringId();
                                que.solutions++;
                                edited = true;
                            }else{
                                LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has VALID globalSolId");
                            }
                        }else{
                            LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has NO globalSolId");
                        }
                    }
                    if(edited){
                        QuestionDAO.INSTANCE.save(que);
                        generateEventAysc(que.userId, que, EventActionType.UPDATE,
                                EventType.INDEX_QUESTION, UserActionType.UPDATED, false);
                        CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
                        generateEventAysc(cmdsQuestion.userId, cmdsQuestion, EventActionType.UPDATE,
                                EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
                    }
                }else{
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is published of type "+cmdsQuestion.type+" has NO solutions");
                }
            }else{
                if(CollectionUtils.isNotEmpty(cmdsQuestion.solutionInfo.solutions)){
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has solutions");
                    for(SolutionFormat sol : cmdsQuestion.solutionInfo.solutions){
                        if(!StringUtils.isEmpty(sol.globalSolId)){
                            LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has globalSolId");
                            sol.globalSolId = null;
                            edited = true;
                        }else{
                            LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has NO globalSolId");
                        }
                    }
                }else{
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has NO solutions");
                }
                if(!StringUtils.isEmpty(cmdsQuestion.solutionInfo.globalAnsId)){
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has globalAnsId");
                    cmdsQuestion.solutionInfo.globalAnsId = null;
                    edited  = true;
                }else{
                    LOGGER.debug("fixMissingSolutions : This cmds question "+cmdsQuestion._getStringId()+" which is unpublished of type "+cmdsQuestion.type+" has NO globalAnsId");
                }
                if(edited){
                    CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
                    generateEventAysc(cmdsQuestion.userId, cmdsQuestion, EventActionType.UPDATE,
                            EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
                }
            }
            count++;
        }
    }

}

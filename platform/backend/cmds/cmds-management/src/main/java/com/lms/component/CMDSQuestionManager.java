package com.lms.component;

import com.google.gson.Gson;
import com.lms.cmds.HintFormat;
import com.lms.cmds.HintInfo;
import com.lms.cmds.SolutionInfo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.CMDSResourcesManager;
import com.lms.components.ChallengeServiceModule;
import com.lms.components.QuestionComponent;
import com.lms.enums.*;
import com.lms.managers.AbstractContentManager;
import com.lms.models.*;
import com.lms.pojo.BoardMappings;
import com.lms.pojos.*;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.questions.GetSolutionsRes;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.question.SolutionFormat;
import com.lms.repo.CMDSAssignmentRepo;
import com.lms.repo.CMDSTestRepo;
import com.lms.repository.AnswerRepo;
import com.lms.repository.CMDSQuestionRepo;
import com.lms.repository.QuestionRepo;
import com.lms.repository.SolutionRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CMDSQuestionManager extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(CMDSQuestionManager.class);
    @Value("${learnpedia.id}")
    private String learnPediaId;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private CMDSModuleManager cmdsModuleManager;
    @Autowired
    private CMDSResourcesManager cmdsResourcesManager;
    @Autowired
    private CMDSAssignmentRepo cmdsAssignmentRepo;
    @Autowired
    private CMDSTestRepo cmdsTestRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private AnswerRepo answerRepo;
    @Autowired
    private SolutionRepo solutionRepo;
    @Autowired
    private ChallengeServiceModule challengeServiceModule;


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

        logger.debug("Received difficulty" + request.difficulty);
        question.difficulty = request.difficulty;

        // validating hints
        question.hints = new HintInfo();
        List<HintFormat> formattedHints = new ArrayList<HintFormat>();

        List<Integer> deductions = new ArrayList<Integer>();
        if (CollectionUtils.isNotEmpty(request.hints)) {
            for (String hintText : request.hints) {
                if (!StringUtils.isEmpty(hintText)) {
                    HintFormat hintFormat = new HintFormat();
                    hintFormat.newText = hintText;

                    formattedHints.add(hintFormat);
                    deductions.add(0);

                } else {
                    throw new VedantuException(VedantuErrorCode.INCORRECT_HINT_FORMAT);
                }
            }

            question.hints.deductions = deductions;
            // question.hints.hints = formattedHints;

        }
        // adding type
        if (request.type == QuestionType.UNKNOWN) {
            throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_TYPE);
        }
        question.type = request.type;

        // check if boardIds are provided
        logger.debug("Re0ceived board ids" + request.brdIds);
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
            logger.debug("Inside CMDSQuestionManager MCQ || PARA || MATRIX type: " + question.type);
            MCQsolutionInfo mcqSolInfo = new MCQsolutionInfo();
            // validations?
            mcqSolInfo.answer = request.answers;
            mcqSolInfo.optionBody.newOptions = request.options;
            solInfo = mcqSolInfo;

        }
        if (question.type == QuestionType.TEXT) {
            logger.debug("Inside CMDSQuestionManager TEXT type: " + question.type);
            TextSolutionInfo textSolInfo = new TextSolutionInfo();
            textSolInfo.answer = request.answers != null ? request.answers.get(0) : null;
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

        if (question.questionBody == null && !StringUtils.isEmpty(request.content)) {
            question.questionBody = new QuestionFormat();
            question.questionBody.newText = request.content;
        }

        if (CollectionUtils.isNotEmpty(request.tags)) {
            question.tags.addAll(request.tags);
        }
        question.status = QuestionStatus.INCOMPLETE;

        if (request.type == QuestionType.PARA) {
            question.paragraphId = request.paraId;
        }

        try {

            if (question != null) {
                question.addHook();

            }
            question.removeImageSrc(true);

            // CMDSImageUtil.convertImageUrlToUuidAndSaveImage(question, true, false);
            if (question.type == QuestionType.PARA) {
                if (request.folderId == null || request.folderId.isEmpty() || request.folderId.equals("null")) {
                    List<String> folderIds = getFolderIds(question.paragraphId);
                    request.folderId = (folderIds.isEmpty() || folderIds == null) ? "" : folderIds.get(0);
                }
            }
            logger.debug("FolderId is: " + request.folderId);
            addQuestion(request.orgId, request.userId, request.folderId, question);
            addQuestionRes = new AddQuestionRes(question._getStringId());
            if (request.type == QuestionType.TEXT) {
                addQuestionRes.paraId = question.id + "";
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            if (addQuestionRes == null) {
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
        }

        return addQuestionRes;
    }

    private List<String> getFolderIds(String globalQid) {

        logger.info(".............Entered getFolderIds function.............." + globalQid);
        List<String> folderIds = new ArrayList<String>();
        if (globalQid != null) {
            folderIds = getfolderIds(globalQid);
        }
        logger.info(".............Exited getFolderIds function..............");
        return folderIds;
    }

    public List<String> getfolderIds(String globalQid) {
        logger.info(".............Entered getFolderIds DAO function..............");
        List<String> folderIds = new ArrayList<String>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("source.id").equals(globalQid);
        criteria.and("target.type").equals(EntityType.FOLDER);
        query.addCriteria(criteria);

        List<CMDSContentLink> cmdsContentLinks = mongoTemplate.find(query, CMDSContentLink.class);
        for (CMDSContentLink cmdsContentLink : cmdsContentLinks) {
            folderIds.add(cmdsContentLink.target.id);
        }

        return folderIds;
    }

    public CMDSQuestion addQuestion(String orgId, String userId, String folderId,
                                    CMDSQuestion question) throws Exception {

        /* Added by Shivank */
        question.completed = isReadyToPublished(question);
        /* Added by Shivank */

        cmdsQuestionRepo.save(question);
        // Adding question ID of type PARA to list of question type TEXT
        if (question.type == QuestionType.PARA) {
            addParaIdToTextQuestion(question);
        }
        SrcEntity questionEntity = new SrcEntity(EntityType.CMDSQUESTION, question._getStringId());

        //if(question.type != QuestionType.PARA){
        questionComponent.generateEventAysc(userId, question, UserActionType.EventActionType.ADD, EventType.INDEX_CMDS_QUESTION,
                UserActionType.ADDED, true);
        //}

        String parentESId = cmdsModuleManager.addAsCMDSResource(questionEntity,
                UserActionType.EventActionType.ADD, question);
        logger.debug("parentEsID is: " + parentESId);
        if (StringUtils.isEmpty(question.questionSetId)) {
            cmdsModuleManager.addToFolder(orgId, userId, questionEntity, folderId,
                    CmdsContentLinkType.ADDED, parentESId);
        }
        String learnpediaId = learnPediaId;
        if (orgId.equals(learnpediaId)) {
            logger.debug("addQuestion : About share " + question.type + " Question");
            //shareQuestion(question,orgId,userId);
            logger.debug("addQuestion : Shared " + question.type + " Question");
        }
        return question;
    }

    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSQuestion) {

            CMDSQuestion question = (CMDSQuestion) cmdsModel;

            boolean canBePublished = true;

            if (canBePublished && (question.recordState != VedantuRecordState.ACTIVE)) {
                canBePublished &= false;
                logger.debug(" Question is not in active state id:" + question._getStringId());
            }

            if (canBePublished) {
                if (question.type == QuestionType.SCQ) {
                    SCQSolutionInfo sCQSolutionInfo = (SCQSolutionInfo) question.solutionInfo;
                    if (StringUtils.isEmpty(sCQSolutionInfo.answer)) {
                        logger.debug(" Answer is not provided for SCQ id:"
                                + question._getStringId());
                    }
                }

                if (question.type == QuestionType.MCQ || question.type == QuestionType.PARA || question.type == QuestionType.MATRIX) {
                    MCQsolutionInfo mCQsolutionInfo = (MCQsolutionInfo) question.solutionInfo;
                    if (CollectionUtils.isEmpty(mCQsolutionInfo.answer)) {
                        logger.debug(" Answer is not provided for MCQ/PARA id:"
                                + question._getStringId());
                    }
                }

                if (question.type == QuestionType.NUMERIC) {
                    NumericSolutionInfo numericSolutionInfo = (NumericSolutionInfo) question.solutionInfo;
                    if (StringUtils.isEmpty(numericSolutionInfo.answer)) {
                        logger.debug(" Answer is not provided for Numeric id:"
                                + question._getStringId());
                    }
                }
            }

            if (canBePublished
                    && (question.questionBody == null || StringUtils
                    .isEmpty(question.questionBody.newText))) {
                canBePublished &= false;
                logger.debug(" Invalid Question Body id:" + question._getStringId());
            }

            if (canBePublished && (CollectionUtils.isEmpty(question.boardIds))) {
                canBePublished &= false;
                logger.debug(" Question is added but no boards id:" + question._getStringId());
            }

            if (canBePublished && (question.type == null || question.type == QuestionType.UNKNOWN)) {
                canBePublished &= false;
                logger.debug(" Invalid Question Type id:" + question._getStringId());
            }

            if (canBePublished
                    && (question.difficulty == null || question.difficulty == Difficulty.UNKNOWN)) {
                canBePublished &= false;
                logger.debug(" Invalid Question Difficulty id:" + question._getStringId());
            }

            return canBePublished;
        }
        return false;
    }

    private void addParaIdToTextQuestion(CMDSQuestion question) {
        try {
            Optional<CMDSQuestion> textQuestion1 = cmdsQuestionRepo.findById(question.paragraphId);
            CMDSQuestion textQuestion = textQuestion1.get();
            List<String> paraIds = textQuestion.paraIds;
            logger.debug("ParaIds are " + paraIds.toString());
            paraIds.add(String.valueOf(question.id));
            textQuestion.paraIds = paraIds;
            cmdsQuestionRepo.save(textQuestion);
        } catch (VedantuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public EditContentRes update(EditQuestionReq request) throws VedantuException {

        logger.info(".............Entered Manager Update function" + request.solution + "...");
        Optional<CMDSQuestion> questionOriginal1 = cmdsQuestionRepo.findById(request.entity.id);
        CMDSQuestion questionOriginal = questionOriginal1.get();
        CMDSQuestion question = new CMDSQuestion();
        boolean updateAll = CollectionUtils.isEmpty(request.updateList);
        if (questionOriginal == null) {
            logger.error("question[" + request.entity.id + "] not found");
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

        logger.debug("..........AssociatedContentsList.........." + associatedContentsList);
        logger.debug("..........AssociatedContents.........." + associatedContents);
        logger.info("AssociatedContentCount" + editEntities);

        if (associatedContents.size() > 0 && editEntities.size() == 0) {
            logger.info(".............Both the collections are different..............");
            questionUpdateRes.isUpdated = false;
        }
        //
        else {
            logger.info(".............Both the collections are same or null..............");

            boolean createCopy = false;
            boolean updateRightAway = false;

            for (SrcEntityEdit editEntity : editEntities) {
                logger.info("............Inside for loop..............");
                if (editEntity.editType == EntityEditType.CREATE_COPY) {
                    logger.info("............Create copy..............");
                    createCopy = true;
                }
                if (editEntity.editType == EntityEditType.RIGHT_AWAY) {
                    logger.info("............Update right away..............");
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

            logger.debug("Received difficulty" + request.difficulty);
            // question.difficulty = request.difficulty;

            // validating hints

            List<Integer> deductions = new ArrayList<Integer>();

            if (updateAll || request.updateList.contains("hints")) {
                logger.info(".............Checking for hints..............");
                if (request.hints == null) {
                    question.hints = new HintInfo();
                } else {
                    question.hints = new HintInfo();
                    List<HintFormat> formattedHints = new ArrayList<HintFormat>();
                    for (String hintText : request.hints) {
                        if (!StringUtils.isEmpty(hintText)) {
                            HintFormat hintFormat = new HintFormat();
                            hintFormat.newText = hintText;

                            formattedHints.add(hintFormat);
                            deductions.add(0);

                        } else {
                            throw new VedantuException(VedantuErrorCode.INCORRECT_HINT_FORMAT);
                        }
                    }

                    question.hints.deductions = deductions;
                    question.hints.hints = formattedHints;
                }
            }
            // adding type
            if (question.published == false) {
                if (updateAll || request.updateList.contains("type")) {
                    if (request.type != QuestionType.UNKNOWN) {
                        question.type = request.type;
                    }
                }
            }


            // check if boardIds are provided
            logger.debug("Received board ids" + request.brdIds);
            if (question.published == false) {
                if (updateAll || request.updateList.contains("brdIds")) {

                    // if (updateRightAway == true) {
                    //     LOGGER.debug(".....before error.......");
                    //     throw new VedantuException(VedantuErrorCode.QUESTION_SUBJECT_CANNOT_BE_EDITED,
                    //             "Question subject cannot be edited because it is bounded to test");
                    // }
                    logger.debug(".....after error.......");
                    question.boardIds = new HashSet<String>();
                    if (request.brdIds != null) {
                        question.boardIds.addAll(request.brdIds);
                    }
                }
            }
            // check if targetIds
            // TODO: confirm targetIds
            if (question.published == false) {
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
                logger.info(".............Checking for answers..............");
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

                            question.solutionInfo.optionBody.newOptions = request.options;
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

                            question.solutionInfo.optionBody.newOptions = request.options;
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
                logger.info(".............Update list contains tags..............");
                question.tags = new HashSet<String>();
                if (request.tags != null) {
                    question.tags.addAll(request.tags);
                }

            }

            if (updateAll || request.updateList.contains("difficulty")) {
                logger.info(".............Update list contains difficulty..............");
                if (request.difficulty != null) {
                    question.difficulty = request.difficulty;

                }
            }

            if (updateAll || request.updateList.contains("source")) {
                logger.info(".............Update list contains source..............");
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

            logger.info(".............Question about to update..............");

            questionUpdateRes.isUpdated = true;
            question.completed = isReadyToPublished(question);
            cmdsQuestionRepo.save(question);
            if (question.published) {
                publish(request.userId, request.orgId, question);
            }
            cmdsModuleManager.addAsCMDSResource(new SrcEntity(EntityType.CMDSQUESTION,
                    question._getStringId()), UserActionType.EventActionType.UPDATE, question);

            questionComponent.generateEventAysc(request.userId, question, UserActionType.EventActionType.UPDATE,
                    EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
            SrcEntity questionEntity = new SrcEntity(EntityType.CMDSQUESTION,
                    question._getStringId());

            String parentESId = cmdsModuleManager.addAsCMDSResource(questionEntity,
                    UserActionType.EventActionType.ADD, question);

            List<String> folderIds = getFolderIds(question._getStringId());
            logger.debug(".............Displaying folder ids..." + folderIds + "..............");
            for (String folderId : folderIds) {
                logger.debug(".............Adding to folder..." + folderId + "..............");
                cmdsModuleManager.addToFolder(request.orgId, request.userId, questionEntity,
                        folderId, CmdsContentLinkType.ADDED, parentESId);
            }
            // if (StringUtils.isEmpty(question.questionSetId)) {
            // CMDSResourcesManager.addToFolder(request.orgId, request.userId, questionEntity,
            // question.folderId,
            // CmdsContentLinkType.ADDED, parentESId);
            // }

            logger.info(".............Question Successfully updated..............");
            if (question.scope == Scope.PRIVATE) {
                logger.info(".............   Sending Email  (Not Implemented Yet)  .............");
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

    public List<SrcEntityPublishableState> getAssociatedContent(String globalQid) {

        logger.info(".............Entered Update function.............." + globalQid);
        List<SrcEntityPublishableState> entitiesPublishableState = new ArrayList<SrcEntityPublishableState>();
        if (globalQid != null) {
            entitiesPublishableState = getAssociatedConten(globalQid);
        }
        logger.info(".............Exited Update function..............");
        return entitiesPublishableState;
    }

    public List<SrcEntityPublishableState> getAssociatedConten(String globalQid) {
        logger.info(".............Entered getAssociatedContent DAO function..............");
        List<SrcEntityPublishableState> entitiesPublishableState = new ArrayList<SrcEntityPublishableState>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("metadata.details.qids").in(globalQid);
        //    List<CMDSTest> tests = cmdsTestRepo.findByMetaDataDetailsQidsIn(globalQid);
        // ds.find(CMDSTest.class).field("metadata.details.qIds").equal(globalQid).asList();
        List<CMDSTest> tests = mongoTemplate.find(query.addCriteria(criteria), CMDSTest.class);
        for (CMDSTest test : tests) {
            SrcEntity entity = new SrcEntity();
            entity.id = test._getStringId();
            entity.type = EntityType.CMDSTEST;

            SrcEntityPublishableState entityPublishableState = new SrcEntityPublishableState();
            entityPublishableState.entity = entity;
            entityPublishableState.name = test.name;
            entityPublishableState.published = test.published;

            entitiesPublishableState.add(entityPublishableState);
        }
        Query query1 = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and("metadata.qids").equals(globalQid);
        query1.addCriteria(criteria1);

        List<CMDSAssignment> assignments = mongoTemplate.find(query1, CMDSAssignment.class);
        //cmdsAssignmentRepo.findByMetadataQidsIn(globalQid);

        for (CMDSAssignment assignment : assignments) {
            SrcEntity entity = new SrcEntity();
            entity.id = assignment._getStringId();
            entity.type = EntityType.CMDSASSIGNMENT;

            SrcEntityPublishableState entityPublishableState = new SrcEntityPublishableState();
            entityPublishableState.entity = entity;
            entityPublishableState.name = assignment.name;
            entityPublishableState.published = assignment.published;

            entitiesPublishableState.add(entityPublishableState);
        }
        logger.info(".............Exited getAssociatedContent DAO function..............");
        return entitiesPublishableState;
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

    public Question publish(String userId, String orgId, CMDSQuestion cmdsQuestion)
            throws VedantuException {

        if ((cmdsQuestion.type == QuestionType.SCQ || cmdsQuestion.type == QuestionType.MCQ || cmdsQuestion.type == QuestionType.MATRIX || cmdsQuestion.type == QuestionType.PARA)
                && (CollectionUtils.isEmpty(cmdsQuestion.solutionInfo.optionBody.newOptions))) {
            throw new VedantuException(VedantuErrorCode.QUESTION_OPTIONS_ARE_MISSING);
        }
        logger.debug(" Whole cmdsQuestion " + cmdsQuestion.toString());

        Set<String> imguuids = collectAllUUIDForImages(cmdsQuestion);
        Question publishedQuestion = null;
        Optional<Question> publishedQuestion1 = null;
        if (!StringUtils.isEmpty(cmdsQuestion.globalQid)) {

            publishedQuestion1 = questionRepo.findById(cmdsQuestion.globalQid);
            publishedQuestion = publishedQuestion1.get();
            // if (publishedQuestion != null) {
            //     return publishedQuestion;
            // }
            publishedQuestion.content = cmdsQuestion.questionBody.newText;
            publishedQuestion.options = cmdsQuestion.solutionInfo != null && cmdsQuestion.solutionInfo.optionBody != null ? cmdsQuestion.solutionInfo.optionBody.newOptions
                    : new ArrayList<String>();
            publishedQuestion.type = cmdsQuestion.type;
            publishedQuestion.imgUuids = imguuids;
        } else {
            // since question not published already creating new Question
            logger.info("Creating new publishable question");

            publishedQuestion = new Question(
                    cmdsQuestion.questionBody.newText,
                    cmdsQuestion.userId,
                    cmdsQuestion.type,
                    cmdsQuestion.source,
                    imguuids,
                    LatexType.LATEX,
                    (cmdsQuestion.solutionInfo != null && cmdsQuestion.solutionInfo.optionBody != null ? cmdsQuestion.solutionInfo.optionBody.newOptions
                            : new ArrayList<String>()));

        }
        logger.info("qrquestion body newText: " + cmdsQuestion.questionBody.newText);
        publishedQuestion.contentSrc = cmdsQuestion.contentSrc;

        publishedQuestion.setQrQid(cmdsQuestion._getStringId());


        // if question type is Grid set options appropriately
//        if (publishedQuestion.type == QuestionType.MATRIX && cmdsQuestion.solutionInfo != null) {
//            GridSolutionInfo sInfo = (GridSolutionInfo) cmdsQuestion.solutionInfo;
//            if (publishedQuestion.options == null) {
//                publishedQuestion.options = new ArrayList<String>();
//            }
//            publishedQuestion.matrix = new HashMap<String, List<String>>();
//            publishedQuestion.matrix.put("A", sInfo.cola);
//            publishedQuestion.matrix.put("B", sInfo.colb);
//        }
        verifyAndSetMetadata(publishedQuestion, cmdsQuestion);
        publishedQuestion.scope = Scope.ORG;

        logger.debug("saving published question first  ");
        questionRepo.save(publishedQuestion);
        processQuestionForPublishing(publishedQuestion, cmdsQuestion.questionSetId);

        publishSolution(publishedQuestion, cmdsQuestion);

        logger.debug("saving published question again  ");

        questionRepo.save(publishedQuestion);

        cmdsQuestion.globalQid = publishedQuestion._getStringId();
        cmdsQuestion.published = true;
        cmdsQuestion.status = QuestionStatus.COMPLETE;
        logger.debug("saving cmds question again  ");
        cmdsQuestionRepo.save(cmdsQuestion);
        cmdsResourcesManager.addLink(
                new SrcEntity(EntityType.CMDSQUESTION, cmdsQuestion._getStringId()), new SrcEntity(
                        EntityType.QUESTION, publishedQuestion._getStringId()),
                CmdsContentLinkType.PUBLISHED, userId, false);

        logger.debug("indexing question first time.. ");
        // live add global question search index
        QuestionSearchIndexDetails details = new QuestionSearchIndexDetails();
        details.fromMongoModel(publishedQuestion);
        CMDSModuleManager.addLiveEntityToSearchIndex(details, EntityType.QUESTION, true);

        questionComponent.generateEventAysc(cmdsQuestion.userId, cmdsQuestion, UserActionType.EventActionType.UPDATE,
                EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
        return publishedQuestion;
    }

    private Set<String> collectAllUUIDForImages(CMDSQuestion question) {

        Set<String> imguuids = new HashSet<String>();
        // collecting all questionbody uuids
        if (question.questionBody != null
                && CollectionUtils.isNotEmpty(question.questionBody.uuidImages)) {
            imguuids.addAll(question.questionBody.uuidImages);
        }

        // collecting all solution uuids
        if (question.solutionInfo != null && question.solutionInfo.optionBody != null
                && CollectionUtils.isNotEmpty(question.solutionInfo.optionBody.uuidImages)) {

            imguuids.addAll(question.solutionInfo.optionBody.uuidImages);
        }
        return imguuids;
    }

    private void verifyAndSetMetadata(Question publishedQuestion, CMDSQuestion cmdsQuestion)
            throws VedantuException {

        // Metadata providedMetadata = cmdsQuestion.metadata;
        // add exam related info
        logger.info("Publishing difficulty" + cmdsQuestion.difficulty);

        if (cmdsQuestion.difficulty != null && cmdsQuestion.difficulty != Difficulty.UNKNOWN) {

            publishedQuestion.difficulty = cmdsQuestion.difficulty;

        } else {
            throw new VedantuException(VedantuErrorCode.DIFFICULTY_NOT_SPECIFIED);
        }

        if (cmdsQuestion.boardIds != null) {
            if (publishedQuestion.boardIds == null) {
                publishedQuestion.boardIds = new HashSet<String>();
            }
            publishedQuestion.boardIds.addAll(cmdsQuestion.boardIds);
        } else {
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
        }

        if (cmdsQuestion.targetIds != null) {
            if (publishedQuestion.targetIds == null) {
                publishedQuestion.targetIds = new HashSet<String>();
            }
            publishedQuestion.targetIds.addAll(cmdsQuestion.targetIds);
        }

        if (CollectionUtils.isNotEmpty(cmdsQuestion.tags)) {
            if (publishedQuestion.tags == null) {
                publishedQuestion.tags = new HashSet<String>();
            }
            publishedQuestion.tags.addAll(cmdsQuestion.tags);
        }

    }

    public void processQuestionForPublishing(Question question, String questionSetId) {

        if (CollectionUtils.isEmpty(question.imgUuids)) {
            return;
        }
        for (String uuid : question.imgUuids) {
            // copySolut(true, uuid, question._getStringId(), questionSetId);
            logger.info("uuid: " + uuid);
            logger.info("question contains uuid: " + question.content.contains(uuid));
            logger.info("bedfore content publish: " + question.content);
            // question.content = ImageDisplayURLUtil.getGlobalTypeImageFormat(question.content,
            // uuid);
            logger.info("after content published: " + question.content);
            // if (CollectionUtils.isNotEmpty(question.options)) {
            // List<String> options = new ArrayList<String>();
            // // for (String option : question.options) {
            // // // options.add(ImageDisplayURLUtil.getGlobalTypeImageFormat(option, uuid));
            // // }
            // // question.options = options;
            // }

            if (!question.matrix.isEmpty()) {
                Set<String> keys = question.matrix.keySet();
                // for (String key : keys) {
                // // List<String> convertedOptn = new ArrayList<String>();
                // // for (String noptn : question.matrix.get(key)) {
                // // // noptn = ImageDisplayURLUtil.getGlobalTypeImageFormat(noptn, uuid);
                // // convertedOptn.add(noptn);
                // // }
                // // question.matrix.remove(key);
                // // question.matrix.put(key, convertedOptn);
                // }

            }
        }
    }

    private boolean publishSolution(Question publishedQuestion, CMDSQuestion question)
            throws VedantuException {

        logger.debug("Publishing solutions" + question.solutionInfo.getClass());
        publishedQuestion.solutions = 0L;// no solutions are published yet so solution count is set
        // to 0
        // save all solutions
        if (question.solutionInfo == null) {
            return true;
        }

        if (CollectionUtils.isNotEmpty(question.solutionInfo.solutions)) {
            logger.debug(".... solutions is not null ....");
            for (SolutionFormat solution : question.solutionInfo.solutions) {
                Solution publishedSolution = null;
                Optional<Solution> publishedSolution1 = null;
                if (!StringUtils.isEmpty(solution.globalSolId)) {
                    logger.debug("....global solution id is not empty ....");
                    publishedSolution1 = solutionRepo.findById(solution.globalSolId);
                    //SolutionDAO.INSTANCE.getById(solution.globalSolId);
                    publishedSolution = publishedSolution1.get();
                }
                logger.debug("....outside if statement ....");
                if (publishedSolution == null) {
                    logger.debug(".......cmdsattachments......" + solution.attachments + "......");
                    List<Attachment> cmdsAttachments = solution.attachments;
                    List<Attachment> attachments = new ArrayList<Attachment>();
                    if (cmdsAttachments != null) {
                        for (Attachment cmdsAttachment : cmdsAttachments) {
                            SrcEntity cmdsEntity = cmdsAttachment.entity;
                            SrcEntity globalEntity = getGlobalEntity(cmdsEntity.id);
                            Attachment attachment = new Attachment();
                            attachment.entity = globalEntity;
                            attachments.add(attachment);
                        }
                    }

                    publishedSolution = new Solution(publishedQuestion._getStringId(),
                            publishedQuestion.userId, solution.newText, new ArrayList<String>(),
                            SolutionType.ORGS, attachments);
                    publishedSolution.verified = true;

                    if (solution.uuidImages != null) {
                        publishedSolution.imgUuids.addAll(solution.uuidImages);
                    }
                    logger.debug("saving solution : ");
                    logger.debug("saving solution : ");
                    solutionRepo.save(publishedSolution);

                    solution.globalSolId = publishedSolution._getStringId();
                    publishedQuestion.solutions++;

                    processSolutionForPublishing(publishedSolution, question.questionSetId);
                    solutionRepo.save(publishedSolution);
                    // need to save as we have updated solution.
                }
            }
        }

        // saving answer now answers
        logger.debug("saving answers : " + question.solutionInfo.getClass());
        Answer questionAnswer = answerRepo.findByqId(question.globalQid);
        if (questionAnswer == null) {
            questionAnswer = new Answer(publishedQuestion._getStringId(), publishedQuestion.userId,
                    publishedQuestion.type);
            logger.debug(" created new answer " + questionAnswer);
        }
        if (publishedQuestion.type == QuestionType.SCQ) {
            logger.debug("Publishing SCQ");
            SCQSolutionInfo sInfo = (SCQSolutionInfo) question.solutionInfo;
            logger.debug("Publishing scq answer" + sInfo.answer);
            questionAnswer.answer = Arrays.asList(sInfo.answer);
        } else if (publishedQuestion.type == QuestionType.MCQ || publishedQuestion.type == QuestionType.MATRIX || publishedQuestion.type == QuestionType.PARA) {
            MCQsolutionInfo sInfo = (MCQsolutionInfo) question.solutionInfo;
            Set answerSet = new HashSet(sInfo.answer);
//            answerSet.addAll(sInfo.answer);
            List answerList = new ArrayList(answerSet);
            questionAnswer.answer = answerList;
        } else if (publishedQuestion.type == QuestionType.TEXT) {
            TextSolutionInfo sInfo = (TextSolutionInfo) question.solutionInfo;
            if (sInfo.answer != null) {
                questionAnswer.answer.add(sInfo.answer);
            }
//        } else if (publishedQuestion.type == QuestionType.MATRIX) {
//            GridSolutionInfo sInfo = (GridSolutionInfo) question.solutionInfo;
//            questionAnswer.matrixAnswer = sInfo.gridAnswer;
//
//            if (publishedQuestion.options == null) {
//                publishedQuestion.options = new ArrayList<String>();
//            }
//
//            publishedQuestion.matrix = new HashMap<String, List<String>>();
//            publishedQuestion.matrix.put("A", sInfo.cola);
//            publishedQuestion.matrix.put("B", sInfo.colb);

        } else if (publishedQuestion.type == QuestionType.NUMERIC) {
            NumericSolutionInfo sInfo = (NumericSolutionInfo) question.solutionInfo;
            questionAnswer.answer = Arrays.asList(sInfo.answer);
        }
        if (questionAnswer.answer != null) {
            publishedQuestion.hasAns = true;
            logger.debug("Saving answer now ");
            answerRepo.save(questionAnswer);

            question.solutionInfo.globalAnsId = questionAnswer._getStringId();
            logger.debug("Saved answer" + questionAnswer._getStringId());

        }
        answerRepo.save(questionAnswer);
        return true;

    }

    public SrcEntity getGlobalEntity(String id) {

        Optional<CMDSQuestion> question = cmdsQuestionRepo.findById(id);
        return new SrcEntity(EntityType.QUESTION, question.get().globalQid);
    }

    public void processSolutionForPublishing(Solution solution, String questionSetId)
            throws VedantuException {

        if (CollectionUtils.isEmpty(solution.imgUuids)) {
            logger.debug("No images found for soultion " + solution._getStringId()
                    + " for question set id " + questionSetId);
            return;
        }
        for (String uuid : solution.imgUuids) {
            copySolutionImage(false, uuid, solution._getStringId(), questionSetId);
            // if (solution.content != null) {
            // LOGGER.debug(" Solution content before updating to global type format: "
            // + solution.content);
            // // solution.content = ImageDisplayURLUtil.getGlobalTypeImageFormat(solution.content,
            // // uuid);
            // LOGGER.debug(" Solution content after updating to global type format: "
            // + solution.content);
            // }
        }
    }

    public void copySolutionImage(boolean isQuestion, String uuid, String entityId,
                                  String questionSetId) throws VedantuException {

      /*  SolutionEntityFileStorage solutionStorage = new SolutionEntityFileStorage();
        CMDSQuestionEntityFileStorage questionStorge = new CMDSQuestionEntityFileStorage();

        String originalUUIDImageName = AbstractEntityFileStorage.computeFileId(uuid,
                EntityType.CMDSQUESTION, ImageUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, ImageSize.ORIGINAL);

        String destinationlUUIDImageName = AbstractEntityFileStorage.computeFileId(uuid,
                EntityType.SOLUTION, ImageUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, ImageSize.ORIGINAL);

        try {

            solutionStorage.copy(questionStorge, originalUUIDImageName, destinationlUUIDImageName);
        } catch (EntityFileStorageException e) {
            LOGGER.error("File Storage Exception", e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_PUBLISH_QUESTION,
                    "solution image moving failed");
        }

        return;
        */

        // // copy image to global directory
        // try {
        // CMDSQuestionEntityFileStorage storage =
        // (CMDSQuestionEntityFileStorage)
        // EntityStorageFactory.INSTANCE.get(EntityType.CMDSQUESTION);
        // // String storage.computeDisplayUrlComponent(uuid,
        // ImageUtils.JPG_EXTENTION_WITHOUT_DOT,MediaType.IMAGE,
        // FileCategory.CONVERTED, ImageSize.ORIGINAL);
        // String fileName = storage.computeFileId(uuid,
        // ImageUtils.JPG_EXTENTION_WITHOUT_DOT,MediaType.IMAGE,
        // FileCategory.CONVERTED, ImageSize.ORIGINAL);
        //
        //
        // AbstractEntityFileStorage entityStorage = null;
        // Map<String, String> infoTags = new HashMap<String, String>();
        // if (isQuestion) {
        // entityStorage = new QuestionEntityFileStorage();
        // infoTags.put(QuestionConstants.QID, entityId);
        // } else {
        // entityStorage = new SolutionEntityFileStorage();
        // infoTags.put(QuestionConstants.SOL_ID, entityId);
        // }
        // String newFileName = entityStorage.computeFileId(uuid,
        // ImageUtils.JPG_EXTENTION_WITHOUT_DOT,MediaType.IMAGE,
        // FileCategory.CONVERTED, ImageSize.ORIGINAL);
        // // LOGGER.debug("Storing image : " + imageFile.getAbsolutePath() +
        // " as " + uuid);
        // StorageResult result = entityStorage.copy(storage, fileName,
        // newFileName);
        // // StorageResult result = entityStorage.storeImage(uuid,imageFile,
        // FileCategory.CONVERTED, ImageSize.ORIGINAL,
        // // infoTags);
        // LOGGER.debug("Stored : " + fileName + " as " + result.fileId);
        // } catch (EntityFileStorageException e) {
        // LOGGER.error(e.getMessage(), e);
        // }

    }

    public AddChallengeRes publishCMDSQuestionAsChallenge(PublishQuestionAsChallengeReq request)
            throws VedantuException {

        AddChallengeRes response = new AddChallengeRes();

        for (SrcEntity questionEntity : request.entities) {
            if (questionEntity.type == EntityType.CMDSQUESTION) {
                CMDSQuestion cmdsQuestion = getQuestionById(questionEntity.id);
                Question globalQuestion = publish(request.userId,
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
                        hints.add(!StringUtils.isEmpty(hInfo.newText) ? hInfo.newText
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
                AddChallengeRes addChallRes = challengeServiceModule.addChallenge(addChallReq);
                return addChallRes;
            }
        }
        return response;
    }

    public CMDSQuestion getQuestionById(String questionId) throws VedantuException {

        Optional<CMDSQuestion> question = cmdsQuestionRepo.findById(questionId);
        if (null == question) {
            logger.error("cannot find cmds question for _id: " + questionId);
            throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
        }

        return question.get();
    }

    public GetCMDSQuestionRes getQuestion(GetCMDSQuestionReq request) throws VedantuException {

        GetCMDSQuestionRes response = new GetCMDSQuestionRes();

        response.info = (CMDSQuestionInfo) getBasicInfo(request.id, VedantuRecordState.ACTIVE);
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
                        if (attachment.entity.type == EntityType.CMDSTEST) {
                            attachmentInfo.info = cmdsModuleManager.getBasicCMDSTestInfo(attachment.entity.id);

                        } else if (attachment.entity.type == EntityType.CMDSVIDEO) {

                            attachmentInfo.info = cmdsModuleManager.getBasicCMDSVideoInfo(attachment.entity.id);

                        } else if (attachment.entity.type == EntityType.CMDSQUESTION) {
                            attachmentInfo.info = cmdsModuleManager.getBasicCMDSQuestionInfo(attachment.entity.id);
                        }
                        //  attachmentInfo.info = getBasicInfo(attachment.entity.id);
                        attachmentsInfo.add(attachmentInfo);
                    }
                    solution.attachmentsInfo = attachmentsInfo;

                }

            }
        }
        return response;

    }

    public ModelBasicInfo getBasicInfo(String id, VedantuRecordState state) {

        logger.debug("Creating basic info for question Id : " + id);

        CMDSQuestion question = cmdsQuestionRepo.findByIdAndRecordState(id, state);
        if (question == null) {
            logger.debug(" No question found for id " + id + "  " + state);
            return null;
        }
        CMDSQuestionInfo info = (CMDSQuestionInfo) question.toBasicInfo();
        logger.debug("Created basic info for question  : " + info);
        return info;
    }

    public GetCMDSQuestionSearchRes getQuestions(GetQuestionsReq getQuestionsReq)
            throws VedantuException {

        logger.debug("......inside getQuestions.........");
        GetCMDSQuestionSearchRes response = new GetCMDSQuestionSearchRes();

        boolean showParaCount = false;
        boolean showNonParaCount = false;
        List<String> types = new ArrayList<String>();
        List<String> difficulty = new ArrayList<String>();
        List<String> scope = new ArrayList<String>();
        SearchListResponse<CMDSQuestionSearchIndexDetails> results = null;
        if (CollectionUtils.isEmpty(getQuestionsReq.includeDifficulty)) {
            difficulty.add("EASY");
            difficulty.add("MODERATE");
            difficulty.add("TOUGH");
            getQuestionsReq.includeDifficulty = difficulty;
        }
        String learnpediaId = learnPediaId;
        if (!getQuestionsReq.orgId.equals(learnpediaId)) {
            if (!cmdsResourcesManager.includeLearnpediaQuestions(learnpediaId, getQuestionsReq.orgId)) {
                scope.add("org");
                getQuestionsReq.scope = scope;
            }
        }
        if (getQuestionsReq.quesType.equals("PARA_QUES")) {
            results = getParagraphQuestionsInfo(getQuestionsReq);
            showParaCount = true;
            showNonParaCount = true;
        } else {
            if (getQuestionsReq.quesType.equals("NOT_PARA")) {
                if (CollectionUtils.isEmpty(getQuestionsReq.includeTypes)) {
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
        if (showParaCount) {
            types.clear();
            types.add("TEXT");
            getQuestionsReq.includeTypes = types;
            SearchListResponse<CMDSQuestionSearchIndexDetails> paraResults = getEntityInfos(
                    getQuestionsReq, EntityType.CMDSQUESTION, CMDSQuestionSearchIndexDetails.class,
                    null);
            response.paraHits = paraResults.totalHits;
        }
        if (showNonParaCount) {
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
            logger.debug("......inside outer for loop.........");
            CMDSQuestionInfo questionInfo = ((CMDSQuestionInfo) getBasicInfo(details.id, VedantuRecordState.ACTIVE));

            // questionInfo = (CMDSQuestionInfo) CMDSQuestionDAO.INSTANCE.getBasicInfo(details.id);
            if (questionInfo == null)
                continue;
            if (questionInfo.detail != null && questionInfo.detail.solutionInfo != null
                    && CollectionUtils.isNotEmpty(questionInfo.detail.solutionInfo.solutions)) {
                for (SolutionFormat solution : questionInfo.detail.solutionInfo.solutions) {
                    if (solution.attachments != null) {
                        List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
                        for (Attachment attachment : solution.attachments) {
                            logger.debug("......inside for loop.........");
                            AttachmentInfo attachmentInfo = new AttachmentInfo();
                            attachmentInfo.entity = attachment.entity;
                            // attachmentInfo.info
                            // VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(attachment.entity.type);
                            attachmentInfo.info = getBasicInfo(attachment.entity.id, VedantuRecordState.ACTIVE);
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

    private <T extends IListResponseObj> SearchListResponse<T> getParagraphQuestionsInfo(
            GetQuestionsReq getQuestionsReq) {
        logger.debug("Inside getParagraphQuestionsInfo which retrieves para questions of particular paragraph");

        CMDSQuestion paragraphQuestion = new CMDSQuestion();
        try {
            paragraphQuestion = getQuestionById(getQuestionsReq.paraId);
        } catch (VedantuException e) {
            logger.debug("Exception while retreiveing data : " + e.getMessage());
        }
        List<String> paraQuesIds = paragraphQuestion.paraIds;
        if (paraQuesIds.size() > 0) {
            // AbstractFacetBuilder[] facets = ElasticSearchUtils.getBoardsTagFacets(getQuestionsReq.size);
            return (SearchListResponse<T>) getBasicInfoOFParaQuestionFromESSearch(paraQuesIds,
                    CMDSQuestionSearchIndexDetails.class);
        } else {
            return new SearchListResponse<T>();
        }
    }

    protected <T extends IListResponseObj> SearchListResponse<T> getBasicInfoOFParaQuestionFromESSearch(
            List<String> qIds, Class<T> respObj) {

      /*  if (qIds == null || qIds.size() == 0) {
            logger.error("empty search response for getting paragraph questions : ");
            return new SearchListResponse<T>();
        }
        logger.debug("Ready to get Para question for ES resources");
        // ES query
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("content.id").is(new String[qIds.size()]);

        SearchResponse response = ElasticSearchUtils.getSearchResponse(query, "timeCreated",
                "desc", 0, qIds.size(), EntityType.CMDSRESOURCE.getIndexName(),
                EntityType.CMDSRESOURCE.getIndexType().toLowerCase(), null, false,
                (AbstractFacetBuilder[]) null);
        if (response == null || response.getHits().getTotalHits() == 0) {
            logger.error("empty search response for ES query : ");
            return new SearchListResponse<T>();
        }
        logger.debug(" Search responses " + response.getHits());
        SearchListResponse<T> listResponse = new SearchListResponse<T>();
        SearchHits allHits = response.getHits();
        long totalHits = allHits.getTotalHits();
        logger.debug("totalHits: " + totalHits);
        for (SearchHit hits : allHits.getHits()) {
            logger.trace("hits : " + hits.sourceAsString());
            Map<String,Object> sourceAsMap = hits.sourceAsMap();
            logger.debug("source as MAp : "+sourceAsMap.getClass().getName());
            CMDSQuestion paraQuestion = null;
            try {
                paraQuestion = getQuestionById(((Map<String,Object>)sourceAsMap.get("content")).get("id").toString());
            } catch (VedantuException e) {
                logger.debug("Exception while retreiveing data : "+e.getMessage());
            }
            catch (Exception ex){
                logger.debug("Exception while converting MAP : "+ex.getMessage());
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

        listResponse.totalHits = qIds.size();
        return listResponse;*/
        return null;
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

    private GetUsageRes getUsage(String id, EntityType containerType, int start, int size,
                                 VedantuRecordState state) throws VedantuException {

        AtomicLong totalHits = new AtomicLong();
        GetUsageRes response = new GetUsageRes();

        //   VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(containerType);
        if (containerType == EntityType.CMDSQUESTIONSET) {
            CMDSQuestion cmdsQuestion = new CMDSQuestion();
            List<? extends VedantuBaseMongoModel> modelList = getContainers(id, start,
                    size, state, totalHits);
            for (VedantuBaseMongoModel model : modelList) {

                response.data.list.add(model.toBasicInfo());
            }
            response.data.totalHits = totalHits.longValue();
        }


        return response;
    }

    public List<CMDSQuestionSet> getContainers(String id, int start, int size,
                                               VedantuRecordState state, AtomicLong totalHits) {

        if (id == null) {
            return null;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CMDSQuestionSet.QUESTION_IDS).in(id);
        //findQuery.field(CMDSQuestionSet.QUESTION_IDS).contains(id);
        if (state != null) {
            criteria.and(ConstantsGlobal.RECORD_STATE).equals(state);
        }
        List<CMDSQuestionSet> cmdsQuestionSetsList = mongoTemplate.find(query.addCriteria(criteria), CMDSQuestionSet.class);
        totalHits.set(cmdsQuestionSetsList.stream().count());
        return cmdsQuestionSetsList;

    }

    public GetSolutionsRes getSolutions(GetSolutionsReq req) throws VedantuException {

        Optional<CMDSQuestion> question1 = cmdsQuestionRepo.findById(req.qId);
        if (!question1.isPresent()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "with given id cmdsQuestion is not prasent");
        }
        CMDSQuestion question = question1.get();
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
                    // VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(attachment.entity.type);
                    attachmentInfo.info = getBasicInfo(attachment.entity.id, VedantuRecordState.ACTIVE);
                    attachmentsInfo.add(attachmentInfo);
                }
                solution.attachmentsInfo = attachmentsInfo;

            }
        }

        return response;
    }

    public void fixMissingSolutions(String orgId, String scope, String cmdsQId) throws VedantuException {
        // TODO Auto-generated method stub
        List<CMDSQuestion> cmdsQuestions = getQuestionsByOrgIdAndScope(orgId, scope.toUpperCase(), cmdsQId);
        logger.debug("fixMissingSolutions : Total questions " + cmdsQuestions.size());
        int count = 1;
        boolean edited;
        Gson gson = new Gson();
        for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
            logger.debug("fixMissingSolutions : Current cmdsQuestion count " + count);
            edited = false;
            if (cmdsQuestion.published) {
                if (CollectionUtils.isNotEmpty(cmdsQuestion.solutionInfo.solutions)) {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has solutions");
                    Optional<Question> que1 = questionRepo.findById(cmdsQuestion.globalQid);
                    Question que = null;
                    if (que1.isPresent()) {
                        que = que1.get();
                    }
                    for (SolutionFormat sol : cmdsQuestion.solutionInfo.solutions) {
                        if (!StringUtils.isEmpty(sol.globalSolId)) {
                            logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has globalSolId");
                            //Implement validation
                            Optional<Solution> solu1 = solutionRepo.findById(sol.globalSolId);
                            Solution solu = null;
                            if (solu1.isPresent()) {
                                solu = solu1.get();
                            }
                            if (!solu.qId.equals(cmdsQuestion.globalQid)) {
                                logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has INVALID globalSolId");
                                String jsonString = gson.toJson(solu);
                                Solution newSolution = gson.fromJson(jsonString, Solution.class);
                                newSolution.qId = cmdsQuestion.globalQid;
                                newSolution.userId = cmdsQuestion.userId;
                                newSolution.id = null;
                                newSolution.timeCreated = que.timeCreated;
                                newSolution.lastUpdated = que.timeCreated;
                                solutionRepo.save(newSolution);
                                sol.globalSolId = newSolution._getStringId();
                                que.solutions++;
                                edited = true;
                            } else {
                                logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has VALID globalSolId");
                            }
                        } else {
                            logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has NO globalSolId");
                        }
                    }
                    if (edited) {
                        questionRepo.save(que);
                        generateEventAysc(que.userId, que, UserActionType.EventActionType.UPDATE,
                                EventType.INDEX_QUESTION, UserActionType.UPDATED, false);
                        cmdsQuestionRepo.save(cmdsQuestion);
                        generateEventAysc(cmdsQuestion.userId, cmdsQuestion, UserActionType.EventActionType.UPDATE,
                                EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
                    }
                } else {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is published of type " + cmdsQuestion.type + " has NO solutions");
                }
            } else {
                if (CollectionUtils.isNotEmpty(cmdsQuestion.solutionInfo.solutions)) {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has solutions");
                    for (SolutionFormat sol : cmdsQuestion.solutionInfo.solutions) {
                        if (!StringUtils.isEmpty(sol.globalSolId)) {
                            logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has globalSolId");
                            sol.globalSolId = null;
                            edited = true;
                        } else {
                            logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has NO globalSolId");
                        }
                    }
                } else {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has NO solutions");
                }
                if (!StringUtils.isEmpty(cmdsQuestion.solutionInfo.globalAnsId)) {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has globalAnsId");
                    cmdsQuestion.solutionInfo.globalAnsId = null;
                    edited = true;
                } else {
                    logger.debug("fixMissingSolutions : This cmds question " + cmdsQuestion._getStringId() + " which is unpublished of type " + cmdsQuestion.type + " has NO globalAnsId");
                }
                if (edited) {
                    cmdsQuestionRepo.save(cmdsQuestion);
                    generateEventAysc(cmdsQuestion.userId, cmdsQuestion, UserActionType.EventActionType.UPDATE,
                            EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
                }
            }
            count++;
        }
    }

    public List<CMDSQuestion> getQuestionsByOrgIdAndScope(String orgId, String scope, String cmdsQId) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if (!StringUtils.isEmpty(cmdsQId)) {
            criteria.and("_id").is(new ObjectId(cmdsQId));
        }
        criteria.and("contentSrc.type").is(EntityType.ORGANIZATION);
        criteria.and("contentSrc.id").is(orgId);
        criteria.and("recordState").is(VedantuRecordState.ACTIVE);
        criteria.and("scope").is(scope);
        query.addCriteria(criteria);
        List<CMDSQuestion> questions = mongoTemplate.find(query, CMDSQuestion.class);
        return questions;
    }

    public void fixBoards(String orgId, String boardId) {
//        List<String> types = new ArrayList<String>();
//        types.add("SCQ");
//        types.add("MCQ");
//        types.add("NUMERIC");
//        LOGGER.debug("fixBoards : types are "+types.toString());
        String learnpediaId = learnPediaId;
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.SCOPE).is(Scope.PRIVATE.name());
        criteria.and(ConstantsGlobal.RECORD_STATE).is(VedantuRecordState.ACTIVE.name());
        criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_ID).is(orgId);
        criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_TYPE).is(EntityType.ORGANIZATION.name());
//        query.put(ConstantsGlobal.TYPE, new BasicDBObject(MongoManager.IN_QUERY, types));
        List<String> courseBoardsTofix = new ArrayList<String>();
        Map<String, String> parentToSharedBoardMap = new HashMap<String, String>();
        Map<String, String> sharedToParentBoardMap = new HashMap<String, String>();
        BoardMapping boardMapping = getBySharedToOrgId(learnpediaId, orgId);
        if (boardMapping == null) {
            logger.debug("fixBoards : There are no board mappings found for this orgId");
            return;
        }
        List<BoardMappings> boardMappings = boardMapping.boardMappings;
        for (BoardMappings boardMap : boardMappings) {
            if (boardMap.boardType.equals(BoardType.COURSE.name())) {
                //parentCourseBoards.add(boardMap.parentBoardId);
            }
            parentToSharedBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
            sharedToParentBoardMap.put(boardMap.sharedToBoardId, boardMap.parentBoardId);
        }
        courseBoardsTofix.add(boardId);
        logger.debug("fixBoards : parent course boards are " + courseBoardsTofix.toString());
        criteria.and(ConstantsGlobal.BOARD_IDS).in(courseBoardsTofix);
        logger.debug("fixBoards : Before getting count of all corrupted questions, Query");
        query.addCriteria(criteria);
        List<CMDSQuestion> cmdsQuestions = mongoTemplate.find(query, CMDSQuestion.class);
        logger.debug("fixBoards : After getting count of all corrupted questions, Query");
        logger.debug("fixBoards : Total count of all corrupted questions from Query is " + cmdsQuestions.stream().count());
        Iterator<CMDSQuestion> cmdsQuestionsIterator = cmdsQuestions.iterator();
        EditQuestionReq request = new EditQuestionReq();
        request.updateList.add("brdIds");
        SrcEntity entity = new SrcEntity();
        entity.type = EntityType.CMDSQUESTION;
        int count = 1;
        while (cmdsQuestionsIterator.hasNext()) {
            CMDSQuestion cmdsQuestion = cmdsQuestionsIterator.next();
            entity.id = cmdsQuestion._getStringId();
            logger.debug("fixBoards : Fixing " + count + " cmdsQuestion " + cmdsQuestion._getStringId());
            request.entity = entity;
            List<SrcEntityEdit> editEntities = new ArrayList<SrcEntityEdit>();
            // Get edit entities
            List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(cmdsQuestion._getStringId());
            Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                    associatedContentsList);
            logger.debug("fixBoards : ..........AssociatedContentsList.........." + associatedContentsList);
            logger.debug("fixBoards : ..........AssociatedContents.........." + associatedContents);

            for (SrcEntityPublishableState associatedContent : associatedContentsList) {
                SrcEntityEdit editEntity = new SrcEntityEdit();
                editEntity.entity = associatedContent.entity;
                editEntity.editType = EntityEditType.RIGHT_AWAY;
                editEntities.add(editEntity);
            }
            logger.info("fixBoards : AssociatedContentCount" + editEntities);

            request.editEntities = editEntities;
            request.orgId = cmdsQuestion.contentSrc.id;
            request.userId = cmdsQuestion.userId;
            request.brdIds.clear();
            request.brdIds.addAll(getFixedBoardIds(cmdsQuestion.boardIds, parentToSharedBoardMap));
            request.callingUserId = cmdsQuestion.userId;
            request.callingApp = "cmds-app";
            request.callingAppId = "cmds-app";
            try {
                logger.debug("fixBoards : Editing shared " + orgId + " question " + cmdsQuestion._getStringId());
                update(request);
                logger.debug("fixBoards : Edited shared " + orgId + " question " + cmdsQuestion._getStringId());
            } catch (VedantuException e) {
                logger.debug("Error while Editing shared " + orgId + " Question " + cmdsQuestion._getStringId() + " with message " + e.getMessage());
            }
            count++;
        }
    }

    public BoardMapping getBySharedToOrgId(String parentOrgId, String sharedToOrgId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!parentOrgId.equals("N.A")) {
            criteria.and("parentOrgId").is(parentOrgId);
        }
        criteria.and("sharedToOrgId").is(sharedToOrgId);
        query.addCriteria(criteria);
        List<BoardMapping> mapping = mongoTemplate.find(query, BoardMapping.class);
        return mapping.get(0);
    }

    private Set<String> getFixedBoardIds(Set<String> boardIds,
                                         Map<String, String> parentToSharedBoardMap) {
        // TODO Auto-generated method stub
        Set<String> newBoards = new HashSet<String>();
        for (String brdId : boardIds) {
            newBoards.add(parentToSharedBoardMap.get(brdId));
        }
        return newBoards;
    }

    public void fixBoardMappings(String orgId) {

        String learnpediaId = learnPediaId;
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.SCOPE).is(Scope.PRIVATE.name());
        criteria.and(ConstantsGlobal.RECORD_STATE).is(VedantuRecordState.ACTIVE.name());
        criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_ID).is(orgId);
        criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_TYPE).is(EntityType.ORGANIZATION.name());
        //  criteria.and(ConstantsGlobal.BOARD_IDS).is(new BasicDBObject(MongoManager.SIZE, 1));
        logger.debug("fixBoardMappings  :: Before Getting Quextions Count to Fix ");
        List<CMDSQuestion> cmdsQuestions = mongoTemplate.find(query.addCriteria(criteria), CMDSQuestion.class);
        logger.debug("fixBoardMappings  :: After Getting Quextions Count to Fix ");
        logger.debug("fixBoardMappings Total Questions to Fix : " + cmdsQuestions.stream().count());
        logger.debug("fixBoardMappings Getting all parentToShared board mappings...");
        Map<String, String> parentToSharedBoardMap = new HashMap<String, String>();
        BoardMapping boardMapping = getBySharedToOrgId(learnpediaId, orgId);
        if (boardMapping == null) {
            logger.debug("fixBoards : There are no board mappings found for this orgId");
            return;
        }
        List<BoardMappings> boardMappings = boardMapping.boardMappings;
        for (BoardMappings boardMap : boardMappings) {
            parentToSharedBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
        }
        logger.debug("fixBoardMappings Got all parentToShared board mappings. Whoz count is " + parentToSharedBoardMap.size());
        Iterator<CMDSQuestion> cmdsQuestionsIterator = cmdsQuestions.iterator();
        EditQuestionReq request = new EditQuestionReq();
        request.updateList.add("brdIds");
        SrcEntity entity = new SrcEntity();
        entity.type = EntityType.CMDSQUESTION;
        int count = 1;
        while (cmdsQuestionsIterator.hasNext()) {
            CMDSQuestion cmdsQuestion = cmdsQuestionsIterator.next();
            entity.id = cmdsQuestion._getStringId();
            logger.debug("fixBoardMappings : Fixing " + count + " cmdsQuestion " + cmdsQuestion._getStringId());
            request.entity = entity;
            List<SrcEntityEdit> editEntities = new ArrayList<SrcEntityEdit>();
            // Get edit entities
            List<SrcEntityPublishableState> associatedContentsList = getAssociatedContent(cmdsQuestion._getStringId());
            Set<SrcEntityPublishableState> associatedContents = new HashSet<SrcEntityPublishableState>(
                    associatedContentsList);
            logger.debug("fixBoardMappings : ..........AssociatedContentsList.........." + associatedContentsList);
            logger.debug("fixBoardMappings : ..........AssociatedContents.........." + associatedContents);

            for (SrcEntityPublishableState associatedContent : associatedContentsList) {
                SrcEntityEdit editEntity = new SrcEntityEdit();
                editEntity.entity = associatedContent.entity;
                editEntity.editType = EntityEditType.RIGHT_AWAY;
                editEntities.add(editEntity);
            }
            logger.info("fixBoardMappings : AssociatedContentCount" + editEntities);
            request.editEntities = editEntities;
            request.orgId = cmdsQuestion.contentSrc.id;
            request.userId = cmdsQuestion.userId;
            request.brdIds.clear();
            request.brdIds.addAll(getFixedBoardIds(cmdsQuestion.parentQId, parentToSharedBoardMap));
            request.callingUserId = cmdsQuestion.userId;
            request.callingApp = "cmds-app";
            request.callingAppId = "cmds-app";
            try {
                logger.debug("fixBoardMappings : Editing shared " + orgId + " question " + cmdsQuestion._getStringId());
                logger.debug("fixBoardMappings : LOGGING REQUEST Before : " + cmdsQuestion.boardIds.toString() + " After : " + request.brdIds.toString());
                if (!request.brdIds.isEmpty()) {
                    update(request);
                } else {
                    logger.error("fixBoardMappings : Corresponding Board Id Is Empty");
                }
                logger.debug("fixBoardMappings : Edited shared " + orgId + " question " + cmdsQuestion._getStringId());
            } catch (VedantuException e) {
                logger.debug("fixBoardMappings : Error while Editing shared " + orgId + " Question " + cmdsQuestion._getStringId() + " with message " + e.getMessage());
            }
            count++;
        }
    }

    private Set<String> getFixedBoardIds(String parentCmdsQuestionId,
                                         Map<String, String> parentToSharedBoardMap) {
        // TODO Auto-generated method stub
        Set<String> newBoards = new HashSet<String>();
        Optional<CMDSQuestion> cmdsQuestion = cmdsQuestionRepo.findById(parentCmdsQuestionId);
        for (String brdId : cmdsQuestion.get().boardIds) {
            newBoards.add(parentToSharedBoardMap.get(brdId));
        }
        return newBoards;
    }

}

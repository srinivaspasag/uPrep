package com.lms.maintenance.managers;

import com.google.gson.Gson;
import com.lms.cmds.SolutionInfo;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.component.CMDSModuleManager;
import com.lms.component.CMDSQuestionManager;
import com.lms.enums.QuestionType;
import com.lms.models.CMDSFolder;
import com.lms.models.CMDSQuestion;
import com.lms.question.SolutionFormat;
import com.lms.repository.CMDSQuestionRepo;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class ShareQuestionsThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CMDSModuleManager.class);
    private final String jobId;
    private Thread t;
    @Autowired
    private EntityOperationStatusRepo entityOperationStatusRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CMDSQuestionManager cmdsQuestionManager;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;

    public ShareQuestionsThread(String jobId) {
        this.jobId = jobId;
        logger.debug("ShareQuestionsThread : Creating Job Id " + jobId);
    }

    private static SolutionInfo removeGlobalAnswerIdSolutionId(SolutionInfo solutionInfo) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(solutionInfo);
        SolutionInfo newSolutionInfo = gson.fromJson(jsonString, solutionInfo.getClass());
        newSolutionInfo.globalAnsId = null;
        for (SolutionFormat solution : newSolutionInfo.solutions) {
            solution.globalSolId = null;
        }
        return newSolutionInfo;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        String key = "";
        String value = "";
        while (keysItr.hasNext()) {
            key = keysItr.next();
            value = object.getString(key);
            map.put(key, value);
        }
        return map;
    }

    private static Set<String> getNewBoards(Map<String, String> toBeChangedToBoardIds, Set<String> orginalBoardIds) {
        logger.debug("ShareQuestionsThread : Inside getNewBoards");
        Set<String> boardIds = new HashSet<String>();
        for (String brdId : orginalBoardIds) {
            if (toBeChangedToBoardIds.containsKey(brdId)) {
                boardIds.add(toBeChangedToBoardIds.get(brdId));
            } else {
                logger.debug(
                        "ShareQuestionsThread : Corresponding shared board mapping not found for board id " + brdId);
            }
        }
        logger.debug("ShareQuestionsThread : Getting out of getNewBoards");
        return boardIds;
    }

    public void run() {
        logger.debug("ShareQuestionsThread : Running " + jobId);
        EntityOperationStatus job = getEntityById(jobId);
        try {
            if (job.numOfSteps > 0 && job.oType == OperationType.CMDS_QUESTION_SHARING) {
                logger.debug("ShareQuestionsThread : About to share questions");
                JSONObject info = new JSONObject(job.message);
                String QType = info.getString("QType");
                if (QType.equals("SCQ")) {
                    shareQuestions(job, info);
                } else if (QType.equals("MCQ")) {
                    shareQuestions(job, info);
                } else if (QType.equals("MATRIX")) {
                    shareQuestions(job, info);
                } else if (QType.equals("NUMERIC")) {
                    shareQuestions(job, info);
                } else if (QType.equals("TEXT")) {
                    shareTEXTQuestions(job, info);
                } else if (QType.equals("PARA")) {
                    sharePARAQuestions(job, info);
                }
            } else {
                logger.debug(
                        "ShareQuestionsThread : UnSupported Operation Type or Number Of Steps less than 0 for Job Id "
                                + jobId);
            }
        } catch (JSONException e) {
            logger.debug("ShareQuestionsThread : JSONException Occured while Running Job Id " + jobId + e.getMessage());
        } catch (VedantuException e) {
            logger.debug(
                    "ShareQuestionsThread : VedantuException Occured while Running Job Id " + jobId + e.getMessage());
        }
    }

    private void shareQuestions(EntityOperationStatus job, JSONObject info) throws JSONException, VedantuException {
        logger.debug("ShareQuestionsThread : About to share " + info.getString("QType") + " questions for jobId "
                + job._getStringId() + " to Organsiation " + info.getString("sharedToOrgId"));
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if (info.getString("QType").equals("SCQ")) {
            types.add(QuestionType.SCQ.toString());
        } else if (info.getString("QType").equals("MCQ")) {
            types.add(QuestionType.MCQ.toString());
        } else if (info.getString("QType").equals("MATRIX")) {
            types.add(QuestionType.MATRIX.toString());
        } else if (info.getString("QType").equals("NUMERIC")) {
            types.add(QuestionType.NUMERIC.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        CMDSQuestion sharedCmdsQues = new CMDSQuestion();
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while (totalQuestionsToMap > 0) {
            logger.debug("ShareQuestionsThread : start is : " + start + " size is : " + size + " for query w.r.t jobId "
                    + job._getStringId() + " to Organsiation " + info.getString("sharedToOrgId"));
            cmdsQuestions = getQuestionsByBoard(info.getString("parentOrgId"),
                    info.getString("sharedToOrgId"), boardIds, types, start, size);
            logger.debug("ShareQuestionsThread : Documents retrieved from above query is : " + cmdsQuestions.size()
                    + " for jobId " + job._getStringId() + " to Organsiation " + info.getString("sharedToOrgId"));
            if (cmdsQuestions.size() == 0) {
                logger.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }

            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of Question " + count
                        + "   ::::::::::::::::" + " for jobId " + job._getStringId() + " to Organsiation "
                        + info.getString("sharedToOrgId"));
                logger.debug("ShareQuestionsThread : Board Map is Built for organisation "
                        + info.getString("sharedToOrgId"));
                sharedCmdsQues = createAndSaveNewSharedCMDSQuestionCopy(cmdsQuestion, boardIdsMap,
                        info.getString("sharedToOrgUserId"), info.getString("sharedToOrgId"));
                logger.debug(
                        "ShareQuestionsThread : Question shared and ready to map that question in parent question for organisation "
                                + info.getString("sharedToOrgId"));
                cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQues._getStringId());
                cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
                cmdsQuestionRepo.save(cmdsQuestion);
                logger.debug(
                        "ShareQuestionsThread : Succesfully mapped shared question details in parent question for organisation "
                                + info.getString("sharedToOrgId"));
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                entityOperationStatusRepo.save(job);
                logger.debug("ShareQuestionsThread : Job saved successfully for organisation "
                        + info.getString("sharedToOrgId"));
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId() + " to Organsiation " + info.getString("sharedToOrgId"));
            }
            cmdsQuestions.clear();
            start = start + size;
        }
        logger.debug("ShareQuestionsThread : Total " + info.getString("QType") + " questions shared is " + (count - 1)
                + " for jobId " + job._getStringId() + " to Organsiation " + info.getString("sharedToOrgId"));
    }

    public CMDSQuestion createAndSaveNewSharedCMDSQuestionCopy(CMDSQuestion questionOriginal,
                                                               Map<String, String> boardIdsMap, String userId, String orgId) throws VedantuException {
        logger.debug("ShareQuestionsThread : Inside createAndSaveNewSharedCMDSQuestionCopy");
        String folderId = getRootFolder(orgId)._getStringId();
        CMDSQuestion question = new CMDSQuestion();
        question.boardIds = getNewBoards(boardIdsMap, questionOriginal.boardIds);
        question.completed = questionOriginal.completed;
        question.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        question.hints = questionOriginal.hints;
        question.difficulty = questionOriginal.difficulty;
        question.name = questionOriginal.name;
        // question.targetIds = questionOriginal.targetIds;
        question.lastUpdated = System.currentTimeMillis();
        question.origRefNo = questionOriginal.origRefNo;
        question.questionBody = questionOriginal.questionBody;
        question.recordState = questionOriginal.recordState;
        question.scope = Scope.PRIVATE;
        question.solutionInfo = questionOriginal.published
                ? removeGlobalAnswerIdSolutionId(questionOriginal.solutionInfo)
                : questionOriginal.solutionInfo;
        question.source = questionOriginal.source;
        question.status = questionOriginal.status;
        question.tags = questionOriginal.tags;
        question.timeCreated = System.currentTimeMillis();
        question.type = questionOriginal.type;
        question.userId = userId;
        question.parentQId = questionOriginal._getStringId();
        try {
            logger.debug("ShareQuestionsThread : Before calling addQuestion");
            question = cmdsQuestionManager.addQuestion(orgId, userId, folderId, question);
            logger.debug("ShareQuestionsThread : After calling addQuestion");
        } catch (Exception e) {
            logger.debug("ShareQuestionsThread : Exception occured while creating new shared cmdsquestion copy "
                    + e.getMessage());
        }
        return question;
    }

    public CMDSQuestion createAndSaveNewPARASharedCMDSQuestionCopy(CMDSQuestion questionOriginal,
                                                                   Map<String, String> boardIdsMap, String userId, String orgId, CMDSQuestion sharedCmdsQuesTEXT)
            throws VedantuException {
        logger.debug("ShareQuestionsThread : Inside createAndSaveNewSharedCMDSQuestionCopy");
        String folderId = getRootFolder(orgId)._getStringId();
        CMDSQuestion question = new CMDSQuestion();
        question.paragraphId = sharedCmdsQuesTEXT._getStringId();
        question.boardIds = getNewBoards(boardIdsMap, questionOriginal.boardIds);
        question.completed = questionOriginal.completed;
        question.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        question.hints = questionOriginal.hints;
        question.difficulty = questionOriginal.difficulty;
        question.name = questionOriginal.name;
        // question.targetIds = questionOriginal.targetIds;
        question.lastUpdated = System.currentTimeMillis();
        question.origRefNo = questionOriginal.origRefNo;
        question.questionBody = questionOriginal.questionBody;
        question.recordState = questionOriginal.recordState;
        question.scope = Scope.PRIVATE;
        question.solutionInfo = questionOriginal.published
                ? removeGlobalAnswerIdSolutionId(questionOriginal.solutionInfo)
                : questionOriginal.solutionInfo;
        question.source = questionOriginal.source;
        question.status = questionOriginal.status;
        question.tags = questionOriginal.tags;
        question.timeCreated = System.currentTimeMillis();
        question.type = questionOriginal.type;
        question.userId = userId;
        question.parentQId = questionOriginal._getStringId();
        try {
            logger.debug("ShareQuestionsThread : Before calling addQuestion");
            question = cmdsQuestionManager.addQuestion(orgId, userId, folderId, question);
            logger.debug("ShareQuestionsThread : After calling addQuestion");
        } catch (Exception e) {
            logger.debug("ShareQuestionsThread : Exception occured while creating new shared cmdsquestion copy "
                    + e.getMessage());
        }
        return question;
    }

    private void shareTEXTQuestions(EntityOperationStatus job, JSONObject info) throws JSONException, VedantuException {
        logger.debug("ShareQuestionsThread : About to share " + info.getString("QType") + " questions for jobId "
                + job._getStringId());
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if (info.getString("QType").equals("TEXT")) {
            types.add(QuestionType.TEXT.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while (totalQuestionsToMap > 0) {
            logger.debug("ShareQuestionsThread : start is : " + start + " size is : " + size + " for jobId "
                    + job._getStringId());
            cmdsQuestions = getQuestionsByBoard(info.getString("parentOrgId"),
                    info.getString("sharedToOrgId"), boardIds, types, start, size);
            logger.debug("ShareQuestionsThread : Documents retrieved from above query is : " + cmdsQuestions.size()
                    + " for jobId " + job._getStringId());
            if (cmdsQuestions.size() == 0) {
                logger.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }
            CMDSQuestion sharedCmdsQuesTEXT = new CMDSQuestion();
            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of TEXT Question " + count
                        + "   ::::::::::::::::" + " for jobId " + job._getStringId());
                sharedCmdsQuesTEXT = createAndSaveNewSharedCMDSQuestionCopy(cmdsQuestion, boardIdsMap,
                        info.getString("sharedToOrgUserId"), info.getString("sharedToOrgId"));
                cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQuesTEXT._getStringId());
                cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
                cmdsQuestionRepo.save(cmdsQuestion);
                sharePARAQuestions(cmdsQuestion, sharedCmdsQuesTEXT, boardIdsMap, info);
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                entityOperationStatusRepo.save(job);
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId());
            }
            cmdsQuestions.clear();
            start = start + size;
        }
        logger.debug("ShareQuestionsThread : Total " + info.getString("QType") + " questions shared is " + (count - 1)
                + " for jobId " + job._getStringId());
    }

    private void sharePARAQuestions(EntityOperationStatus job, JSONObject info) throws JSONException, VedantuException {
        logger.debug("ShareQuestionsThread : About to share " + info.getString("QType") + " questions for jobId "
                + job._getStringId());
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if (info.getString("QType").equals("PARA")) {
            types.add(QuestionType.PARA.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        CMDSQuestion originalCmdsText = new CMDSQuestion();
        CMDSQuestion sharedCmdsQuesTEXT = new CMDSQuestion();
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while (totalQuestionsToMap > 0) {
            logger.debug("ShareQuestionsThread : start is : " + start + " size is : " + size + " for jobId "
                    + job._getStringId());
            cmdsQuestions = getQuestionsByBoard(info.getString("parentOrgId"),
                    info.getString("sharedToOrgId"), boardIds, types, start, size);
            logger.debug("ShareQuestionsThread : Documents retrieved from above query is : " + cmdsQuestions.size()
                    + " for jobId " + job._getStringId());
            if (cmdsQuestions.size() == 0) {
                logger.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }
            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of PARA Question " + count
                        + "   ::::::::::::::::" + " for jobId " + job._getStringId());
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                entityOperationStatusRepo.save(job);
                if (StringUtils.isEmpty(cmdsQuestion.paragraphId)) {
                    continue;
                }
                originalCmdsText = getCMDSQuestionById(cmdsQuestion.paragraphId);
                if (originalCmdsText == null) {
                    continue;
                }
                sharedCmdsQuesTEXT = getSharedCmdsQuesText(originalCmdsText, info.getString("sharedToOrgId"));
                if (sharedCmdsQuesTEXT == null) {
                    continue;
                }
                sharePARAQuestions(originalCmdsText, sharedCmdsQuesTEXT, boardIdsMap, info);
                logger.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId());
            }
            cmdsQuestions.clear();
            start = start + size;
        }
    }

    private CMDSQuestion getSharedCmdsQuesText(CMDSQuestion originalCmdsText, String orgId) {
        if (CollectionUtils.isEmpty(originalCmdsText.sharedCMDSQuesIds)) {
            return null;
        }
        Set<String> qIds = originalCmdsText.sharedCMDSQuesIds;
        CMDSQuestion question = new CMDSQuestion();
        for (String qId : qIds) {
            question = getCMDSQuestionById(qId);
            if (question.contentSrc.id.equals(orgId)) {
                return question;
            }
        }
        return null;
    }

    private void sharePARAQuestions(CMDSQuestion originalCmdsText, CMDSQuestion sharedCmdsQuesTEXT,
                                    Map<String, String> boardIdsMap, JSONObject info) throws JSONException, VedantuException {
        List<String> originalCmdsTextParaIds = originalCmdsText.paraIds;
        CMDSQuestion cmdsQuestion = new CMDSQuestion();
        CMDSQuestion sharedCmdsQuesPARA = new CMDSQuestion();
        for (String paraId : originalCmdsTextParaIds) {
            logger.debug("ShareQuestionsThread : ::::::::::::::::   Creating PARA Question Copy   ::::::::::::::::");
            cmdsQuestion = getCMDSQuestionById(paraId);
            if (cmdsQuestion.sharedToOrgIds.contains(info.getString("sharedToOrgId"))) {
                continue;
            }
            sharedCmdsQuesPARA = createAndSaveNewPARASharedCMDSQuestionCopy(cmdsQuestion, boardIdsMap,
                    info.getString("sharedToOrgUserId"), info.getString("sharedToOrgId"), sharedCmdsQuesTEXT);
            cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQuesPARA._getStringId());
            cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
            cmdsQuestionRepo.save(cmdsQuestion);
        }

    }

    public void start() {
        logger.debug("ShareQuestionsThread : Starting Job Id " + jobId);
        if (t == null) {
            t = new Thread(this, jobId);
            t.start();
        }
    }

    private EntityOperationStatus getEntityById(String jobId2) {
        Optional<EntityOperationStatus> entityOperationStatusOptional = entityOperationStatusRepo.findById(jobId2);
        if (entityOperationStatusOptional.isPresent()) {
            return entityOperationStatusOptional.get();
        }
        return null;
    }

    public List<CMDSQuestion> getQuestionsByBoard(String orgId, String sharedToOrgId, List<String> boardIds, List<String> types, int start, int size) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("contentSrc.type").is(EntityType.ORGANIZATION);
        criteria.and("contentSrc.id").is(orgId);
        criteria.and("recordState").is(VedantuRecordState.ACTIVE);
        criteria.and("boardIds").in(boardIds);
        criteria.and("type").in(types);
        List<String> sharedOrgIds = new ArrayList<String>();
        sharedOrgIds.add(sharedToOrgId);
        criteria.and("sharedToOrgIds").nin(sharedOrgIds);
        query.addCriteria(criteria);
        query.limit(size);
        return mongoTemplate.find(query, CMDSQuestion.class);
    }

    public CMDSFolder getRootFolder(String orgId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("organizationId").is(orgId);
        criteria.and("isRoot").is(Boolean.TRUE);
        query.addCriteria(criteria);
        query.fields().exclude("parent");
        return mongoTemplate.findOne(query, CMDSFolder.class);
    }

    private CMDSQuestion getCMDSQuestionById(String paragraphId) {
        Optional<CMDSQuestion> cmdsQuestionOptional = cmdsQuestionRepo.findById(paragraphId);
        if (cmdsQuestionOptional.isPresent()) {
            cmdsQuestionOptional.get();
        }
        return null;
    }


}

package com.vedantu.cmds.maintenance.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.vedantu.cmds.daos.CMDSFolderDAO;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.managers.CMDSQuestionManager;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.pojos.content.question.SolutionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.OperationType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.ei.utils.CollectionUtils;

public class ShareQuestionsThread implements Runnable {
    private Thread       t;
    private String       jobId;
    static final ALogger LOGGER = Logger.of(ShareQuestionsThread.class);

    public ShareQuestionsThread(String jobId) {
        this.jobId = jobId;
        LOGGER.debug("ShareQuestionsThread : Creating Job Id " + jobId);
    }

    public void run() {
        LOGGER.debug("ShareQuestionsThread : Running " + jobId);
        EntityOperationStatus job = EntityOperationStatusDAO.INSTANCE.getById(jobId);
        try {
            if (job.numOfSteps > 0 && job.oType == OperationType.CMDS_QUESTION_SHARING) {
                LOGGER.debug("ShareQuestionsThread : About to share questions");
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
                LOGGER.debug("ShareQuestionsThread : UnSupported Operation Type or Number Of Steps less than 0 for Job Id "
                        + jobId);
            }
        } catch (JSONException e) {
            LOGGER.debug("ShareQuestionsThread : JSONException Occured while Running Job Id " + jobId
                    + e.getMessage());
        } catch (VedantuException e) {
            LOGGER.debug("ShareQuestionsThread : VedantuException Occured while Running Job Id "
                    + jobId + e.getMessage());
        }
    }

    private void shareQuestions(EntityOperationStatus job, JSONObject info) throws JSONException,
            VedantuException {
        LOGGER.debug("ShareQuestionsThread : About to share " + info.getString("QType")
                + " questions for jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if(info.getString("QType").equals("SCQ")){
            types.add(QuestionType.SCQ.toString());
        }else if(info.getString("QType").equals("MCQ")){
            types.add(QuestionType.MCQ.toString());
        }else if(info.getString("QType").equals("MATRIX")){
            types.add(QuestionType.MATRIX.toString());
        }else if(info.getString("QType").equals("NUMERIC")){
            types.add(QuestionType.NUMERIC.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        CMDSQuestion sharedCmdsQues = new CMDSQuestion();
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while (totalQuestionsToMap > 0) {
            LOGGER.debug("ShareQuestionsThread : start is : " + start + " size is : " + size
                    + " for query w.r.t jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
            cmdsQuestions = CMDSQuestionDAO.INSTANCE.getQuestionsByBoard(
                    info.getString("parentOrgId"),info.getString("sharedToOrgId"), boardIds, types, start, size);
            LOGGER.debug("ShareQuestionsThread : Documents retrieved from above query is : "
                    + cmdsQuestions.size() + " for jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
            if(cmdsQuestions.size() == 0){
                LOGGER.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }

            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of Question "
                        + count + "   ::::::::::::::::" + " for jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
                LOGGER.debug("ShareQuestionsThread : Board Map is Built for organisation "+info.getString("sharedToOrgId"));
                sharedCmdsQues = createAndSaveNewSharedCMDSQuestionCopy(cmdsQuestion,
                        boardIdsMap, info.getString("sharedToOrgUserId"),
                        info.getString("sharedToOrgId"));
                LOGGER.debug("ShareQuestionsThread : Question shared and ready to map that question in parent question for organisation "+info.getString("sharedToOrgId"));
                cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQues._getStringId());
                cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
                CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
                LOGGER.debug("ShareQuestionsThread : Succesfully mapped shared question details in parent question for organisation "+info.getString("sharedToOrgId"));
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                EntityOperationStatusDAO.INSTANCE.save(job);
                LOGGER.debug("ShareQuestionsThread : Job saved successfully for organisation "+info.getString("sharedToOrgId"));
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
            }
            cmdsQuestions.clear();
            start = start + size;
        }
        LOGGER.debug("ShareQuestionsThread : Total " + info.getString("QType")
                + " questions shared is " + (count - 1) + " for jobId " + job._getStringId()+" to Organsiation "+info.getString("sharedToOrgId"));
    }

    public static CMDSQuestion createAndSaveNewSharedCMDSQuestionCopy(
            CMDSQuestion questionOriginal, Map<String, String> boardIdsMap, String userId,
            String orgId) throws VedantuException {
        LOGGER.debug("ShareQuestionsThread : Inside createAndSaveNewSharedCMDSQuestionCopy");
        String folderId = CMDSFolderDAO.INSTANCE.getRootFolder(orgId)._getStringId();
        CMDSQuestion question = new CMDSQuestion();
        question.boardIds = getNewBoards(boardIdsMap, questionOriginal.boardIds);
        question.completed = questionOriginal.completed;
        question.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        question.hints = questionOriginal.hints;
        question.difficulty = questionOriginal.difficulty;
        question.name = questionOriginal.name;
        //question.targetIds = questionOriginal.targetIds;
        question.lastUpdated = System.currentTimeMillis();
        question.origRefNo = questionOriginal.origRefNo;
        question.questionBody = questionOriginal.questionBody;
        question.recordState = questionOriginal.recordState;
        question.scope = Scope.PRIVATE;
        question.solutionInfo = questionOriginal.published ? removeGlobalAnswerIdSolutionId(questionOriginal.solutionInfo) : questionOriginal.solutionInfo;
        question.source = questionOriginal.source;
        question.status = questionOriginal.status;
        question.tags = questionOriginal.tags;
        question.timeCreated = System.currentTimeMillis();
        question.type = questionOriginal.type;
        question.userId = userId;
        question.parentQId = questionOriginal._getStringId();
        try {
            LOGGER.debug("ShareQuestionsThread : Before calling addQuestion");
            question = CMDSQuestionManager.addQuestion(orgId, userId, folderId, question);
            LOGGER.debug("ShareQuestionsThread : After calling addQuestion");
        } catch (Exception e) {
            LOGGER.debug("ShareQuestionsThread : Exception occured while creating new shared cmdsquestion copy "
                    + e.getMessage());
        }
        return question;
    }

    private static SolutionInfo removeGlobalAnswerIdSolutionId(SolutionInfo solutionInfo) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(solutionInfo);
        SolutionInfo newSolutionInfo = gson.fromJson(jsonString, solutionInfo.getClass());
        newSolutionInfo.globalAnsId = null;
        for(SolutionFormat solution: newSolutionInfo.solutions){
            solution.globalSolId = null;
        }
        return newSolutionInfo;
    }

    public static CMDSQuestion createAndSaveNewPARASharedCMDSQuestionCopy(
            CMDSQuestion questionOriginal, Map<String, String> boardIdsMap, String userId,
            String orgId, CMDSQuestion sharedCmdsQuesTEXT) throws VedantuException {
        LOGGER.debug("ShareQuestionsThread : Inside createAndSaveNewSharedCMDSQuestionCopy");
        String folderId = CMDSFolderDAO.INSTANCE.getRootFolder(orgId)._getStringId();
        CMDSQuestion question = new CMDSQuestion();
        question.paragraphId = sharedCmdsQuesTEXT._getStringId();
        question.boardIds = getNewBoards(boardIdsMap, questionOriginal.boardIds);
        question.completed = questionOriginal.completed;
        question.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        question.hints = questionOriginal.hints;
        question.difficulty = questionOriginal.difficulty;
        question.name = questionOriginal.name;
        //question.targetIds = questionOriginal.targetIds;
        question.lastUpdated = System.currentTimeMillis();
        question.origRefNo = questionOriginal.origRefNo;
        question.questionBody = questionOriginal.questionBody;
        question.recordState = questionOriginal.recordState;
        question.scope = Scope.PRIVATE;
        question.solutionInfo = questionOriginal.published ? removeGlobalAnswerIdSolutionId(questionOriginal.solutionInfo) : questionOriginal.solutionInfo;
        question.source = questionOriginal.source;
        question.status = questionOriginal.status;
        question.tags = questionOriginal.tags;
        question.timeCreated = System.currentTimeMillis();
        question.type = questionOriginal.type;
        question.userId = userId;
        question.parentQId = questionOriginal._getStringId();
        try {
            LOGGER.debug("ShareQuestionsThread : Before calling addQuestion");
            question = CMDSQuestionManager.addQuestion(orgId, userId, folderId, question);
            LOGGER.debug("ShareQuestionsThread : After calling addQuestion");
        } catch (Exception e) {
            LOGGER.debug("ShareQuestionsThread : Exception occured while creating new shared cmdsquestion copy "
                    + e.getMessage());
        }
        return question;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        String key = StringUtils.EMPTY;
        String value = StringUtils.EMPTY;
        while (keysItr.hasNext()) {
            key = keysItr.next();
            value = object.getString(key);
            map.put(key, value);
        }
        return map;
    }

    private static Set<String> getNewBoards(Map<String, String> toBeChangedToBoardIds,
            Set<String> orginalBoardIds) {
        LOGGER.debug("ShareQuestionsThread : Inside getNewBoards");
        Set<String> boardIds = new HashSet<String>();
        for (String brdId : orginalBoardIds) {
            if (toBeChangedToBoardIds.containsKey(brdId)) {
                boardIds.add(toBeChangedToBoardIds.get(brdId));
            }else{
                LOGGER.debug("ShareQuestionsThread : Corresponding shared board mapping not found for board id "+brdId);
            }
        }
        LOGGER.debug("ShareQuestionsThread : Getting out of getNewBoards");
        return boardIds;
    }

    private void shareTEXTQuestions(EntityOperationStatus job, JSONObject info)
            throws JSONException, VedantuException {
        LOGGER.debug("ShareQuestionsThread : About to share " + info.getString("QType")
                + " questions for jobId " + job._getStringId());
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if(info.getString("QType").equals("TEXT")){
            types.add(QuestionType.TEXT.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while (totalQuestionsToMap > 0) {
            LOGGER.debug("ShareQuestionsThread : start is : " + start + " size is : " + size
                    + " for jobId " + job._getStringId());
            cmdsQuestions = CMDSQuestionDAO.INSTANCE.getQuestionsByBoard(
                    info.getString("parentOrgId"),info.getString("sharedToOrgId"), boardIds, types, start, size);
            LOGGER.debug("ShareQuestionsThread : Documents retrieved from above query is : "
                    + cmdsQuestions.size() + " for jobId " + job._getStringId());
            if(cmdsQuestions.size() == 0){
                LOGGER.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }
            CMDSQuestion sharedCmdsQuesTEXT = new CMDSQuestion();
            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of TEXT Question "
                        + count + "   ::::::::::::::::" + " for jobId " + job._getStringId());
                sharedCmdsQuesTEXT = createAndSaveNewSharedCMDSQuestionCopy(
                        cmdsQuestion, boardIdsMap, info.getString("sharedToOrgUserId"),
                        info.getString("sharedToOrgId"));
                cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQuesTEXT._getStringId());
                cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
                CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
                sharePARAQuestions(cmdsQuestion, sharedCmdsQuesTEXT, boardIdsMap, info);
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                EntityOperationStatusDAO.INSTANCE.save(job);
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId());
            }
            cmdsQuestions.clear();
            start = start + size;
        }
        LOGGER.debug("ShareQuestionsThread : Total " + info.getString("QType")
                + " questions shared is " + (count - 1) + " for jobId " + job._getStringId());
    }

    private void sharePARAQuestions(EntityOperationStatus job, JSONObject info) throws JSONException, VedantuException {
        LOGGER.debug("ShareQuestionsThread : About to share " + info.getString("QType")
                + " questions for jobId " + job._getStringId());
        int count = 1;
        int start = 0;
        int size = 50;
        List<String> boardIds = new ArrayList<String>();
        boardIds.add(info.getString("parentBoardId"));
        List<String> types = new ArrayList<String>();
        if(info.getString("QType").equals("PARA")){
            types.add(QuestionType.PARA.toString());
        }
        int totalQuestionsToMap = job.numOfSteps;
        Map<String, String> boardIdsMap = toMap(info.getJSONObject("boardIdsMap"));
        CMDSQuestion originalCmdsText = new CMDSQuestion();
        CMDSQuestion sharedCmdsQuesTEXT = new CMDSQuestion();
        List<CMDSQuestion> cmdsQuestions = new ArrayList<CMDSQuestion>();
        while(totalQuestionsToMap > 0){
            LOGGER.debug("ShareQuestionsThread : start is : " + start + " size is : " + size
                    + " for jobId " + job._getStringId());
            cmdsQuestions = CMDSQuestionDAO.INSTANCE.getQuestionsByBoard(
                    info.getString("parentOrgId"),info.getString("sharedToOrgId"), boardIds, types, start, size);
            LOGGER.debug("ShareQuestionsThread : Documents retrieved from above query is : "
                    + cmdsQuestions.size() + " for jobId " + job._getStringId());
            if(cmdsQuestions.size() == 0){
                LOGGER.debug("ShareQuestionsThread : Mismatch in cmdsquestions count and number of steps");
                break;
            }
            for (CMDSQuestion cmdsQuestion : cmdsQuestions) {
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Creating new copy of PARA Question "
                        + count + "   ::::::::::::::::" + " for jobId " + job._getStringId());
                count++;
                totalQuestionsToMap--;
                job.numOfStepsCompleted++;
                EntityOperationStatusDAO.INSTANCE.save(job);
                if(StringUtils.isEmpty(cmdsQuestion.paragraphId)){
                    continue;
                }
                originalCmdsText = CMDSQuestionDAO.INSTANCE.getById(cmdsQuestion.paragraphId);
                if(originalCmdsText == null){
                    continue;
                }
                sharedCmdsQuesTEXT = getSharedCmdsQuesText(originalCmdsText,info.getString("sharedToOrgId"));
                if(sharedCmdsQuesTEXT == null){
                    continue;
                }
                sharePARAQuestions(originalCmdsText,sharedCmdsQuesTEXT,boardIdsMap,info);
                LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Created new copy of Question   ::::::::::::::::"
                        + " for jobId " + job._getStringId());
            }
            cmdsQuestions.clear();
            start = start + size;
        }
    }

    private CMDSQuestion getSharedCmdsQuesText(CMDSQuestion originalCmdsText, String orgId) {
        if(CollectionUtils.isEmpty(originalCmdsText.sharedCMDSQuesIds)){
            return null;
        }
        Set<String> qIds = originalCmdsText.sharedCMDSQuesIds;
        CMDSQuestion question = new CMDSQuestion();
        for(String qId : qIds){
            question = CMDSQuestionDAO.INSTANCE.getById(qId);
            if(question.contentSrc.id.equals(orgId)){
                return question;
            }
        }
        return null;
    }

    private void sharePARAQuestions(CMDSQuestion originalCmdsText, CMDSQuestion sharedCmdsQuesTEXT,
            Map<String, String> boardIdsMap, JSONObject info) throws JSONException,
            VedantuException {
        List<String> originalCmdsTextParaIds = originalCmdsText.paraIds;
        CMDSQuestion cmdsQuestion = new CMDSQuestion();
        CMDSQuestion sharedCmdsQuesPARA = new CMDSQuestion();
        for (String paraId : originalCmdsTextParaIds) {
            LOGGER.debug("ShareQuestionsThread : ::::::::::::::::   Creating PARA Question Copy   ::::::::::::::::");
            cmdsQuestion = CMDSQuestionDAO.INSTANCE.getById(paraId);
            if(cmdsQuestion.sharedToOrgIds.contains(info.getString("sharedToOrgId"))){
                continue;
            }
            sharedCmdsQuesPARA = createAndSaveNewPARASharedCMDSQuestionCopy(
                    cmdsQuestion, boardIdsMap, info.getString("sharedToOrgUserId"),
                    info.getString("sharedToOrgId"), sharedCmdsQuesTEXT);
            cmdsQuestion.sharedCMDSQuesIds.add(sharedCmdsQuesPARA._getStringId());
            cmdsQuestion.sharedToOrgIds.add(info.getString("sharedToOrgId"));
            CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
        }

    }

    public void start() {
        LOGGER.debug("ShareQuestionsThread : Starting Job Id " + jobId);
        if (t == null) {
            t = new Thread(this, jobId);
            t.start();
        }
    }
}

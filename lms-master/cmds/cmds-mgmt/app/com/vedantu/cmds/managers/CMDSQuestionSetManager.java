package com.vedantu.cmds.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSQuestionSetDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSQuestionSet;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.cmds.pojos.requests.questions.GetQuestionQSReq;
import com.vedantu.cmds.pojos.requests.questions.UploadQuestionSetFileReq;
import com.vedantu.cmds.pojos.responses.questions.ConfirmQuestionSetUploadRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionsRes;
import com.vedantu.cmds.pojos.responses.questions.UploadQuestionSetFileRes;
import com.vedantu.cmds.question.parser.QuestionSetDocParser;
import com.vedantu.cmds.utils.SInfoDeserializer;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.CMDSQuestionSetEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.tests.Metadata;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSQuestionSetManager extends AbstractCMDSContentManager {

    private static final ALogger         LOGGER   = Logger.of(CMDSQuestionSetManager.class);

    public static GsonBuilder            builder  = new GsonBuilder();
    private static Gson                  gson     = new Gson();
    public static CMDSQuestionSetManager INSTANCE = new CMDSQuestionSetManager();
    static {

        builder.registerTypeHierarchyAdapter(SolutionInfo.class, new SInfoDeserializer());
        gson = builder.create();
    }

    private CMDSQuestionSetManager() {

    }

    public UploadQuestionSetFileRes uploadQuestionFile(UploadQuestionSetFileReq request)
            throws VedantuException {

        UploadQuestionSetFileRes response = new UploadQuestionSetFileRes();
        // by ajith
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();

        LOGGER.info("Name of the file " + request.questionSetFile.getAbsolutePath());

        Logger.info("Organization id:" + request.orgId);
        LOGGER.info("Absolute on where to send " + tempFs.getDirectory() + File.separator);

        List<CMDSQuestion> allQuestions = null;
        Metadata globalMetadata = new Metadata();
        CMDSQuestionSet questionSet = new CMDSQuestionSet();

        try {
            LOGGER.debug("FileName" + request.questionSetFileName);

            allQuestions = QuestionSetDocParser.getContent(request.orgId,
                    request.questionSetFileName, request.questionSetFile, request.userId,
                    globalMetadata);

            List<String> collectedQuestionIds = new ArrayList<String>();

            questionSet.recordState = VedantuRecordState.TEMPORARY;
            questionSet.name = request.questionSetFileName;
            questionSet.contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
            questionSet.fileName = request.questionSetFileName;
            questionSet.numberOfQuestionsComplete = allQuestions.size();
            questionSet.userId = request.userId;
            /* Added by Shivank */
            questionSet.completed = CMDSQuestionSetDAO.INSTANCE.isReadyToPublished(questionSet);
            /* Added by Shivank */
            CMDSQuestionSetDAO.INSTANCE.save(questionSet);

            for (CMDSQuestion question : allQuestions) {

                question.recordState = VedantuRecordState.TEMPORARY;
                question.questionSetId = questionSet._getStringId();
                question.questionSetName = questionSet.name;
                /* Added by Shivank */
                question.completed = CMDSQuestionDAO.INSTANCE.isReadyToPublished(question);
                /* Added by Shivank */
                CMDSQuestionDAO.INSTANCE.save(question);
                collectedQuestionIds.add(question._getStringId());
                questionSet.addBoards(question.boardIds);
                questionSet.addTargets(question.boardIds);
                questionSet.addTags(question.tags);

            }
            questionSet.questionIds = collectedQuestionIds;

            CMDSQuestionSetDAO.INSTANCE.save(questionSet);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(e.getMessage() + " , error Line Info : ["
                    + globalMetadata.__getErrorLine() + "]");
            throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
        }
        String prefixFileName = null;
        try {
            prefixFileName = UUID.randomUUID().toString();
            // IEntityFileStorage entityStorage =
            // EntityType.QUESTION.getStorage();
            // StorageResult storeResult =
            // entityStorage.storeImage(prefixFileName, file,
            // FileCategory.ORIGINAL, ImageSize.ORIGINAL, null);
            String tempQuestionSetFileBeforeConfirmation = tempFs.getFilePath(
                    EntityType.CMDSQUESTIONSET.name().toLowerCase(), prefixFileName
                            + request.questionSetFileName);
            File nF = new File(tempQuestionSetFileBeforeConfirmation);
            FileUtils.copyFile(request.questionSetFile, nF);
            request.questionSetFile.delete();

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
        }

        response.metadata = globalMetadata;

        response.filePrefix = prefixFileName;
        response.questionSetName = globalMetadata.name;
        response.questionSetId = questionSet._getStringId();
        return response;
    }

    // @Deprecated
    // public ConfirmQuestionSetUploadRes confirmQuestions(String orgId, String questionList,
    // String userId, Metadata metadata, String prefixFileName,
    // String providedQuestionSetName, String folderId) throws VedantuException,
    // JSONException, JsonParseException, JsonMappingException, IOException,
    // ClassNotFoundException {
    //
    // Logger.debug(questionList);
    // if (StringUtils.isEmpty(folderId)) {
    // throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
    // }
    //
    // JSONArray questionJsonArray = new JSONArray(questionList);
    // List<String> qIds = new ArrayList<String>();
    // CMDSQuestionSet questionSet = new CMDSQuestionSet(providedQuestionSetName,
    // new ArrayList<String>(), userId, "Type", "INCOMPLETE", null, Difficulty.UNKNOWN,
    // metadata);
    //
    // CMDSQuestionSetDAO.INSTANCE.save(questionSet);
    //
    // String questionSetId = questionSet._getStringId();
    // List<String> successfulQuestionIds = new ArrayList<String>();
    // Class.forName("com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo");
    // String questionSetName = null;
    //
    // CMDSQuestion question = null;
    // for (int i = 0; i < questionJsonArray.length(); i++) {
    // try {
    // LOGGER.debug("json element is :" + questionJsonArray.getJSONObject(i).toString());
    //
    // question = gson.fromJson(questionJsonArray.getString(i), CMDSQuestion.class);
    // } catch (JSONException e1) {
    // throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
    // }
    // question.questionSetId = questionSetId;
    // try {
    // CMDSImageUtil.convertImageUrlToUuidAndSaveImage(question, true, false);
    // } catch (IOException e) {
    // LOGGER.error(e.getMessage(), e);
    // }
    // try {
    // CMDSQuestion confirmedQuestion = CMDSQuestionManager.INSTANCE.addQuestion(orgId,
    // userId, folderId, question);
    //
    // // TODO collecting all question ids to rollback everything
    //
    // successfulQuestionIds.add(confirmedQuestion._getStringId());
    // } catch (Exception e) {
    // throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
    //
    // }
    //
    // qIds.add(question._getStringId());
    // // ???
    // questionSetName = question.questionSetName;
    // }
    //
    // questionSet.questionIds = qIds;
    // if (StringUtils.isNotEmpty(providedQuestionSetName)) {
    // questionSet.name = providedQuestionSetName;
    // } else {
    // questionSet.name = questionSetName;
    // }
    // questionSet.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
    // Logger.debug("QuestionSet name" + questionSetName);
    //
    // CMDSQuestionSetDAO.INSTANCE.save(questionSet);
    //
    // LOGGER.info("questionSet saved successfully : " + questionSet);
    // CMDSResourcesManager.addToFolder(orgId, userId, new SrcEntity(EntityType.CMDSQUESTIONSET,
    // questionSet._getStringId()), folderId, CmdsContentLinkType.ADDED);
    //
    // LOGGER.info("copying the orininal QR file ");
    //
    // try {
    // LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
    // String tempQuestionSetFileBeforeConfirmation = tempFs.getFilePath(
    // EntityType.CMDSQUESTIONSET.name().toLowerCase(), prefixFileName
    // + questionSet.fileName);
    //
    // // TODO update to object storage
    // CMDSQuestionSetEntityFileStorage fileStorage = new CMDSQuestionSetEntityFileStorage();
    // Map<String, String> tags = new HashMap<String, String>();
    // tags.put(ConstantsGlobal.QUESTION_SET_ID, questionSet._getStringId());
    //
    // fileStorage.store(questionSet._getStringId(), new File(
    // tempQuestionSetFileBeforeConfirmation), MediaType.DOC, FileCategory.ORIGINAL,
    // tags);
    // // FileUtils.moveFile(new File(tempQuestionSetFileBeforeConfirmation),
    // // new File(CMDSImageUtil.getSaveToDocumentFileDir(questionSetId) + File.separator
    // // + question));
    //
    // } catch (EntityFileStorageException e) {
    //
    // LOGGER.error(e.getMessage(), e);
    // }
    // ConfirmQuestionSetUploadRes response = new ConfirmQuestionSetUploadRes();
    // response.isConfirmed = true;
    // response.questionsSetName = questionSetName;
    // LOGGER.info("question id COUNT=0:::" + qIds.get(0));
    // return response;
    // }

    public ConfirmQuestionSetUploadRes confirmQuestions(String orgId, List<String> questionList,
            String userId, String prefixFileName, String providedQuestionSetName,
            String questionSetId, String folderId, boolean shouldConfirm) throws VedantuException,
            JSONException, JsonParseException, JsonMappingException, IOException,
            ClassNotFoundException {

        LOGGER.debug("Total Question set list : " + questionList);
        if (StringUtils.isEmpty(folderId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_FOLDER_ID);
        }

        CMDSQuestionSet questionSet = CMDSQuestionSetDAO.INSTANCE.updateToProcessing(questionSetId);

        if (questionSet == null) {
            questionSet = CMDSQuestionSetDAO.INSTANCE.getById(questionSetId);

            if (questionSet != null && questionSet.recordState == VedantuRecordState.CONFIRMING) {
                throw new VedantuException(VedantuErrorCode.UNDER_PROCESSING);
            }

            throw new VedantuException(VedantuErrorCode.QUESTION_SET_NOT_FOUND);
        }

        if (!shouldConfirm) {
            ConfirmQuestionSetUploadRes response = new ConfirmQuestionSetUploadRes();
            CMDSQuestionSetDAO.INSTANCE.markDeleted(questionSet);
            response.isConfirmed = !delete(questionSet);
            response.questionsSetName = questionSet.name;
            return response;
        }

        questionSet.recordState = VedantuRecordState.CONFIRMING;

        LOGGER.debug("Updated record state to processing");

        questionSet.name = StringUtils.isNotEmpty(providedQuestionSetName) ? providedQuestionSetName
                : questionSet.name;

        List<CMDSQuestion> questions = CMDSQuestionDAO.INSTANCE.getByIds(
                ObjectIdUtils.toObjectIds(questionSet.questionIds), VedantuRecordState.TEMPORARY);
        if (questions.size() != questionSet.questionIds.size()) {
            throw new VedantuException(VedantuErrorCode.QUESTION_SET_SIZE_NOT_MATCHED);
        }
        for (CMDSQuestion cmdsQuestion : questions) {

            cmdsQuestion.questionSetName = StringUtils.isNotEmpty(providedQuestionSetName) ? providedQuestionSetName
                    : questionSet.name;
            cmdsQuestion.recordState = VedantuRecordState.ACTIVE;
            try {
                cmdsQuestion.removeImageSrc(true);

            } catch (EntityFileStorageException exception) {
                LOGGER.error("File storage Exception", exception);
                throw new VedantuException(VedantuErrorCode.QUESTION_SET_CAN_NOT_BE_SAVED);
            }

            try {
                LOGGER.debug("Saving questionId" + cmdsQuestion._getStringId());
                CMDSQuestionManager.addQuestion(orgId, userId, folderId, cmdsQuestion);
            } catch (Exception e) {
                LOGGER.debug("Can not save question " + cmdsQuestion);
            }

            // CMDSImageUtil.convertImageUrlToUuidAndSaveImage(cmdsQuestion, true, false);
        }

        LOGGER.info("copying the orininal QR file ");

        try {

            LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
            String tempQuestionSetFileBeforeConfirmation = tempFs.getFilePath(
                    EntityType.CMDSQUESTIONSET.name().toLowerCase(), prefixFileName
                            + questionSet.fileName);

            // TODO update to object storage
            CMDSQuestionSetEntityFileStorage fileStorage = new CMDSQuestionSetEntityFileStorage();
            Map<String, String> tags = new HashMap<String, String>();
            tags.put(ConstantsGlobal.QUESTION_SET_ID, questionSet._getStringId());

            fileStorage.store(questionSet._getStringId(), new File(
                    tempQuestionSetFileBeforeConfirmation), MediaType.DOC, FileCategory.ORIGINAL,
                    tags);
            // FileUtils.moveFile(new File(tempQuestionSetFileBeforeConfirmation), new File(
            // CMDSImageUtil.getSaveToDocumentFileDir(questionSetId) + File.separator
            // + question));

            // FileUtils.moveFile(new File(CMDSImageUtil.TEMP_DIR + File.separator + prefixFileName
            // + questionSet.name),
            // new File(CMDSImageUtil.getSaveToDocumentFileDir(questionSetId) + File.separator
            // + questionSet.name));

        } catch (EntityFileStorageException e) {
            // TODO Auto-generated catch block
            LOGGER.debug(" move file in event ");
        }

        questionSet.recordState = VedantuRecordState.ACTIVE;
        CMDSQuestionSetDAO.INSTANCE.updateModel(questionSet,
                Arrays.asList(ConstantsGlobal.RECORD_STATE, ConstantsGlobal.NAME));
        SrcEntity questionSetEntity = new SrcEntity(EntityType.CMDSQUESTIONSET,
                questionSet._getStringId());
        String parentLiveIndexId = AbstractCMDSContentManager.addAsCMDSResource(questionSetEntity,
                EventActionType.ADD, questionSet);
        LOGGER.info("questionSet saved successfully : " + questionSet);
        CMDSResourcesManager
                .addToFolder(orgId, userId,
                        new SrcEntity(EntityType.CMDSQUESTIONSET, questionSet._getStringId()),
                        folderId, CmdsContentLinkType.ADDED, parentLiveIndexId);
        ConfirmQuestionSetUploadRes response = new ConfirmQuestionSetUploadRes();
        response.isConfirmed = true;
        response.questionsSetName = questionSet.name;
        response.id = questionSet._getStringId();
        LOGGER.info("question id COUNT=0:::" + questionSet.questionIds.get(0));
        return response;
    }

    /**
     * This will do hard delete and it should be called very carefully
     *
     * @param questionSet
     * @return
     */
    public boolean delete(CMDSQuestionSet questionSet) {

        if (questionSet.recordState == VedantuRecordState.TEMPORARY
                || questionSet.recordState == VedantuRecordState.DELETED) {
            for (String questionId : questionSet.questionIds) {
                CMDSQuestionDAO.INSTANCE.markDeleted(questionId);
            }

            CMDSQuestionSetDAO.INSTANCE.markDeleted(questionSet._getStringId());
        }
        return true;
    }

    public GetCMDSQuestionsRes getQuestions(GetQuestionQSReq request) throws VedantuException {

        VedantuRecordState state = VedantuRecordState.ACTIVE;
        if (request.state != null) {
            state = request.state;
        }

        CMDSQuestionSet questionSet = CMDSQuestionSetDAO.INSTANCE.getById(request.questionSet.id,
                state);

        if (questionSet == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_SET_NOT_FOUND);
        }
        GetCMDSQuestionsRes response = new GetCMDSQuestionsRes();
        response.totalHits = questionSet.questionIds.size();
        if (request.start < 0 || request.start >= questionSet.questionIds.size()) {
            throw new VedantuException(VedantuErrorCode.INVALID_START_SPECIFIED);
        }
        // TODO change using slice in mongodb
        int toIndex = ((request.start + request.size) <= questionSet.questionIds.size()) ? (request.start + request.size)
                : questionSet.questionIds.size();
        Logger.debug("Start " + request.start + " end aat " + toIndex);
        List<String> questionIds = questionSet.questionIds.subList(request.start, toIndex);
        for (String questionId : questionIds) {
            CMDSQuestionInfo questionInfo = (CMDSQuestionInfo) CMDSQuestionDAO.INSTANCE
                    .getBasicInfo(questionId, state);

            response.list.add(questionInfo);
        }

        return response;
    }

    //
    // private boolean updateQuestionSet(String userId, CMDSQuestionSet questionSet)
    // throws VedantuException {
    //
    // if (questionSet == null) {
    // return false;
    // }
    // if (CollectionUtils.isNotEmpty(questionSet.questionIds)) {
    // questionSet.boardIds.clear();
    // questionSet.tags.clear();
    // questionSet.targetIds.clear();
    // List<CMDSQuestion> questionList = CMDSQuestionDAO.INSTANCE.getByIds(ObjectIdUtils
    // .toObjectIds(questionSet.questionIds));
    // for (CMDSQuestion question : questionList) {
    //
    // questionSet.addBoards(question.boardIds);
    // questionSet.addTargets(question.targetIds);
    // questionSet.addTags(question.tags);
    // }
    // }
    // /* Added by Shivank */
    // questionSet.completed = CMDSQuestionSetDAO.INSTANCE.isReadyToPublished(questionSet);
    // /* Added by Shivank */
    // CMDSQuestionSetDAO.INSTANCE.save(questionSet);
    // ReIndexDetails details = new ReIndexDetails();
    // details.userId = userId;
    // details.ids = new ArrayList<String>();
    // details.ids.add(questionSet._getStringId());
    // details.type = EntityType.CMDSQUESTIONSET;
    // generateEventAysc(userId, details, EventType.REINDEX_CMDS_RESOURCE);
    // return true;
    // }

    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        CMDSQuestionSet content = CMDSQuestionSetDAO.INSTANCE.getById(request.entity.id);

        if (content == null) {
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(AbstractContentModel.NAME);
        }

        CMDSQuestionSetDAO.INSTANCE.updateModel(content, updateList);

        addAsCMDSResource(request.entity, EventActionType.UPDATE, content);

        return true;
    }

}

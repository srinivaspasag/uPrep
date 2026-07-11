package com.vedantu.cmds.mgmt.publishers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSQuestionDAO;
import com.vedantu.cmds.daos.CMDSQuestionSetDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.managers.AbstractCMDSContentManager;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.models.CMDSQuestionSet;
import com.vedantu.cmds.pojos.content.question.SolutionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.GridSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.NumericSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.TextSolutionInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSQuestionEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.SolutionEntityFileStorage;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.image.ImageUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.LatexType;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.SolutionType;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class QuestionPublisher extends AbstractCMDSContentManager {

    public static QuestionPublisher INSTANCE = new QuestionPublisher();
    private static ALogger          LOGGER   = Logger.of(QuestionPublisher.class);

    private QuestionPublisher() {

    }

    @Override
    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        CMDSQuestion cmdsQuestion = CMDSQuestionDAO.INSTANCE.getById(content.id);
        if (cmdsQuestion == null) {
            LOGGER.debug("No Question found : " + content.id);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_PUBLISH_QUESTION);
        }

        Question question = null;
        // if (!cmdsQuestion.published || StringUtils.isEmpty(cmdsQuestion.globalQid)
                // || QuestionDAO.INSTANCE.getById(cmdsQuestion.globalQid) == null) {
            // cmdsQuestion.globalQid = null;
            question = publish(userId, orgId, cmdsQuestion);

        // }
        return question;
    }

    /**
     * This method is public as challenges can use if its needed same way questions publishing also
     * can use it
     *
     * @param userId
     * @param orgId
     * @param cmdsQuestion
     * @return
     * @throws VedantuException
     */
    public Question publish(String userId, String orgId, CMDSQuestion cmdsQuestion)
            throws VedantuException {

        if ((cmdsQuestion.type == QuestionType.SCQ || cmdsQuestion.type == QuestionType.MCQ || cmdsQuestion.type == QuestionType.MATRIX || cmdsQuestion.type == QuestionType.PARA)
                && (CollectionUtils.isEmpty(cmdsQuestion.solutionInfo.optionBody.newOptions))) {
            throw new VedantuException(VedantuErrorCode.QUESTION_OPTIONS_ARE_MISSING);
        }
        LOGGER.debug(" Whole cmdsQuestion " + cmdsQuestion.toString());

        Set<String> imguuids = collectAllUUIDForImages(cmdsQuestion);
        Question publishedQuestion = null;
        if (StringUtils.isNotEmpty(cmdsQuestion.globalQid)) {

            publishedQuestion = QuestionDAO.INSTANCE.getQuestion(cmdsQuestion.globalQid);
            // if (publishedQuestion != null) {
            //     return publishedQuestion;
            // }
            publishedQuestion.content = cmdsQuestion.questionBody.newText;
            publishedQuestion.options = cmdsQuestion.solutionInfo != null && cmdsQuestion.solutionInfo.optionBody != null ? cmdsQuestion.solutionInfo.optionBody.newOptions
                    : new ArrayList<String>();
            publishedQuestion.type = cmdsQuestion.type;
            publishedQuestion.imgUuids = imguuids;
        }else{
        // since question not published already creating new Question
        LOGGER.info("Creating new publishable question");

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
        LOGGER.info("qrquestion body newText: " + cmdsQuestion.questionBody.newText);
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

        LOGGER.debug("saving published question first  ");
        QuestionDAO.INSTANCE.save(publishedQuestion);
        processQuestionForPublishing(publishedQuestion, cmdsQuestion.questionSetId);

        publishSolution(publishedQuestion, cmdsQuestion);

        LOGGER.debug("saving published question again  ");

        QuestionDAO.INSTANCE.save(publishedQuestion);

        cmdsQuestion.globalQid = publishedQuestion._getStringId();
        cmdsQuestion.published = true;
        cmdsQuestion.status = QuestionStatus.COMPLETE;
        LOGGER.debug("saving cmds question again  ");
        CMDSQuestionDAO.INSTANCE.save(cmdsQuestion);
        CmdsContentLinkDAO.INSTANCE.addLink(
                new SrcEntity(EntityType.CMDSQUESTION, cmdsQuestion._getStringId()), new SrcEntity(
                        EntityType.QUESTION, publishedQuestion._getStringId()),
                CmdsContentLinkType.PUBLISHED, userId);

        LOGGER.debug("indexing question first time.. ");
        // live add global question search index
        QuestionSearchIndexDetails details = new QuestionSearchIndexDetails();
        details.fromMongoModel(publishedQuestion);
        addLiveEntityToSearchIndex(details, EntityType.QUESTION, true);

        generateEventAysc(cmdsQuestion.userId, cmdsQuestion, EventActionType.UPDATE,
                EventType.INDEX_CMDS_QUESTION, UserActionType.UPDATED, false);
        return publishedQuestion;
    }

    private static boolean publishSolution(Question publishedQuestion, CMDSQuestion question)
            throws VedantuException {

        LOGGER.debug("Publishing solutions" + question.solutionInfo.getClass());
        publishedQuestion.solutions = 0L;// no solutions are published yet so solution count is set
                                         // to 0
        // save all solutions
        if (question.solutionInfo == null) {
            return true;
        }

        if (CollectionUtils.isNotEmpty(question.solutionInfo.solutions)) {
            LOGGER.debug(".... solutions is not null ....");
            for (SolutionFormat solution : question.solutionInfo.solutions) {
                Solution publishedSolution = null;
                if (StringUtils.isNotEmpty(solution.globalSolId)) {
                    LOGGER.debug("....global solution id is not empty ....");
                    publishedSolution = SolutionDAO.INSTANCE.getById(solution.globalSolId);
                }
                LOGGER.debug("....outside if statement ....");
                if (publishedSolution == null) {
                    LOGGER.debug(".......cmdsattachments......" + solution.attachments + "......");
                    List<Attachment> cmdsAttachments = solution.attachments;
                    List<Attachment> attachments = new ArrayList<Attachment>();
                     if(cmdsAttachments!=null){
                        for (Attachment cmdsAttachment : cmdsAttachments) {
                            SrcEntity cmdsEntity = cmdsAttachment.entity;
                            VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE
                                    .get(cmdsEntity.type);

                            SrcEntity globalEntity = ((IPublishable) dao)
                                    .getGlobalEntity(cmdsEntity.id);

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
                    LOGGER.debug("saving solution : ");
                    LOGGER.debug("saving solution : ");
                    SolutionDAO.INSTANCE.save(publishedSolution);

                    solution.globalSolId = publishedSolution._getStringId();
                    publishedQuestion.solutions++;

                    processSolutionForPublishing(publishedSolution, question.questionSetId);
                    SolutionDAO.INSTANCE.save(publishedSolution);
                    // need to save as we have updated solution.
                }
            }
        }

        // saving answer now answers
        LOGGER.debug("saving answers : " + question.solutionInfo.getClass());
        Answer questionAnswer = AnswerDAO.INSTANCE.getByQuestionId(question.globalQid);
        if(questionAnswer == null) {
            questionAnswer = new Answer(publishedQuestion._getStringId(), publishedQuestion.userId,
                    publishedQuestion.type);
            LOGGER.debug(" created new answer " + questionAnswer);
        }
        if (publishedQuestion.type == QuestionType.SCQ) {
            LOGGER.debug("Publishing SCQ");
            SCQSolutionInfo sInfo = (SCQSolutionInfo) question.solutionInfo;
            LOGGER.debug("Publishing scq answer" + sInfo.answer);
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
        }
//         else if (publishedQuestion.type == QuestionType.MATRIX) {
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
          else if (publishedQuestion.type == QuestionType.SUBJECTIVE) {
            TextSolutionInfo sInfo = (TextSolutionInfo) question.solutionInfo;
            if (sInfo.answer != null) {
                questionAnswer.answer.add(sInfo.answer);
            }
        } else if (publishedQuestion.type == QuestionType.NUMERIC) {
            NumericSolutionInfo sInfo = (NumericSolutionInfo) question.solutionInfo;
            questionAnswer.answer = Arrays.asList(sInfo.answer);
        }
        if (questionAnswer.answer != null) {
            publishedQuestion.hasAns = true;
            LOGGER.debug("Saving answer now ");
            AnswerDAO.INSTANCE.save(questionAnswer);

            question.solutionInfo.globalAnsId = questionAnswer._getStringId();
            LOGGER.debug("Saved answer" + questionAnswer._getStringId());

        }
        AnswerDAO.INSTANCE.save(questionAnswer);
        return true;

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

    private static void verifyAndSetMetadata(Question publishedQuestion, CMDSQuestion cmdsQuestion)
            throws VedantuException {

        // Metadata providedMetadata = cmdsQuestion.metadata;
        // add exam related info
        LOGGER.info("Publishing difficulty" + cmdsQuestion.difficulty);

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

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

        if (model == null || !(model instanceof CMDSQuestion)) {
            return;
        }
        CMDSQuestion question = (CMDSQuestion) model;
        if (StringUtils.isNotEmpty(question.questionSetId)) {
            CMDSQuestionSet cmdsQuestionSet = CMDSQuestionSetDAO.INSTANCE
                    .getById(question.questionSetId);

            if (cmdsQuestionSet != null) {

                if (CollectionUtils.isNotEmpty(cmdsQuestionSet.questionIds)) {
                    cmdsQuestionSet.numberOfQuestionsComplete++;
                    if (cmdsQuestionSet.numberOfQuestionsComplete == cmdsQuestionSet.questionIds
                            .size()) {
                        cmdsQuestionSet.status = QuestionStatus.COMPLETE;
                    }
                }
            }
        }
    }

    @Override
    public void prePublish(SrcEntity content) {

        // TODO Auto-generated method stub

    }

    public static void processQuestionForPublishing(Question question, String questionSetId) {

        if (CollectionUtils.isEmpty(question.imgUuids)) {
            return;
        }
        for (String uuid : question.imgUuids) {
            // copySolut(true, uuid, question._getStringId(), questionSetId);
            LOGGER.info("uuid: " + uuid);
            LOGGER.info("question contains uuid: " + question.content.contains(uuid));
            LOGGER.info("bedfore content publish: " + question.content);
            // question.content = ImageDisplayURLUtil.getGlobalTypeImageFormat(question.content,
            // uuid);
            LOGGER.info("after content published: " + question.content);
            // if (CollectionUtils.isNotEmpty(question.options)) {
            // List<String> options = new ArrayList<String>();
            // // for (String option : question.options) {
            // // // options.add(ImageDisplayURLUtil.getGlobalTypeImageFormat(option, uuid));
            // // }
            // // question.options = options;
            // }

            if (org.apache.commons.collections.MapUtils.isNotEmpty(question.matrix)) {
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

    public static void processSolutionForPublishing(Solution solution, String questionSetId)
            throws VedantuException {

        if (CollectionUtils.isEmpty(solution.imgUuids)) {
            LOGGER.debug("No images found for soultion " + solution._getStringId()
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

    public static void copySolutionImage(boolean isQuestion, String uuid, String entityId,
            String questionSetId) throws VedantuException {

        SolutionEntityFileStorage solutionStorage = new SolutionEntityFileStorage();
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
}

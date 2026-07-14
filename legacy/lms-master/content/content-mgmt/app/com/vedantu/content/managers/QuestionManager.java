package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.entity.storage.QuestionEntityFileStorage;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.daos.analytics.UserQuestionAnalyticsDAO;
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.enums.SolutionType;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.content.pojos.AttachmentInfo;
import com.vedantu.content.pojos.ContentSize;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.questions.AddQuestionReq;
import com.vedantu.content.pojos.requests.questions.AddSolutionReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionsReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionsSolutionsReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.responses.questions.AddQuestionRes;
import com.vedantu.content.pojos.responses.questions.AddSolutionRes;
import com.vedantu.content.pojos.responses.questions.GetQuestionRes;
import com.vedantu.content.pojos.responses.questions.GetQuestionsSolutionRes;
import com.vedantu.content.pojos.responses.questions.GetSolutionRes;
import com.vedantu.content.pojos.responses.questions.GetSolutionsRes;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.events.utils.EventUtil;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.UserInfo;
import com.vedantu.user.social.actions.event.details.SolutionDetails;

public class QuestionManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(QuestionManager.class);

    public static AddQuestionRes addQuestion(AddQuestionReq addQuestionReq) throws VedantuException {

        if (addQuestionReq.type == QuestionType.NUMERIC) {
            try {
                String answer = addQuestionReq.answers.get(0);
                Double.parseDouble(answer);
            } catch (Exception e) {
                LOGGER.error("invalid answerd format for questionType: " + addQuestionReq.type, e);
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT, e);
            }
        }

        validateBoardIds(addQuestionReq._getAllBoardIds());
        try {
            addQuestionReq.removeImageSrc(true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.debug("adding question : " + addQuestionReq);
        Question qus = QuestionDAO.INSTANCE.addQuestion(addQuestionReq.userId,
                addQuestionReq.content, addQuestionReq.source, addQuestionReq.brdIds,
                addQuestionReq.type, addQuestionReq.options, addQuestionReq.targetIds,
                addQuestionReq.tags, addQuestionReq.matrixAnswer, addQuestionReq.scope,
                addQuestionReq.difficulty, addQuestionReq.contentSrc);

        LOGGER.debug("added question : " + addQuestionReq);
        List<String> fields = new ArrayList<String>();
        if (addQuestionReq.answers != null || addQuestionReq.matrixAnswer != null) {
            Logger.debug("adding answer ");
            AnswerDAO.INSTANCE.addAnswer(addQuestionReq.userId, qus._getStringId(),
                    addQuestionReq.type, addQuestionReq.answers, addQuestionReq.matrixAnswer);
            qus.hasAns = true;
            fields.add(ConstantsGlobal.HAS_ANS);
            LOGGER.debug("added answer ");
        }
        if (StringUtils.isNotEmpty(addQuestionReq.solution)) {
            Logger.debug("adding solution ");
            addSolution(new AddSolutionReq(qus.userId, qus._getStringId(), addQuestionReq.solution,
                    addQuestionReq.answers, addQuestionReq.matrixAnswer, SolutionType.UGS), false);
            qus.solutions = 1;
            fields.add(ConstantsGlobal.SOLUTION);
            LOGGER.debug("added solution ");
        }
        if (!fields.isEmpty()) {
            QuestionDAO.INSTANCE.updateModel(qus, fields);
        }
        generateEventAysc(addQuestionReq.userId, qus, EventActionType.ADD,
                EventType.INDEX_QUESTION, UserActionType.ADDED, true);
        AddQuestionRes addQuestionRes = new AddQuestionRes();
        addQuestionRes.fromMongoModel(qus);
        return (AddQuestionRes) annotateExtraInfo(
                addQuestionRes.userId,
                addQuestionRes.contentSrc != null
                        && addQuestionRes.contentSrc.type == EntityType.ORGANIZATION ? addQuestionRes.contentSrc.id
                        : null, EntityType.QUESTION, addQuestionRes);
    }

    public static AddSolutionRes addSolution(AddSolutionReq addSolutionReq) throws VedantuException {

        return addSolution(addSolutionReq, true);
    }

    public static AddSolutionRes addSolution(AddSolutionReq addSolutionReq, boolean generateEvent)
            throws VedantuException {

        try {
            addSolutionReq.removeImageSrc(true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (addSolutionReq.type == null) {
            addSolutionReq.type = SolutionType.UGS;

        }
        Solution sol = SolutionDAO.INSTANCE.addSolution(addSolutionReq.userId, addSolutionReq.qId,
                addSolutionReq.content, addSolutionReq.answers, addSolutionReq.gridAnswer,
                addSolutionReq.type, addSolutionReq.attachments);
        AddSolutionRes addSolutionRes = new AddSolutionRes(sol.qId, sol._getStringId());
        if (generateEvent) {
            generateAddSolutionEvent(sol);
            QuestionDAO.INSTANCE.incSolutionCount(sol.qId, 1);
        }
        return addSolutionRes;
    }

    public static GetQuestionRes getQuestionInfo(GetQuestionReq getQusReq) throws VedantuException {

        Question question = QuestionDAO.INSTANCE.getQuestion(getQusReq.id);
        if (question.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetQuestionRes qusRes = new GetQuestionRes();
        qusRes.fromMongoModel(question);
        qusRes = (GetQuestionRes) annotateExtraInfo(getQusReq.userId, qusRes.contentSrc != null
                && qusRes.contentSrc.type == EntityType.ORGANIZATION ? qusRes.contentSrc.id : null,
                EntityType.QUESTION, qusRes);
        // TODO: check why the challengeId is not coming in every question posted via challenge
        if (StringUtils.isEmpty(qusRes.challengeId)) {
            Challenge chall = ChallengeDAO.INSTANCE.findOne(ChallengeDAO.INSTANCE.createQuery()
                    .filter("entities.id", qusRes.id));
            qusRes.challengeId = chall == null ? null : chall._getStringId();
        }

        if (qusRes.attempted) {
            // if the user has attempted this question in global scope then add
            // the answer info for the user
            UserQuestionAnalytics userQuestionAnalytics = UserQuestionAnalyticsDAO.INSTANCE
                    .getAnalytics(getQusReq.userId, new SrcEntity(EntityType.QUESTION, qusRes.id),
                            qusRes.id);
            Answer answer = AnswerDAO.INSTANCE.getQuestionAnswer(qusRes.id);
            qusRes.answer = AnalyticsManager.getUserQuestionAnswerGiven(qusRes,
                    userQuestionAnalytics, answer);
        }
        return qusRes;
    }

    public static SearchListResponse<GetQuestionRes> getQuestions(GetQuestionsReq getQuestionsReq) {

        SearchListResponse<GetQuestionRes> results = getEntityInfos(getQuestionsReq,
                EntityType.QUESTION, GetQuestionRes.class, null);

        annotateExtraInfo(getQuestionsReq.userId, getQuestionsReq.orgId, EntityType.QUESTION,
                results.list);
        return results;
    }

	public static List<String> constructAnswerText(List<String> answerGiven) {
		List<String> newAnswerGiven = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(answerGiven)) {
			for (String answer : answerGiven) {
				answer = ImageHTMLUtils.addImageSrcUrl(
						EntityType.SUBJECTIVEANSWER, answer);
				newAnswerGiven.add(answer);
			}
		}
		return newAnswerGiven;
	}

    public static SearchListResponse<GetQuestionRes> getSimilarQuestion(
            GetSimilarEntities getQuestionsReq) {

        SearchListResponse<GetQuestionRes> results = getSimilarEntityInfos(getQuestionsReq,
                GetQuestionRes.class, null);
        annotateExtraInfo(getQuestionsReq.userId, getQuestionsReq.orgId, EntityType.QUESTION,
                results.list);
        return results;
    }

    public static GetSolutionsRes getSolutions(GetSolutionsReq getSolutionsReq)
            throws VedantuException {

        VedantuDBResult<Solution> solutions = SolutionDAO.INSTANCE.getSolutions(
                getSolutionsReq.qId, getSolutionsReq.userId, getSolutionsReq.start,
                getSolutionsReq.size, SortOrder.DESC , true);
        GetSolutionsRes getSolutionsRes = new GetSolutionsRes();
        getSolutionsRes.totalHits = solutions.totalHits;
        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();
        for (Solution sol : solutions.results) {
            userIds.add(sol.userId);
            entityIds.add(sol._getStringId());
            GetSolutionRes solRes = toSolutionRes(sol);
            solRes.addImageSrcUrl();
            solRes.user = new UserInfo(sol.userId, null);
            solRes.addImageSrcUrl();
            getSolutionsRes.list.add(solRes);
            List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
            if (CollectionUtils.isNotEmpty(sol.attachments)) {
                for (Attachment attachment : sol.attachments) {
                    VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                            .get(attachment.entity.type);
                    AttachmentInfo attachmentInfo = new AttachmentInfo();
                    attachmentInfo.entity = attachment.entity;
                    attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                    attachmentsInfo.add(attachmentInfo);
                }
            }
            solRes.attachmentsInfo = attachmentsInfo;
        }
        annotateUserSocialActionInfos(getSolutionsReq.orgId, getSolutionsReq.userId,
                EntityType.SOLUTION, getSolutionsRes.list, userIds, entityIds);
        return getSolutionsRes;
    }

    public static GetQuestionsSolutionRes getSolutionsMap(GetQuestionsSolutionsReq req)
            throws VedantuException {

        GetQuestionsSolutionRes res = new GetQuestionsSolutionRes();
        if (CollectionUtils.isEmpty(req.qIds)) {
            return res;
        }
        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();
        VedantuDBResult<Solution> solutions = SolutionDAO.INSTANCE.getSolutions(req.qIds,
                req.userId, MongoManager.NO_START, MongoManager.NO_LIMIT, SortOrder.DESC,
                req.verifiedOnly);
        List<GetSolutionRes> annotatedInfos = new ArrayList<GetSolutionRes>();
        for (Solution sol : solutions.results) {
            userIds.add(sol.userId);
            entityIds.add(sol._getStringId());
            GetSolutionRes solRes = toSolutionRes(sol);
            solRes.addImageSrcUrl();
            solRes.addImageSrcUrl();
            solRes.user = new UserInfo(sol.userId, null);
            annotatedInfos.add(solRes);
        }

        annotateUserSocialActionInfos(req.orgId, req.userId, EntityType.SOLUTION, annotatedInfos,
                userIds, entityIds);

        for (GetSolutionRes solRes : annotatedInfos) {
            if (res.solutions.get(solRes.qId) == null) {
                res.solutions.put(solRes.qId, new ArrayList<GetSolutionRes>());
            }
            res.solutions.get(solRes.qId).add(solRes);
        }
        annotatedInfos.clear();

        return res;
    }

    public static Map<String, QuestionSearchIndexDetails> getQuestionsMap(Collection<String> qIds) {

        return getQuestionsMap(qIds, true);
    }

    private static GetSolutionRes toSolutionRes(Solution sol) {

        List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
        if (CollectionUtils.isNotEmpty(sol.attachments)) {
            for (Attachment attachment : sol.attachments) {
                VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                        .get(attachment.entity.type);
                AttachmentInfo attachmentInfo = new AttachmentInfo();
                attachmentInfo.entity = attachment.entity;
                attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                attachmentsInfo.add(attachmentInfo);
            }
        }

        GetSolutionRes solRes = new GetSolutionRes(sol._getStringId(), sol.upVotes, sol.views,
                sol.followers, sol.comments, false, sol.qId, sol.userId, sol.content, sol.type,
                sol.answers, sol.verified, sol.timeCreated, sol.lastUpdated, attachmentsInfo);
        return solRes;
    }

    // TODO: optimise this process, fetch all the question from es
    public static Map<String, QuestionSearchIndexDetails> getQuestionsMap(Collection<String> qIds,
            boolean addBoardDetails) {

        Map<String, QuestionSearchIndexDetails> questionMap = new HashMap<String, QuestionSearchIndexDetails>();
        List<Question> questions = QuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(
                new ArrayList<String>(qIds), true));
        if (questions == null) {
            return questionMap;
        }
        for (Question q : questions) {
            QuestionSearchIndexDetails details = new QuestionSearchIndexDetails();
            details.__addBoardDetails(addBoardDetails);
            details.fromMongoModel(q);
            //Fix wrong solution count fetching
            details.solutions = SolutionDAO.INSTANCE.getSolutionsCount(q._getStringId());
            details.addImageSrcUrl();
            questionMap.put(q._getStringId(), details);
        }
        return questionMap;
    }

    private static void generateAddSolutionEvent(Solution solution) {

        SolutionDetails details = new SolutionDetails();
        details.fromMongoModel(solution);
        details.eventType = EventType.ADD_SOLUTION;
        EventUtil.generateEvent(EventType.ADD_SOLUTION, null, solution.userId, details,
                new SrcEntity(EntityType.QUESTION, solution.qId));
    }

    @Override
    public boolean calculate(String id,boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {

        List<Question> questions = new ArrayList<Question>();

        if (StringUtils.isNotEmpty(id)) {
            Question question = QuestionDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            questions.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Question) {
                    questions.add((Question) content);
                }
            }
        }
        // calculate question image size;

        for (Question question : questions) {
            if( question.size.isFinalized() && !recalculate){
                continue;
            }
            question.size.reset();

            // question body image sizes

            if (CollectionUtils.isNotEmpty(question.imgUuids)) {
                updateSizes(question, question.imgUuids);
            }

            if (question.solutions != 0) {

                List<Solution> solutions = SolutionDAO.INSTANCE
                        .getSolutions(question._getStringId(), null, MongoManager.NO_START,
                                MongoManager.NO_LIMIT).results;

                for (Solution solution : solutions) {
                    if (solution.size == null) {
                        solution.size = new ContentSize();
                    }
                    solution.size.reset();
                    // TODO update after attachment of solutions
                    updateSizes(solution, solution.imgUuids);

                    solution.size.finalize();
                    SolutionDAO.INSTANCE.updateModel(solution, Arrays.asList(Solution.SIZE));
                    question.size.add(solution.size);
                }

            }

            question.size.finalize();
            QuestionDAO.INSTANCE.updateModel(question, Arrays.asList(Question.SIZE));

        }

        return true;

    }

    private void updateSizes(Question question, Set<String> uuidImages) {

        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.QUESTION);
        for (String optionImageId : uuidImages) {

            long originalSize = defs.size(optionImageId, EntityType.QUESTION,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.ORIGINAL);
            long thumbnailSize = defs.size(optionImageId, EntityType.QUESTION,
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

    private void updateSizes(Solution solution, Set<String> uuidImages) {

        if (CollectionUtils.isEmpty(uuidImages)) {
            return;
        }
        IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(EntityType.SOLUTION);
        for (String optionImageId : uuidImages) {

            long originalSize = defs.size(optionImageId, EntityType.SOLUTION,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.ORIGINAL);
            long thumbnailSize = defs.size(optionImageId, EntityType.SOLUTION,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE, FileCategory.CONVERTED,
                    ImageSize.MEDIUM);
            if (originalSize != -1) {
                solution.size.addOriginal(originalSize);
            }
            if (thumbnailSize != -1) {
                solution.size.addThumbnail(thumbnailSize);
            }

        }
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException, EntityFileStorageException {

        Question question = QuestionDAO.INSTANCE.getById(entityId);
        FileData data = null;
        QuestionEntityFileStorage qStorage = new QuestionEntityFileStorage();
        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        if (CollectionUtils.isNotEmpty(question.imgUuids)) {
            Set<String> questionImages = question.imgUuids;

            for (String id : questionImages) {
                DownloadableFileInfo info = new DownloadableFileInfo();
                info.entityId = entityId;
                info.entityType = entityType;
                info.name = AbstractEntityFileStorage.computeFileId(id, EntityType.QUESTION,
                        FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                        FileCategory.CONVERTED, ImageSize.ORIGINAL);
                data = qStorage.getSecuredURL(id, EntityType.QUESTION,
                        FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                        FileCategory.CONVERTED, ImageSize.ORIGINAL);
                if( data != null ){
                  info.size = data.getContentLength();
                  info.downloadUrl = data.getSecuredURL();
                  info.mediaType = MediaType.IMAGE;
                  fileInfos.add(info);
                }
            }
        }

        VedantuDBResult<Solution> solutions = SolutionDAO.INSTANCE.getSolutions(entityId, null, 0,
                1, SortOrder.ASC, true);

        if (solutions.totalHits != 0) {
            for (Solution sol : solutions.results) {
                List<SrcEntity> entities = SolutionDAO.INSTANCE.getChildren(sol._getStringId());
                if (CollectionUtils.isNotEmpty(sol.imgUuids)) {

                    for (String id : sol.imgUuids) {
                        DownloadableFileInfo info = new DownloadableFileInfo();
                        info.entityId = entityId;
                        info.entityType = EntityType.SOLUTION;
                        info.name = AbstractEntityFileStorage.computeFileId(id,
                                EntityType.SOLUTION, FileUtils.JPG_EXTENTION_WITHOUT_DOT,
                                MediaType.IMAGE, FileCategory.CONVERTED, ImageSize.ORIGINAL);
                        data = qStorage.getSecuredURL(id, EntityType.SOLUTION,
                                FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                                FileCategory.CONVERTED, ImageSize.ORIGINAL);

                        info.downloadUrl = data.getSecuredURL();
                        info.size = data.getContentLength();
                        info.mediaType = MediaType.IMAGE;
                        fileInfos.add(info);
                    }
                }
                if (CollectionUtils.isNotEmpty(entities)) {
                    for (SrcEntity solAttachedEntity : entities) {
                        IContentManager manager = EntityTypeContentManagerFactory.INSTANCE
                                .get(entityType);
                        fileInfos.addAll(manager.getFiles(solAttachedEntity.type,
                                solAttachedEntity.id));
                    }
                }
            }

        }
        return fileInfos;
    }
}

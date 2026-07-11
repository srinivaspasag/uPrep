package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EventDetailsFactory;
import com.lms.common.utils.EventUtil;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.enums.UserActionType.EventActionType;
import com.lms.common.vedantu.event.api.IMongoAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.*;
import com.lms.managers.AbstractContentManager;
import com.lms.models.Answer;
import com.lms.models.Question;
import com.lms.models.Solution;
import com.lms.models.analytics.UserQuestionAnalytics;
import com.lms.pojos.Attachment;
import com.lms.pojos.AttachmentInfo;
import com.lms.pojos.requests.AddSolutionReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.questions.AddQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.requests.questions.GetQuestionsSolutionsReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.analytics.IQuestionAnswer;
import com.lms.pojos.responce.analytics.answers.QuestionListAnswer;
import com.lms.pojos.responce.questions.*;
import com.lms.pojos.search.details.AbstractSearchDetail;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.repository.AnswerRepo;
import com.lms.repository.QuestionRepo;
import com.lms.repository.SolutionRepo;
import com.lms.repository.UserQuestionAnalyticsRepo;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.user.vedantu.user.social.actions.event.details.SolutionDetails;
import com.mongodb.DBObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

@Component
public class QuestionComponent extends AbstractContentManager {
	private static final Logger logger = LoggerFactory.getLogger(QuestionComponent.class);
	@Autowired
	private QuestionRepo questionRepo;
	@Autowired
	private AnswerRepo answerRepo;
	@Autowired
	private SolutionRepo solutionRepo;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private UserQuestionAnalyticsRepo userQuestionAnalyticsRepo;
	@Autowired
	private EventUtil eventUtil;

	public void generateEventAysc(final String userId, final VedantuBaseMongoModel model,
								  final EventActionType action, final EventType eventType,
								  final UserActionType userAction, final boolean notificationEnabled)
			throws VedantuException {

		try {
			AbstractSearchDetail details = (AbstractSearchDetail) EventDetailsFactory.getInstance()
					.getDetails(eventType);

			details.userAction = userAction;
			details.isNotificationEnabled = notificationEnabled;

			details.setAction(action.name());
			IMongoAware mongoDetails = details;
			mongoDetails.fromMongoModel(model);
			logger.debug("loaded IndexDetails from mongomodel: " + details);
			generateEventAysc(userId, details, eventType);
		} catch (Exception exception) {
			throw new VedantuException(VedantuErrorCode.EVENT_NOT_SCHEDULED);
		}

	}

	public Question addQuestion(String userId, String content, String source, List<String> brdIds, QuestionType type,
								List<String> options, List<String> targetIds, List<String> tags, Map<String, List<String>> grid,
								Scope scope, Difficulty difficulty, SrcEntity contentSrc) throws VedantuException {

		Question qus = new Question(content, userId, type, source, new HashSet<String>(), LatexType.MATHJAX, options);
		qus.matrix = grid;
		qus.difficulty = difficulty;
		qus.contentSrc = contentSrc;
		qus.scope = scope;
		qus.addBoards(brdIds);
		qus.addTargets(targetIds);
		qus.addTags(tags);
		logger.debug("saving question : " + qus);
		questionRepo.save(qus);

		return qus;

	}

	public Answer addAnswer(String userId, String qId, QuestionType qType, List<String> answers,
							Map<String, List<String>> gridAnswer) throws VedantuException {
		Answer ans = new Answer(qId, userId, qType);
		ans.answer = answers;
		ans.matrixAnswer = gridAnswer;
		logger.debug("saving answer : " + ans);
		answerRepo.save(ans);
		return ans;
	}

	public AddSolutionRes addSolution(AddSolutionReq addSolutionReq, boolean generateEvent)
			throws VedantuException {

		try {
			addSolutionReq.removeImageSrc(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (addSolutionReq.type == null) {
			addSolutionReq.type = SolutionType.UGS;

		}
		Solution sol = addSolution(addSolutionReq.userId, addSolutionReq.qId,
				addSolutionReq.content, addSolutionReq.answers, addSolutionReq.gridAnswer, addSolutionReq.type,
				addSolutionReq.attachments);
		AddSolutionRes addSolutionRes = new AddSolutionRes(sol.qId, sol._getStringId());
		if (generateEvent) {
			generateAddSolutionEvent(sol);
			incSolutionCount(sol.qId, 1);
		}
		return addSolutionRes;
	}

	public Solution addSolution(String userId, String qid, String content, List<String> answers,
								Map<String, List<String>> gridAnswer, SolutionType type, List<Attachment> attachments)
			throws VedantuException {

		Solution sol = new Solution(qid, userId, content, answers, type, attachments);
		sol.gridAnswer = gridAnswer;
		logger.debug("saving solution : " + sol);
		solutionRepo.save(sol);
		return sol;
	}

	private void generateAddSolutionEvent(Solution solution) {

		SolutionDetails details = new SolutionDetails();
		details.fromMongoModel(solution);
		details.eventType = EventType.ADD_SOLUTION;
		CompletableFuture.runAsync(() -> {
			eventUtil.generateEvent(EventType.ADD_SOLUTION, null, solution.userId, details,
					new SrcEntity(EntityType.QUESTION, solution.qId));
		});

	}

	private static GetSolutionRes toSolutionRes(Solution sol) {

		List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
		if (CollectionUtils.isNotEmpty(sol.attachments)) {
			for (Attachment attachment : sol.attachments) {
               /* VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                        .get(attachment.entity.type);
                AttachmentInfo attachmentInfo = new AttachmentInfo();
                attachmentInfo.entity = attachment.entity;
                attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                attachmentsInfo.add(attachmentInfo);*/
			}
		}

		GetSolutionRes solRes = new GetSolutionRes(sol._getStringId(), sol.upVotes, sol.views,
				sol.followers, sol.comments, false, sol.qId, sol.userId, sol.content, sol.type,
				sol.answers, sol.verified, sol.timeCreated, sol.lastUpdated, attachmentsInfo);
		return solRes;
	}

	public static IQuestionAnswer getUserQuestionAnswerGiven(QuestionSearchIndexDetails detail,
															 UserQuestionAnalytics userQuestionAnalytics, Answer answer) {

		IQuestionAnswer qAnswer = new QuestionListAnswer(userQuestionAnalytics == null ? null
				: userQuestionAnalytics.answerGiven, answer == null ? null : answer.answer,
				userQuestionAnalytics == null ? AnswerCorrectness.INCORRECT
						: userQuestionAnalytics.isCorrect, userQuestionAnalytics == null ? 0
				: userQuestionAnalytics.timeTaken);


		return qAnswer;
	}

	public AddQuestionRes addQuestion(AddQuestionReq addQuestionReq) {
		if (addQuestionReq.type == QuestionType.NUMERIC) {
			try {
				String answer = addQuestionReq.answers.get(0);
				Double.parseDouble(answer);
			} catch (Exception e) {
				logger.error("invalid answerd format for questionType: " + addQuestionReq.type, e);
				throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT, e);
			}
		}

		validateBoardIds(addQuestionReq._getAllBoardIds());
		try {
			addQuestionReq.removeImageSrc(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		logger.debug("adding question : " + addQuestionReq);
		Question qus = addQuestion(addQuestionReq.userId, addQuestionReq.content,
				addQuestionReq.source, addQuestionReq.brdIds, addQuestionReq.type, addQuestionReq.options,
				addQuestionReq.targetIds, addQuestionReq.tags, addQuestionReq.matrixAnswer, addQuestionReq.scope,
				addQuestionReq.difficulty, addQuestionReq.contentSrc);

		logger.debug("added question : " + addQuestionReq);
		List<String> fields = new ArrayList<String>();
		if (addQuestionReq.answers != null || addQuestionReq.matrixAnswer != null) {
			logger.debug("adding answer ");
			addAnswer(addQuestionReq.userId, qus._getStringId(), addQuestionReq.type,
					addQuestionReq.answers, addQuestionReq.matrixAnswer);
			qus.hasAns = true;
			fields.add(ConstantsGlobal.HAS_ANS);
			logger.debug("added answer ");
		}
		if (!StringUtils.isEmpty(addQuestionReq.solution)) {
			logger.debug("adding solution ");
			addSolution(new AddSolutionReq(qus.userId, qus._getStringId(), addQuestionReq.solution,
					addQuestionReq.answers, addQuestionReq.matrixAnswer, SolutionType.UGS), false);
			qus.solutions = 1;
			fields.add(ConstantsGlobal.SOLUTION);
			logger.debug("added solution ");
		}
		if (!fields.isEmpty()) {
			//QuestionDAO.INSTANCE.updateModel(qus, fields);
		}
		generateEventAysc(addQuestionReq.userId, qus, EventActionType.ADD, EventType.INDEX_QUESTION,
				UserActionType.ADDED, true);
		AddQuestionRes addQuestionRes = new AddQuestionRes();
		addQuestionRes.fromMongoModel(qus);
		return (AddQuestionRes) annotateExtraInfo(addQuestionRes.userId,
				addQuestionRes.contentSrc != null && addQuestionRes.contentSrc.type == EntityType.ORGANIZATION
						? addQuestionRes.contentSrc.id
						: null,
				EntityType.QUESTION, addQuestionRes);

	}

	public VedantuBaseMongoModel incSolutionCount(String id, int inc) {

       /* UpdateOperations<T> update = getDS().createUpdateOperations(entityClazz).inc(
                ConstantsGlobal.SOLUTIONS, inc);

        T model = getDS().findAndModify(getQuery().filter(FIELD_ID, new ObjectId(id)), update);
        return model;*/
		Optional<Question> questionOptional = questionRepo.findById(id);
		if (questionOptional.isPresent()) {
			Question question = questionOptional.get();
			question.solutions = inc;
			questionRepo.save(question);
		}
		return null;
	}

	public GetSolutionsRes getSolutions(GetSolutionsReq getSolutionsReq) {
		List<Solution> solutions = getSolutions(
				getSolutionsReq.qId, getSolutionsReq.userId, getSolutionsReq.start,
				getSolutionsReq.size, "desc", true);
		GetSolutionsRes getSolutionsRes = new GetSolutionsRes();
		getSolutionsRes.totalHits = solutions.size();
		Set<String> userIds = new HashSet<String>();
		Set<String> entityIds = new HashSet<String>();
		for (Solution sol : solutions) {
			userIds.add(sol.userId);
			entityIds.add(sol._getStringId());
			GetSolutionRes solRes = toSolutionRes(sol);
			addImageSrcUrl(solRes);
			solRes.user = new UserInfo(sol.userId, null);
			//solRes.addImageSrcUrl();
			getSolutionsRes.list.add(solRes);
			List<AttachmentInfo> attachmentsInfo = new ArrayList<AttachmentInfo>();
			if (CollectionUtils.isNotEmpty(sol.attachments)) {
				for (Attachment attachment : sol.attachments) {
                   /* VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE
                            .get(attachment.entity.type);
                    AttachmentInfo attachmentInfo = new AttachmentInfo();
                    attachmentInfo.entity = attachment.entity;
                    attachmentInfo.info = dao.getBasicInfo(attachment.entity.id);
                    attachmentsInfo.add(attachmentInfo);*/
				}
			}
			solRes.attachmentsInfo = attachmentsInfo;
		}
		annotateUserSocialActionInfos(getSolutionsReq.orgId, getSolutionsReq.userId,
				EntityType.SOLUTION, getSolutionsRes.list, userIds, entityIds);
		return getSolutionsRes;

	}

	private List<Solution> getSolutions(String qId,
										String userId, int start, int size, String orderByTime, boolean verifiedOnly) {
		// TODO Auto-generated method stub
		return getSolutions(Arrays.asList(qId), userId, start, size, orderByTime,
				verifiedOnly);
	}

	public List<Solution> getSolutions(Collection<String> qIds, String userId,
									   int start, int size, String orderByTime, boolean verifiedOnly) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.QID).in(qIds.toArray());

		if (verifiedOnly) {
			criteria.and(ConstantsGlobal.VERIFIED).is(verifiedOnly);
		}
		query.with(Sort.by(Sort.Direction.DESC, ConstantsGlobal.TIME_CREATED));

		List<Solution> result = getInfos(query, null, start, size);
		return result;
	}

	public List<Solution> getInfos(Query query, DBObject fields, int start, int size
	) {
		query.skip(start);
		query.limit(size);
		return mongoTemplate.find(query, Solution.class);
	}

	public GetQuestionRes getQuestionInfo(GetQuestionReq getQusReq) {
		Optional<Question> questionOptional = questionRepo.findById(getQusReq.id);
		Question question = questionOptional.get();
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
           /* Challenge chall = ChallengeDAO.INSTANCE.findOne(ChallengeDAO.INSTANCE.createQuery()
                    .filter("entities.id", qusRes.id));
            qusRes.challengeId = chall == null ? null : chall._getStringId();*/
		}

		if (qusRes.attempted) {
			// if the user has attempted this question in global scope then add
			// the answer info for the user
			UserQuestionAnalytics userQuestionAnalytics =
					getAnalytics(getQusReq.userId, new SrcEntity(EntityType.QUESTION, qusRes.id),
							qusRes.id);
			Answer answer = getQuestionAnswer(qusRes.id);
			qusRes.answer = getUserQuestionAnswerGiven(qusRes,
					userQuestionAnalytics, answer);
		}
		return qusRes;

	}

	private UserQuestionAnalytics getAnalytics(String userId,
											   SrcEntity parentEntity, String qId) {
		logger.debug("getAnalytics userId: " + userId + ", parentEntity: " + parentEntity
				+ ", qId: " + qId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("userId").is(userId);
		criteria.and("parentEntity.type").is(parentEntity.type);
		criteria.and("parentEntity.id").is(parentEntity.id);
		criteria.and("qId").is(qId);
		query.addCriteria(criteria);
		UserQuestionAnalytics userQuestionAnalytics = mongoTemplate.findOne(query, UserQuestionAnalytics.class);

		logger.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

		return userQuestionAnalytics;

	}


	public Answer getQuestionAnswer(String id) {
		Answer answer = answerRepo.findByqId(id);
		return answer;
	}

	public GetQuestionsSolutionRes getSolutionsMap(GetQuestionsSolutionsReq req) {
		GetQuestionsSolutionRes res = new GetQuestionsSolutionRes();
		if (CollectionUtils.isEmpty(req.qIds)) {
			return res;
		}
		Set<String> userIds = new HashSet<String>();
		Set<String> entityIds = new HashSet<String>();
		List<Solution> solutions = getSolutions(req.qIds,
				req.userId, 0, 0, "desc",
				req.verifiedOnly);
		List<GetSolutionRes> annotatedInfos = new ArrayList<GetSolutionRes>();
		for (Solution sol : solutions) {
			userIds.add(sol.userId);
			entityIds.add(sol._getStringId());
			GetSolutionRes solRes = toSolutionRes(sol);
			//solRes.addImageSrcUrl();
			//solRes.addImageSrcUrl();
			addImageSrcUrl(solRes);
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

	public Map<String, QuestionSearchIndexDetails> getQuestionsMap(Collection<String> qIds) {

		return getQuestionsMap(qIds, true);
	}

	// TODO: optimise this process, fetch all the question from es
	public Map<String, QuestionSearchIndexDetails> getQuestionsMap(Collection<String> qIds,
																   boolean addBoardDetails) {

		Map<String, QuestionSearchIndexDetails> questionMap = new HashMap<String, QuestionSearchIndexDetails>();
		List<Question> questions = getQuestionsByIds(qIds);
		if (questions == null) {
			return questionMap;
		}
		for (Question q : questions) {
			QuestionSearchIndexDetails details = new QuestionSearchIndexDetails();
			details.__addBoardDetails(addBoardDetails);
			details.fromMongoModel(q);
			//Fix wrong solution count fetching
			details.solutions = getSolutionsCount(q._getStringId());
			//details.addImageSrcUrl();
			addImageSrcUrlForQuestion(details);
			questionMap.put(q._getStringId(), details);
		}
		return questionMap;
	}

	public long getSolutionsCount(String questionId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("qId").is(questionId);
		criteria.and("verified").is(true);
		query.addCriteria(criteria);
		long count = mongoTemplate.count(query, Solution.class);
		return count;
	}

	public List<Question> getQuestionsByIds(Collection<String> qIds) {
		return questionRepo.findAllById(ObjectIdUtils.toObjectIds(
				new ArrayList<String>(qIds), true));
	}

	public ListResponse<GetQuestionRes> getQuestions(GetQuestionsReq getQuestionsReq) {
		SearchListResponse<GetQuestionRes> results = getEntityInfos(getQuestionsReq,
				EntityType.QUESTION, GetQuestionRes.class, null);

		annotateExtraInfo(getQuestionsReq.userId, getQuestionsReq.orgId, EntityType.QUESTION,
				results.list);
		return results;
	}

	public SearchListResponse<GetQuestionRes> getSimilarQuestion(GetSimilarEntities getQuestionsReq) {
		SearchListResponse<GetQuestionRes> results = getSimilarEntityInfos(getQuestionsReq,
				GetQuestionRes.class, null);
		annotateExtraInfo(getQuestionsReq.userId, getQuestionsReq.orgId, EntityType.QUESTION,
				results.list);
		return results;

	}

	public void addImageSrcUrl(GetSolutionRes res) {
		res.content = addImageSrcUrl(EntityType.SOLUTION, res.content);
		if (CollectionUtils.isNotEmpty(res.answers)) {
			List<String> newAnswers = new ArrayList<String>();
			for (String answer : res.answers) {
				answer = addImageSrcUrl(EntityType.SOLUTION, answer);
				newAnswers.add(answer);
			}
			res.answers = newAnswers;
		}
	}

	public void addImageSrcUrlForQuestion(QuestionSearchIndexDetails details) {

		details.content = addImageSrcUrl(EntityType.QUESTION, details.content);
		if (details.options != null) {
			List<String> newOptions = new ArrayList<String>();
			for (String option : details.options) {
				option = addImageSrcUrl(EntityType.QUESTION, option);
				newOptions.add(option);
			}
			details.options = newOptions;
		}

		if (details.matrix != null) {
			Map<String, List<String>> newMatrix = new LinkedHashMap<String, List<String>>();
			for (Entry<String, List<String>> entry : details.matrix.entrySet()) {
				for (String option : entry.getValue()) {
					option = addImageSrcUrl(EntityType.QUESTION, option);
					if (newMatrix.get(entry.getKey()) == null) {
						newMatrix.put(entry.getKey(), new ArrayList<String>());
					}
					newMatrix.get(entry.getKey()).add(option);
				}
			}
			details.matrix = newMatrix;
		}
	}

}

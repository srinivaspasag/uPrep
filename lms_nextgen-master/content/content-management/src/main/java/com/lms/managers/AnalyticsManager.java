package com.lms.managers;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.AnalyticsComponent;
import com.lms.enums.AcademicDimensionType;
import com.lms.enums.AnswerCorrectness;
import com.lms.enums.EnumBasket.Judgement;
import com.lms.enums.EnumBasket.Status;
import com.lms.enums.QuestionType;
import com.lms.models.*;
import com.lms.models.analytics.*;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.models.tests.Assignment;
import com.lms.pojos.analytics.AnswerGivenCount;
import com.lms.pojos.analytics.QuestionAnalyticsExtendedInfo;
import com.lms.pojos.requests.StartAttemptReq;
import com.lms.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.lms.pojos.requests.analytics.RecordAttemptReq;
import com.lms.pojos.requests.analytics.ResetQuestionAttemptReq;
import com.lms.pojos.responce.RecordAttemptRes;
import com.lms.pojos.responce.analytics.GetQuestionAnalyticsRes;
import com.lms.pojos.responce.analytics.ResetQuestionAttemptRes;
import com.lms.pojos.responce.analytics.StartAttemptRes;
import com.lms.pojos.tests.TestQuestionSet;
import com.lms.repository.*;
import com.lms.utils.AnalyticsUtils;
import com.lms.utils.EntityUserActionUtils;
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
import java.util.concurrent.atomic.AtomicLong;


@Component
public class AnalyticsManager extends AbstractContentManager {
	private static final Logger logger = LoggerFactory.getLogger(AnalyticsComponent.class);
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private TestRepo testRepo;
	@Autowired
	private AssignmentRepo assignmentRepo;
	@Autowired
	private EntityUserActionUtils entityUserActionUtils;
	@Autowired
	private UserEntityAttemptRepo userEntityAttemptRepo;
	@Autowired
	private OrgMemberRepo orgMemberRepo;
	@Autowired
	private QuestionRepo questionRepo;
	@Autowired
	private AnswerRepo answerRepo;
	@Autowired
	private UserQuestionAttemptRepo userQuestionAttemptRepo;
	@Autowired
	private AnalyticsUtils analyticsUtils;

	@Autowired
	private QuestionAnalyticsRepo questionAnalyticsRepo;
	@Autowired
	private UserAnalyticsRepo userAnalyticsRepo;

	private boolean isPartialMarkingEnabled(Test test, String qType) {
		if (test.enablePartialMarks) {
			return test.partialMarksQTypes.contains(qType);
		} else {
			return false;
		}
	}

	private boolean isOneOrMoreAnswersAllowed(Test test, String qType) {
		if (CollectionUtils.isNotEmpty(test.oneOrMoreMarksQTypes)) {
			return test.oneOrMoreMarksQTypes.contains(qType);
		} else {
			return false;
		}
	}

	public StartAttemptRes startAttempt(StartAttemptReq startAttemptReq,
										boolean incAttemptCount, long startTime, long entTime) throws VedantuException {
		//Fix orgId value as PUBLIC from APP side.
		if (!startAttemptReq.userId.equalsIgnoreCase("PUBLIC") && !StringUtils.isEmpty(startAttemptReq.orgId) && startAttemptReq.orgId.equalsIgnoreCase("PUBLIC")) {
			startAttemptReq.orgId = orgMemberRepo.findByUserId(startAttemptReq.userId).orgId;
		}
		if (startAttemptReq.entityType == EntityType.TEST && !startAttemptReq.callingApp.equalsIgnoreCase("learn-app") && !startAttemptReq.callingApp.equalsIgnoreCase("cmds-app")) {
			if (startAttemptReq.target != null) {
				ScheduleInfo schedule = null;
				if (startAttemptReq.target.type == EntityType.MODULE) {
					logger.error("STA INSIDE MODULE");
					if (StringUtils.isEmpty(startAttemptReq.sectionId)) {
						throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
					}
					SrcEntity section = new SrcEntity(EntityType.SECTION, startAttemptReq.sectionId);
					SrcEntity globalEntity = new SrcEntity(EntityType.TEST,
							startAttemptReq.entityId);
					//Check whether content link exists for the test.
					AtomicLong moduleHits = new AtomicLong();
					LibraryContentLink moduleLink = getLibraryContentLink(startAttemptReq.target, section, UserActionType.ADDED, VedantuRecordState.ACTIVE, moduleHits);
					if (moduleLink == null) {
						throw new VedantuException(VedantuErrorCode.INVALID_ID,
								"TargetId/TestId is not valid");
					} else {
						AtomicLong moduleTestHits = new AtomicLong();
						LibraryContentLink moduleTestLink = getLibraryContentLink(globalEntity, startAttemptReq.target, UserActionType.ADDED, VedantuRecordState.ACTIVE, moduleTestHits);
						if (moduleTestLink == null) {
							throw new VedantuException(VedantuErrorCode.INVALID_ID,
									"TargetId/TestId is not valid");
						}
					}
					ModuleSchedules moduleSchedule = getGlobalSchedule(section,
							startAttemptReq.target, globalEntity);
					if (moduleSchedule != null) {
						schedule = moduleSchedule.getSchedule();
					}
				} else {
					logger.error("STA INSIDE SECTION");
					AtomicLong totalHits = new AtomicLong();
					LibraryContentLink cLink =
							getLibraryContentLink(new SrcEntity(EntityType.TEST,
											startAttemptReq.entityId), new SrcEntity(
											startAttemptReq.target.type, startAttemptReq.target.id),
									UserActionType.ADDED, VedantuRecordState.ACTIVE, totalHits);
					if (cLink == null) {
						throw new VedantuException(VedantuErrorCode.INVALID_ID,
								"TargetId/TestId is not valid");
					}
					schedule = cLink.getSchedule();
				}
				logger.error("TIME: testState " + startAttemptReq.testState);
				if (schedule != null) {
					if (schedule.startTime != null) {
						logger.error("start TIME: " + schedule.startTime.getTime());
						if (schedule.startTime.getTime() - System.currentTimeMillis() > 0) {
							throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
									"This test is not live yet");
						}
					}
					if (schedule.endTime != null) {
						logger.error("end TIME: " + schedule.endTime.getTime());
						if (StringUtils.isEmpty(startAttemptReq.testState)
								&& System.currentTimeMillis() - schedule.endTime.getTime() > 0) {
							throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
									"This test has expired");
						}
					}
					if (schedule.closeTime != null) {
						logger.error("close TIME: " + schedule.closeTime.getTime());
						if (!StringUtils.isEmpty(startAttemptReq.testState)
								&& startAttemptReq.testState.equals("RESUMED")
								&& System.currentTimeMillis() - schedule.closeTime.getTime() > 0) {
							throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE,
									"This test has expired and cannot be resumed");
						}
					}
				}
			} else {
				throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
			}
		}

		if (!isEntityAttemptAllowed(startAttemptReq)) {
			logger.error("not allowed start attempt of entity: " + startAttemptReq.entityType);
			throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
		}

		UserEntityAttempt userEntityAttempt = getAttempt(
				startAttemptReq.userId, startAttemptReq.entityType, startAttemptReq.entityId);

		if (null != userEntityAttempt && !isMultiAttemptAllowed(startAttemptReq)) {
			if (userEntityAttempt.endTime == 0 && userEntityAttempt.entity.type == EntityType.TEST) {
				StartAttemptRes startAttemptRes = new StartAttemptRes();
				startAttemptRes.info = userEntityAttempt.toBasicInfo();
				startAttemptRes.startTime = userEntityAttempt.timeCreated;
				startAttemptRes.isReattempt = true;
				startAttemptRes.qIds = userEntityAttempt.qIds;
				userEntityAttempt.testStatus = "ONGOING";
				userEntityAttemptRepo.save(userEntityAttempt);
				return startAttemptRes;
			}
			logger.error("disallowing start attempt by userId: " + startAttemptReq.userId
					+ ", entityType: " + startAttemptReq.entityType + ", entityId: "
					+ startAttemptReq.entityId + ", found a previous attempt: " + userEntityAttempt);
			throw new VedantuException(VedantuErrorCode.MULTI_ATTEMPTS_NOT_ALLOWED);
		}

		// Add list of questions and parent (if exist) for this attempt
		List<String> qIds = startAttemptReq.qIds != null ? startAttemptReq.qIds
				: new ArrayList<String>();
		AbstractContentStatsModel entityModel = getAttemptedEntity(new SrcEntity(
				startAttemptReq.entityType, startAttemptReq.entityId));
		AbstractTestCommonModel test = null;
		if (entityModel instanceof AbstractTestCommonModel) {
			test = (AbstractTestCommonModel) entityModel;
			SrcEntity parent = getParentAndUpdateQIds(startAttemptReq, qIds, test);
			userEntityAttempt = addEntityAttempt(startAttemptReq.userId, startAttemptReq.orgId,
					startAttemptReq.entityType, startAttemptReq.entityId, qIds, parent,
					incAttemptCount, startTime, entTime, parent == null, test, EventType.INDEX_TEST);
			// Add testStatus and save
			userEntityAttempt.testStatus = "ONGOING";
			userEntityAttempt.timeLeft = test.duration;
			userEntityAttemptRepo.save(userEntityAttempt);
			if (parent != null) {
				// mark the parent also as attempt
				// this will be usefull for fetching a user attempted test -->
				// we
				// will only fetch those test which does not has any parent
				UserEntityAttempt parentEntityAttempt = getAttempt(
						startAttemptReq.userId, parent.type, parent.id);
				if (parentEntityAttempt == null) {
					parentEntityAttempt = addEntityAttempt(startAttemptReq.userId, startAttemptReq.orgId, parent.type,
							parent.id, null, null, incAttemptCount, startTime, entTime, true, null,
							EventType.INDEX_TEST);
				}
			}
		} else {
			// in case of challenge
			userEntityAttempt = addEntityAttempt(startAttemptReq.userId, startAttemptReq.orgId,
					startAttemptReq.entityType, startAttemptReq.entityId, qIds, null,
					incAttemptCount, startTime, entTime, false, null, null);
		}

		StartAttemptRes startAttemptRes = new StartAttemptRes();
		startAttemptRes.info = userEntityAttempt.toBasicInfo();
		startAttemptRes.qIds = qIds;
		startAttemptRes.startTime = userEntityAttempt.timeCreated;
		startAttemptRes.isReattempt = false;
		return startAttemptRes;
	}

	public List<UserEntityAttempt> getUserAttemptsList(EntityType entityType, String entityId, String orgId, int start, int size, AtomicLong hits, List<String> userIds) {

		logger.debug("getAttempts entityType: " + entityType + ", entityId"
				+ entityId);
		Criteria criteria = new Criteria();
		Query query = new Query();
		criteria.and("entity.type").is(entityType);
		criteria.and("entity.id").is(entityId);
		criteria.and("orgId").is(orgId);
		if (!userIds.isEmpty()) {
			criteria.and("userId").in(userIds);
		}
		query.limit(size);
		query.skip(start);
		query.addCriteria(criteria);
		List<UserEntityAttempt> userEntityAttempts = mongoTemplate.find(query, UserEntityAttempt.class);
		logger.info("getAttempt getUserAttemptsList: " + userEntityAttempts);
		if (hits != null) {
			hits.set(userEntityAttempts.size());
		}
		return userEntityAttempts;
	}

	private boolean isEntityAttemptAllowed(StartAttemptReq startAttemptReq) {

		switch (startAttemptReq.entityType) {
			case TEST:
				return true;
			case CHALLENGE:
				return true;
			case ASSIGNMENT:
				return true;
			case QUESTION:
				return true;
			default:
				return false;
		}
	}

	private boolean isMultiAttemptAllowed(StartAttemptReq startAttemptReq) {

		return false;
	}

	public AbstractContentStatsModel getAttemptedEntity(SrcEntity entity)
			throws VedantuException {
		VedantuBaseMongoModel model = null;
		if (entity.type == EntityType.TEST) {
			Optional<Test> test2 = testRepo.findById(entity.getId());
			Test test1 = testRepo.findById("557807f5e4b0a68bc7ceca37").get();
			Test test = testRepo.findByIdAndRecordState(entity.getId(), "ACTIVE");
			if (test != null) {
				model = test;
			}
		} else if (entity.type == EntityType.ASSIGNMENT) {
			Assignment assignment = assignmentRepo.findByIdAndRecordState(entity.id, VedantuRecordState.ACTIVE);
			if (assignment != null) {
				model = assignment;
			}
		}

		if (model == null) {
			logger.error("no entity found entity:" + entity);
			throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
		}
		AbstractContentStatsModel test = (AbstractContentStatsModel) model;
		return test;
	}

	private UserEntityAttempt addEntityAttempt(String userId, String orgId, EntityType entityType,
											   String entityId, List<String> qIds, SrcEntity parent, boolean incAttemptCount,
											   long startTime, long endTime, boolean updateIndex, AbstractContentStatsModel model,
											   EventType eventType) throws VedantuException {

		UserEntityAttempt userEntityAttempt = addAttempt(userId, orgId,
				entityType, entityId, qIds, parent);
		if (startTime > 0 && endTime > 0) {
			userEntityAttempt.timeCreated = startTime;
			userEntityAttempt.endTime = endTime;
			userEntityAttemptRepo.save(userEntityAttempt);
		}
		SrcEntity attemptedEntity = new SrcEntity(entityType, entityId);
		if (incAttemptCount) {
			entityUserActionUtils.addEntityUserAction(userEntityAttempt.userId, attemptedEntity,
					UserActionType.ATTEMPTED, false, updateIndex);
		}
		return userEntityAttempt;
	}

	public UserEntityAttempt addAttempt(String userId, String orgId, EntityType entityType, String entityId,
										List<String> qIds, SrcEntity parent) {

		return addAttempt(userId, orgId, entityType, entityId, qIds, parent, 0);
	}

	public UserEntityAttempt addAttempt(String userId, String orgId, EntityType entityType, String entityId,
										List<String> qIds, SrcEntity parent, long endTime) {

		logger.debug("addAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
				+ entityId + ", qIds.size: " + (qIds == null ? 0 : CollectionUtils.size(qIds)));

		UserEntityAttempt userEntityAttempt = new UserEntityAttempt(userId, orgId, new SrcEntity(
				entityType, entityId), qIds);

		userEntityAttempt.parent = parent;
		userEntityAttempt.endTime = endTime;
		userEntityAttemptRepo.save(userEntityAttempt);

		logger.info("addAttempt userEntityAttempt: " + userEntityAttempt);

		return userEntityAttempt;
	}

	public UserEntityAttempt getAttempt(String userId, EntityType entityType, String entityId) {

		logger.debug("getAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
				+ entityId);
		Criteria criteria = new Criteria();
		Query query = new Query();
		criteria.and(ConstantsGlobal.USER_ID).is(userId);
		criteria.and("entity.type").is(entityType);
		criteria.and("entity.id").is(entityId);
		query.addCriteria(criteria);
		UserEntityAttempt userEntityAttempt = mongoTemplate.findOne(query, UserEntityAttempt.class);
		logger.info("getAttempt userEntityAttempt: " + userEntityAttempt);

		return userEntityAttempt;
	}

	public LibraryContentLink getLibraryContentLink(SrcEntity content, SrcEntity targetEntity,
													UserActionType linkType, VedantuRecordState recordState, AtomicLong totalHits) {

		List<LibraryContentLink> contentLinks = getLibraryContentLinks(content, targetEntity,
				linkType, null, recordState, 0, 1, totalHits);
		if (CollectionUtils.isNotEmpty(contentLinks)) {
			return contentLinks.get(0);
		}

		return null;
	}

	public List<LibraryContentLink> getLibraryContentLinks(SrcEntity content,
														   SrcEntity targetEntity, UserActionType linkType, String actorId,
														   VedantuRecordState recordState, int start, int size, AtomicLong totalHits) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		if (targetEntity.type != null)
			criteria.and("target.type").is(targetEntity.type);
		if (targetEntity.id != null)
			criteria.and("target.id").is(targetEntity.id);
		if (content.type != null)
			criteria.and("source.type").is(content.type);
		if (content.id != null)
			criteria.and("source.id").is(content.id);
		if (linkType != null)
			criteria.and("linkType").is(linkType);
		if (recordState != null) {
			criteria.and("recordState").is(recordState);
		}
		query.addCriteria(criteria);
		query.skip(start).limit(size);
		List<LibraryContentLink> libraryLinks = mongoTemplate.find(query, LibraryContentLink.class);
		if (totalHits != null) {
			long count = libraryLinks.size();
			totalHits.set(count);
			logger.debug("Total matched results " + totalHits);
		}

		return libraryLinks;
	}

	public ModuleSchedules getGlobalSchedule(SrcEntity target, SrcEntity globalSource, SrcEntity globalEntity) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("target.type").is(target.type);// SECTION
		criteria.and("target.id").is(target.id);
		criteria.and("globalSource.type").is(globalSource.type);// MODULE
		criteria.and("globalSource.id").is(globalSource.id);
		criteria.and("globalEntity.type").is(globalEntity.type);// TEST
		criteria.and("globalEntity.id").is(globalEntity.id);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, ModuleSchedules.class);
	}

	private SrcEntity getParentAndUpdateQIds(StartAttemptReq startAttemptReq,
											 List<String> qIds, AbstractTestCommonModel test) {

		SrcEntity parent = null;
		if (startAttemptReq.entityType == EntityType.TEST
				&& !StringUtils.isEmpty(startAttemptReq.entityId)) {
			if (!StringUtils.isEmpty(test.parentId)) {
				parent = new SrcEntity(EntityType.TEST, test.parentId);
			}
			boolean addedQids = false;
			if (!StringUtils.isEmpty(startAttemptReq.setName)) {
				TestQuestionSet qSet = test.__getQuestionSet(startAttemptReq.setName);
				if (qIds.isEmpty() && qSet != null && CollectionUtils.isNotEmpty(qSet.qIds)) {
					qIds.addAll(qSet.qIds);
					addedQids = true;
				}
			}
			if (qIds.isEmpty() && !addedQids) {
				qIds.addAll(test.__getAllQIds());
			}
		}
		logger.info("getParentAndUpdateQIds : qIds : " + qIds + ", parent:" + parent);
		return parent;
	}

	public RecordAttemptRes recordAttempts(RecordAttemptReq recordAttemptReq)
			throws VedantuException {

		if (!isEntityAttemptAllowed(recordAttemptReq)
				&& EntityType.QUESTION != recordAttemptReq.entityType) {
			logger.error("recordAttempt not allowed record attempt of entity: "
					+ recordAttemptReq.entityType);
			throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
		}

		if (recordAttemptReq.entityType == EntityType.TEST) {
			recordAttemptReq.attemptId = (recordAttemptReq.attemptId.isEmpty() || recordAttemptReq.attemptId == null) ? getAttemptId(
					recordAttemptReq.userId, recordAttemptReq.entityType, recordAttemptReq.entityId)
					: recordAttemptReq.attemptId;
			String testStatus = entityStatus(recordAttemptReq.attemptId);
			if (testStatus.equals("FINISHED")) {
				logger.error("Entity is Finished");
				throw new VedantuException(VedantuErrorCode.TEST_ENDED);
			}
			if (testStatus.equals("PAUSED")) {
				logger.error("Entity is PAUSED");
				throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
			}
			if (testStatus.equals("RESUMED")) {
				logger.error("Entity is RESUMED");
				throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
			}
		}


		if (EntityType.QUESTION == recordAttemptReq.entityType
				&& recordAttemptReq.entityId != null && recordAttemptReq.qId != null && recordAttemptReq.entityId.equals(recordAttemptReq.qId)) {
			logger.error("recordAttempt mismatch in id for entityType: "
					+ recordAttemptReq.entityType + ", entityId: " + recordAttemptReq.entityId
					+ ", qId: " + recordAttemptReq.qId);
			throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_ID);
		}

		final SrcEntity parentEntity = new SrcEntity(recordAttemptReq.entityType,
				recordAttemptReq.entityId);

		if (EntityType.QUESTION == recordAttemptReq.entityType) {
			List<UserQuestionAttempt> prevAttempts = getAttempts(
					recordAttemptReq.userId, parentEntity, Arrays.asList(recordAttemptReq.qId), null);
			if (CollectionUtils.isNotEmpty(prevAttempts)) {
				logger.error("recordAttempt found previous attempts for parentEntity: "
						+ parentEntity + ", qId: " + recordAttemptReq.qId + ", numPrevAttempts: "
						+ CollectionUtils.size(prevAttempts));
				throw new VedantuException(VedantuErrorCode.MULTI_ATTEMPTS_NOT_ALLOWED);
			}
		}

		// Lets first do recording of attempt of the question
		Question question = getQuestion(recordAttemptReq.qId);

		AnswerCorrectness isCorrect = AnswerCorrectness.INCORRECT;
		List<String> correctAnswer = null;
		int score = 0;
		if (question.type.isJudgeable()) {
			logger.debug("recordAttempt : Question is Judgeable");
			Answer answer = getQuestionAnswer(recordAttemptReq.qId);

			if (answer == null) {
				logger.debug("recordAttempt : answer is null");
				if (question.answerId != null && !question.answerId.isEmpty()) {
					logger.debug("recordAttempt : Getting answer again with help of ID");
					Optional<Answer> answerOptional = answerRepo.findById(question.answerId);

					if (!answerOptional.isPresent()) {
						logger.error("user[" + recordAttemptReq.userId + "], question ["
								+ recordAttemptReq.qId + "] does not have a verified answer");
						throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
					}
					answer = answerOptional.get();
				} else {
					logger.error("user[" + recordAttemptReq.userId + "], question ["
							+ recordAttemptReq.qId + "] does not have a verified answer");
					throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
				}
			}
			correctAnswer = answer.answer;
			logger.info("recordAttempt : question: " + question);
			//if (question.type != QuestionType.MATRIX) {
			logger.debug("recordAttempt : Given question is " + question.type);
			boolean isPartialMarksAllowed = false;
			boolean isOneOrMoreAllowed = true;
			if (recordAttemptReq.entityType == EntityType.TEST) {

				Optional<Test> testOptional = testRepo.findById(recordAttemptReq.entityId);
				Test test = testOptional.get();
				isPartialMarksAllowed = isPartialMarkingEnabled(test, question.type.name());
				isOneOrMoreAllowed = isPartialMarksAllowed || isOneOrMoreAnswersAllowed(test, question.type.name());
			}
			if (CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
				logger.debug("recordAttempt : Getting correct answer");
				isCorrect = question.type.isCorrect(Judgement.JUDGE,
						recordAttemptReq.getAnswerGiven(), answer.answer,
						Status.COMPLETE, isPartialMarksAllowed, isOneOrMoreAllowed);
				logger.debug("recordAttempt : isCorrect is " + isCorrect.toString());
			}

			if (answer.optionalCorrectAnswers != null && isCorrect == AnswerCorrectness.INCORRECT) {
				logger.debug("recordAttempt : Checking for optional correct answers if answer is incorrect");
				for (Entry<Integer, List<String>> answers : answer.optionalCorrectAnswers
						.entrySet()) {
					//CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven()) &&
					logger.debug("recordAttempt : Found optional correct answers, and iterating inside it");
					if (CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
						isCorrect = question.type.isCorrect(Judgement.JUDGE,
								recordAttemptReq.getAnswerGiven(), answers.getValue(),
								Status.COMPLETE, isPartialMarksAllowed, isOneOrMoreAllowed);
					}
					if (isCorrect == AnswerCorrectness.CORRECT || isCorrect == AnswerCorrectness.PARTIAL) {
						break;
					}
				}
			}

		}
		logger.debug("recordAttempt : isCorrect Before Adding Attempt is " + isCorrect.toString());
		UserQuestionAttempt userQuestionAttempt = addAttempt(
				recordAttemptReq.userId, recordAttemptReq.attemptId, parentEntity,
				recordAttemptReq.qId, recordAttemptReq.getAnswerGiven(),
				recordAttemptReq.getMatrixAnswer(), question.type, question.type.isJudgeable(),
				isCorrect, score, recordAttemptReq.timeTaken);
		logger.debug("recordAttempt : isCorrect After Adding Attempt is " + isCorrect.toString());
		logger.debug("recordAttempt : userQuestionAttempt: " + userQuestionAttempt);
		if (EntityType.QUESTION == recordAttemptReq.entityType) {
			logger.debug("recordAttempt : EntityType is QUESTION");
			finalizeQuestionAttempt(userQuestionAttempt, question, recordAttemptReq.orgId);
		} else if (recordAttemptReq.entityType == EntityType.ASSIGNMENT) {
			logger.debug("recordAttempt : EntityType is ASSIGNMENT");
			finalizeAssignmentAttempt(userQuestionAttempt, question, recordAttemptReq.orgId);
		}

		final RecordAttemptRes recordAttemptRes = new RecordAttemptRes();
		recordAttemptRes.userAnswer = recordAttemptReq.getAnswerGiven();
		if (EntityType.TEST != recordAttemptReq.entityType) {
			recordAttemptRes.isJudgeable = question.type.isJudgeable();
			recordAttemptRes.correctAnswer = correctAnswer;
			recordAttemptRes.isUserAnswerCorrect = isCorrect;
		}
		logger.info("recordAttempt : response: " + recordAttemptRes);
		return recordAttemptRes;
	}

	public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, List<String> qIds,
												 Boolean isJudgeable) {

		logger.debug("getAttempts userId: " + userId + ", parentEntity: " + parentEntity + ", qIds: " + qIds);
		if (qIds == null) {
			qIds = new ArrayList<String>();
		}
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("userId").is(userId);
		criteria.and("parentEntity.type").is(parentEntity.type);
		criteria.and("parentEntity.id").is(parentEntity.id);
		criteria.and("qId").in(qIds);
		if (isJudgeable != null) {
			criteria.and("isJudgeable").is(isJudgeable.booleanValue());
		}
		query.addCriteria(criteria);
		query.with(Sort.by(ConstantsGlobal.TIME_CREATED));
		List<UserQuestionAttempt> userQuestionAttempts = mongoTemplate.find(query, UserQuestionAttempt.class);

		logger.info("getAttempts userQuestionAttempts.size: " + CollectionUtils.size(userQuestionAttempts));

		return userQuestionAttempts;
	}

	private String entityStatus(String attemptId) throws VedantuException {
		// TODO Auto-generated method stub
		UserEntityAttempt user = _entityStatus(attemptId);

		return user.testStatus;
	}

	private UserEntityAttempt _entityStatus(String attemptId) throws VedantuException {
		// TODO Auto-generated method stub
		Optional<UserEntityAttempt> userOptional = userEntityAttemptRepo.findById(attemptId);
		if (!userOptional.isPresent()) {
			throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
		}
		return userOptional.get();
	}

	private String getAttemptId(String userId, EntityType entityType, String entityId) {
		// TODO Auto-generated method stub
		String attemptId = getAttempt(userId, entityType, entityId)._getStringId();
		return attemptId == null ? "" : attemptId;
	}

	public Question getQuestion(String id) throws VedantuException {

		Optional<Question> questionOptional = questionRepo.findById(id);
		if (!questionOptional.isPresent()) {
			throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND, "no question found with id:" + id);
		}
		return questionOptional.get();
	}

	public Answer getQuestionAnswer(String qId) {

		return answerRepo.findByqId(qId);
	}

	private boolean finalizeQuestionAttempt(UserQuestionAttempt userQuestionAttempt,
											Question question, String orgId) throws VedantuException {

		// NOTE: cannot be used for LEFT

		if (null == userQuestionAttempt) {
			logger.error("finalizeQuestionAttempt cannot finalize question attempt for null userQuestionAttempt");
			return false;
		}
		if (null == question) {
			try {
				question = getQuestion(userQuestionAttempt.qId);
			} catch (VedantuException e) {
				logger.error(
						"finalizeQuestionAttempt swallowing exception but this should not have occurred -- code: "
								+ e.errorCode + ", msg: " + e.getMessage(), e);
				return false;
			}
		}

		// store it in entity attempts and add the mapping to es
		logger.debug("storing UserEntityAttempt userId: " + userQuestionAttempt.userId
				+ ", entity : " + userQuestionAttempt.parentEntity);
		addAttempt(userQuestionAttempt.userId, orgId, EntityType.QUESTION,
				question._getStringId(), Arrays.asList(question._getStringId()),
				userQuestionAttempt.parentEntity, System.currentTimeMillis());
		entityUserActionUtils.addEntityUserAction(userQuestionAttempt.userId,
				userQuestionAttempt.parentEntity, UserActionType.ATTEMPTED, false);

		// update UserQuestionAnalytics
		UserQuestionAnalytics userQuestionAnalytics =
				analyticsUtils.addUserQuestionAnalytics(userQuestionAttempt);
		logger.debug("finalizeQuestionAttempt userQuestionAnalytics: " + userQuestionAnalytics);

		int attempts = 1;
		int correct = 0;
		int incorrect = 0;
		int left = 0;
		int partial = 0;
		if (CollectionUtils.isNotEmpty(userQuestionAttempt.answerGiven)) {
			switch (userQuestionAttempt.isCorrect) {
				case CORRECT:
					correct = 1;
					break;
				case INCORRECT:
					incorrect = 1;
					break;
				case PARTIAL:
					partial = 1;
					break;
			}
		} else {
			left = 1;
		}
		final double score = userQuestionAttempt.score;
		final long timeTaken = userQuestionAttempt.timeTaken;

		final EntityMeasures measures = new EntityMeasures(attempts, correct, partial, incorrect, left,
				timeTaken, score);

		return finalizeQuestionAttempt(userQuestionAttempt.userId, question,
				userQuestionAttempt.parentEntity, measures, userQuestionAttempt.answerGiven,
				userQuestionAttempt.matrixAnswerGiven, userQuestionAttempt.attemptId);

	}

	public UserQuestionAttempt addAttempt(String userId, String attemptId, SrcEntity parentEntity, String qId,
										  List<String> answerGiven, Map<String, List<String>> matrixAnswerGiven, QuestionType type,
										  boolean isJudgeable, AnswerCorrectness isCorrect, int score, long timeTaken) {

		UserQuestionAttempt userQuestionAttempt = new UserQuestionAttempt(userId, attemptId, parentEntity, qId,
				answerGiven, isJudgeable, isCorrect, score, timeTaken);
		userQuestionAttempt.matrixAnswerGiven = matrixAnswerGiven;
		userQuestionAttempt.type = type;

		userQuestionAttemptRepo.save(userQuestionAttempt);
		logger.info("addAttempt saved userQuestionAttempt: " + userQuestionAttempt);

		return userQuestionAttempt;
	}


	private boolean finalizeQuestionAttempt(String userId, Question question,
											SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
											Map<String, List<String>> matrixAnswerGiven, String attemptId) {

		Set<AcademicDimension> acadDims = analyticsUtils.getAcadDimensions(question.boardIds);
		return finalizeQuestionAttempt(userId, question, parentEntity, measures, answerGiven,
				matrixAnswerGiven, acadDims);
	}

	private boolean finalizeQuestionAttempt(String userId, Question question,
											SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
											Map<String, List<String>> matrixAnswerGiven, Set<AcademicDimension> acadDims) {

		// NOTE: can be used for LEFT

		// update QuestionAnalytics
		final String answerGivenKey = AnswerGivenCount.toAnswerKey(question, answerGiven,
				matrixAnswerGiven);

		boolean addedQuestionAnalytics = addAnalytics(
				question._getStringId(), parentEntity, measures, answerGivenKey);
		logger.debug("finalizeQuestionAttempt addedQuestionAnalytics: " + addedQuestionAnalytics);

		// update UserAnalytics
		for (AcademicDimension acadDim : acadDims) {
			boolean added = addAnalytics(userId, acadDim.type,
					acadDim.id, measures);
			logger.debug("finalizeQuestionAttempt acadDim: " + acadDim + ", added: " + added);
		}

		return true;
	}

	public boolean addAnalytics(String qId, SrcEntity parentEntity, QuestionMeasures measures,
								String answerGivenKey) {
		Criteria criteria = new Criteria();
		Query query = new Query();
		criteria.and("qId").is(qId);
		criteria.and("parentEntity.type").is(parentEntity.type);
		criteria.and("parentEntity.id").is(parentEntity.id);
		query.addCriteria(criteria);
		QuestionAnalytics questionAnalytics = (QuestionAnalytics) mongoTemplate.find(query, QuestionAnalytics.class);

		questionAnalytics.getMeasures().setAttempts(questionAnalytics.getMeasures().getAttempts() + measures.getAttempts());
		questionAnalytics.getMeasures().setCorrect(questionAnalytics.getMeasures().getCorrect() + measures.getCorrect());
		questionAnalytics.getMeasures().setPartial(questionAnalytics.getMeasures().getPartial() + measures.getPartial());
		questionAnalytics.getMeasures().setIncorrect(questionAnalytics.getMeasures().getIncorrect() + measures.getIncorrect());
		questionAnalytics.getMeasures().setLeft(questionAnalytics.getMeasures().getLeft() + measures.getLeft());
		questionAnalytics.getMeasures().setTimeTaken(questionAnalytics.getMeasures().timeTaken + measures.timeTaken);
		if (!(answerGivenKey).isEmpty()) {
			// . is replace with _ as map key can not contain . in mongo
//	            answerGivenKey = answerGivenKey.replace(".", AnswerGivenCount.NUMERIC_DOT_REPLACER);
			questionAnalytics.getAnswerGivenCount().put(answerGivenKey, measures.getAttempts());
//	            updateOps.inc("answerGivenCount." + answerGivenKey, measures.attempts);
		}
		questionAnalyticsRepo.save(questionAnalytics);
		return true;
	}

	public boolean addAnalytics(String userId,
								AcademicDimensionType acadDimType, String acadDimId,
								EntityMeasures measures) {

		Criteria criteria = new Criteria();
		Query query = new Query();
		criteria.and("userId").is(userId);
		criteria.and("acadDim.type").is(acadDimType);
		criteria.and("acadDim.id").is(acadDimId);
		query.addCriteria(criteria);
		UserAnalytics userAnalytics = (UserAnalytics) mongoTemplate.find(query, UserAnalytics.class);
		userAnalytics.getMeasures().setAttempts(userAnalytics.getMeasures().getAttempts() + measures.attempts);
		userAnalytics.getMeasures().setCorrect(userAnalytics.getMeasures().getCorrect() + measures.correct);
		userAnalytics.getMeasures().setIncorrect(userAnalytics.getMeasures().getIncorrect() + measures.incorrect);
		userAnalytics.getMeasures().setLeft(userAnalytics.getMeasures().getLeft() + measures.left);
		userAnalytics.getMeasures().setScore(userAnalytics.getMeasures().getScore() + measures.score);
		userAnalytics.getMeasures().setTimeTaken(userAnalytics.getMeasures().getTimeTaken() + measures.timeTaken);
		userAnalyticsRepo.save(userAnalytics);
		return true;
	}

	private void finalizeAssignmentAttempt(UserQuestionAttempt userQuestionAttempt,
										   Question question, String orgId) throws VedantuException {

		UserEntityAttempt entityAttempt = getAttempt(
				userQuestionAttempt.userId, userQuestionAttempt.parentEntity.type,
				userQuestionAttempt.parentEntity.id);
		if (entityAttempt == null) {
			Optional<Assignment> assignmentOptional = assignmentRepo.findById(userQuestionAttempt.parentEntity.id);
			Assignment assignment = assignmentOptional.get();
			List<String> qIds = assignment.__getAllQIds();
			addEntityAttempt(userQuestionAttempt.userId, orgId, userQuestionAttempt.parentEntity.type,
					userQuestionAttempt.parentEntity.id, qIds, null, true, 0, 0, true, assignment,
					EventType.INDEX_ASSIGNMENT);
		}

		int attempts = 1;
		int correct = 0;
		int incorrect = 0;
		int left = 0;
		int partial = 0;
		if (CollectionUtils.isNotEmpty(userQuestionAttempt.answerGiven)) {
			switch (userQuestionAttempt.isCorrect) {
				case CORRECT:
					correct = 1;
					break;
				case INCORRECT:
					incorrect = 1;
					break;
				case PARTIAL:
					partial = 1;
					break;
			}
		} else {
			left = 1;
		}
		final double score = userQuestionAttempt.score;
		final long timeTaken = userQuestionAttempt.timeTaken;

		final EntityMeasures measures = new EntityMeasures(attempts, correct, partial, incorrect, left,
				timeTaken, score);

		// update UserQuestionAnalytics
		UserQuestionAnalytics userQuestionAnalytics = analyticsUtils
				.addUserQuestionAnalytics(userQuestionAttempt);
		logger.info("userQuestionAnalytics: " + userQuestionAnalytics);
		Set<AcademicDimension> acadDims = analyticsUtils.getAcadDimensions(question.boardIds);
		if (entityAttempt != null) {
			entityAttempt.endTime = System.currentTimeMillis();
			userEntityAttemptRepo.save(entityAttempt);
		}
		finalizeQuestionAttempt(userQuestionAttempt.userId, question,
				userQuestionAttempt.parentEntity, measures, userQuestionAttempt.answerGiven,
				userQuestionAttempt.matrixAnswerGiven, acadDims);
		for (AcademicDimension acadDim : acadDims) {
			analyticsUtils.updateUserEntityAnalytics(userQuestionAttempt.userId,
					userQuestionAttempt.parentEntity, acadDim.type, acadDim.id, measures, 0, orgId);
		}
	}

	public ResetQuestionAttemptRes resetQuestionAttemptFromManager(
			ResetQuestionAttemptReq resetAttemptReq) throws VedantuException {

		String testStatus = entityStatus(resetAttemptReq.attemptId);
		if (testStatus.equals("FINISHED")) {
			logger.error("Entity is Finished");
			throw new VedantuException(VedantuErrorCode.TEST_ENDED);
		}
		if (testStatus.equals("PAUSED")) {
			logger.error("Entity is PAUSED");
			throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
		}
		if (testStatus.equals("RESUMED")) {
			logger.error("Entity is RESUMED");
			throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
		}

		resetAttemptReq.attemptId = (resetAttemptReq.attemptId.isEmpty() || resetAttemptReq.attemptId == null) ? getAttemptId(
				resetAttemptReq.userId, resetAttemptReq.entityType, resetAttemptReq.entityId)
				: resetAttemptReq.attemptId;

		List<UserQuestionAttempt> attempts = getAttempts(
				resetAttemptReq.userId, resetAttemptReq.attemptId, resetAttemptReq.qId, null);
		if (CollectionUtils.isEmpty(attempts)) {
			logger.equals("no previous attempts found " + resetAttemptReq);
			throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
		}

		for (UserQuestionAttempt attempt : attempts) {
			attempt.isJudgeable = false;
			userQuestionAttemptRepo.save(attempt);
		}

		ResetQuestionAttemptRes reSetQuestionAttemptRes = new ResetQuestionAttemptRes(true,
				attempts.size());

		return reSetQuestionAttemptRes;
	}

	public List<UserQuestionAttempt> getAttempts(String userId, String attemptId, String qId,
												 Boolean isJudgeable) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("userId").is(userId);
		criteria.and(ConstantsGlobal.QID).is(qId);
		criteria.and(ConstantsGlobal.ATTEMPT_ID).is(attemptId);
		if (isJudgeable != null) {
			criteria.and("isJudgeable").is(isJudgeable.booleanValue());

		}
		query.addCriteria(criteria);
		List<UserQuestionAttempt> userQuestionAttempts = mongoTemplate.find(query, UserQuestionAttempt.class);

		logger.info("getAttempts userQuestionAttempts.sizes: "
				+ CollectionUtils.size(userQuestionAttempts));

		return userQuestionAttempts;
	}

	public GetQuestionAnalyticsRes getQuestionAnalyticsFromManager(
			GetQuestionAnalyticsReq getQuestionAnalyticsReq) throws VedantuException {

		SrcEntity parentEntity = new SrcEntity(getQuestionAnalyticsReq.entityType,
				getQuestionAnalyticsReq.entityId);

		QuestionAnalytics questionAnalytics = getAnalytics(
				getQuestionAnalyticsReq.qId, parentEntity);

		if (null == questionAnalytics) {
			logger.error("questionAnalytics not found for qId: " + getQuestionAnalyticsReq.qId
					+ ", parentEntity: " + parentEntity);
			throw new VedantuException(VedantuErrorCode.ANALYTICS_NOT_FOUND);
		}

		Question question = getQuestion(getQuestionAnalyticsReq.qId);

		if (null == question) {
			logger.error("question not found for qId: " + getQuestionAnalyticsReq.qId
					+ ", parentEntity: " + parentEntity);
			throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
		}

		QuestionAnalyticsExtendedInfo quesAnalyticsExtendedInfo = (QuestionAnalyticsExtendedInfo) questionAnalytics
				.toExtendedInfo();
		quesAnalyticsExtendedInfo.type = question.type;

		// set users answer
		UserQuestionAnalytics userQuestionAnalytics =
				getAnalytics(getQuestionAnalyticsReq.userId, parentEntity,
						getQuestionAnalyticsReq.qId);
		if (null != userQuestionAnalytics) {
			quesAnalyticsExtendedInfo.userAnswerGivenCount.answerGiven = userQuestionAnalytics.answerGiven;
			quesAnalyticsExtendedInfo.isUserAnswerCorrect = userQuestionAnalytics.isCorrect;
		}

		if (question.type.isJudgeable()) {
			// set correct answer count
			Answer answer = getQuestionAnswer(getQuestionAnalyticsReq.qId);
			if (null != answer && CollectionUtils.isNotEmpty(answer.answer)) {
				quesAnalyticsExtendedInfo.correctAnswerGivenCount.answerGiven = answer.answer;

				quesAnalyticsExtendedInfo.correctAnswerGivenCount.count = questionAnalytics
						.findAnswerCount(question, answer.answer, answer.matrixAnswer);
			}

			// set user answer count
			if (null != userQuestionAnalytics) {
				quesAnalyticsExtendedInfo.userAnswerGivenCount.count = questionAnalytics
						.findAnswerCount(question, userQuestionAnalytics.answerGiven,
								userQuestionAnalytics.matrixAnswerGiven);
			}
		}

		GetQuestionAnalyticsRes getQuestionAnalyticsRes = new GetQuestionAnalyticsRes();
		getQuestionAnalyticsRes.info = quesAnalyticsExtendedInfo;

		return getQuestionAnalyticsRes;
	}

	public QuestionAnalytics getAnalytics(String qId, SrcEntity parentEntity) {

		logger.debug("getAnalytics qId: " + qId + ", parentEntity: ");
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("qId").is(qId);
		criteria.and("parentEntity.type").is(parentEntity.type);
		criteria.and("parentEntity.id").is(parentEntity.id);
		query.addCriteria(criteria);
		QuestionAnalytics questionAnalytics = mongoTemplate.findOne(query, QuestionAnalytics.class);
		logger.info("getAnalytics questionAnalytics: " + questionAnalytics);
		if (questionAnalytics != null) {
			Map<String, Long> answerGivenCountMap = new TreeMap<String, Long>();
			for (Entry<String, Long> entry : questionAnalytics.answerGivenCount.entrySet()) {
				answerGivenCountMap.put(entry.getKey().replace(AnswerGivenCount.NUMERIC_DOT_REPLACER, "."),
						entry.getValue());
			}
			questionAnalytics.answerGivenCount = answerGivenCountMap;
		}
		return questionAnalytics;
	}

	public UserQuestionAnalytics getAnalytics(String userId, SrcEntity parentEntity, String qId) {

		logger.debug("getAnalytics userId: " + userId + ", parentEntity: " + parentEntity + ", qId: " + qId);
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
}

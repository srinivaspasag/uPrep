package com.lms.services.serviceImpl;

import com.lms.board.model.Board;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.AnalyticsComponent;
import com.lms.components.ContentComponent;
import com.lms.components.QuestionComponent;
import com.lms.enums.*;
import com.lms.event.details.EndTestDetails;
import com.lms.managers.AbstractContentManager;
import com.lms.models.*;
import com.lms.models.analytics.*;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.models.tests.Assignment;
import com.lms.pojo.*;
import com.lms.pojos.StudentSubjectWiseResult;
import com.lms.pojos.TestMetadata;
import com.lms.pojos.UserAnalyticsResult;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.lms.pojos.requests.analytics.*;
import com.lms.pojos.responce.GetEntityResultAnalyticsRes;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.analytics.*;
import com.lms.pojos.responce.analytics.answers.QuestionAttemptStatsInfo;
import com.lms.pojos.responce.questions.GetSolutionsRes;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.pojos.tests.BoardQus;
import com.lms.pojos.tests.Marks;
import com.lms.pojos.tests.QuestionResultStatus;
import com.lms.pojos.tests.TestQuestionSet;
import com.lms.repository.*;
import com.lms.services.AnalyticsService;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserExtendedInfo;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.user.vedantu.user.pojo.responce.GetUserSelfFullProfileRes;
import com.lms.utils.EntityUserActionUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);
    private final String ACAD_DIM_TYPE = "acadDim.type";
    private final String ACAD_DIM_ID = "acadDim.id";
    public boolean autoResumeTest = false;
    @Autowired
    UserEntityAttemptRepo userEntityAttemptRepo;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    TestRepo testRepo;
    @Autowired
    OrgCenterRepo orgCenterRepo;
    @Autowired
    OrgProgramRepo orgProgramRepo;
    @Autowired
    OrgSectionRepo orgSectionRepo;
    @Autowired
    OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrgDepartmentRepo orgDepartmentRepo;
    @Autowired
    private UserQuestionAnalyticsRepo userQuestionAnalyticsRepo;
    @Autowired
    private EntityUserActionMappingRepo entityUserActionMappingRepo;
    @Autowired
    private UserEntityAnalyticsRepo userEntityAnalyticsRepo;
    @Autowired
    private UserQuestionAttemptRepo userQuestionAttemptRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private AnswerRepo answerRepo;
    @Autowired
    private EntityUserActionUtils entityUserActionUtils;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private UserAnalyticsRepo userAnalyticsRepo;

    @Autowired
    private AssignmentRepo assignmentRepo;
    @Autowired
    private EntityAnalyticsRepo entityAnalyticsRepo;
    @Autowired
    private QuestionAnalyticsRepo questionAnalyticsRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private AnalyticsComponent analyticsComponent;

    @Autowired
    private QuestionComponent questionComponent;

    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private ContentComponent contentComponent;

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

    public String toAnswerKey(Question question, List<String> answers,
                              Map<String, List<String>> matrixAnswerGiven) {
        String ANSWER_KEY_SEPARATOR = "_";
        String NUMERIC_DOT_REPLACER = "#";

        if (answers == null || null == question || null == question.type
                || !question.type.isJudgeable()) {
            return null;
        } else {
            String key = answers + ANSWER_KEY_SEPARATOR;
//            String key = (question.type != QuestionType.MATRIX ? StringUtils.join(answers,
//                    ANSWER_KEY_SEPARATOR) : toAnswerKey(matrixAnswerGiven));
            return StringUtils.isEmpty(key) ? null : key.replace(".", NUMERIC_DOT_REPLACER);
        }
    }


    public Set<AcademicDimension> getAcadDimensionsSubset(Set<String> brdIds,
                                                          Map<String, AcademicDimension> acadDimMap) {

        Set<AcademicDimension> acadDims = new HashSet<AcademicDimension>();
        acadDims.add(acadDimMap.get(AcademicDimensionType.OVERALL.name()));
        for (String brdId : brdIds) {
            AcademicDimension acadDim = acadDimMap.get(brdId);
            if (acadDim != null) {
                acadDims.add(acadDim);
            }
        }
        return acadDims;
    }

    private double calculatePartialScore(List<String> answerGiven, Set<String> correctAnswers, String qId, double partialScore) {
        double score = 0;
        for (String ans : answerGiven) {
            if (correctAnswers.contains(ans)) {
                score += partialScore;
            }
        }
        return score;
    }

    private void updateEntityMeasuresMap(
            Map<String, EntityMeasures> boardwiseEntityMeasuresMap, String brdId,
            EntityMeasures questionMeasures) {

        EntityMeasures entityMeasures = boardwiseEntityMeasuresMap.get(brdId);
        if (entityMeasures == null) {
            entityMeasures = new EntityMeasures();
            boardwiseEntityMeasuresMap.put(brdId, entityMeasures);
        }
        entityMeasures.attempts += questionMeasures.attempts;
        entityMeasures.correct += questionMeasures.correct;
        entityMeasures.partial += questionMeasures.partial;
        entityMeasures.incorrect += questionMeasures.incorrect;
        entityMeasures.left += questionMeasures.left;
        entityMeasures.score += questionMeasures.score;
        entityMeasures.timeTaken += questionMeasures.timeTaken;
    }

    private boolean isMultiAttemptAllowed(StartAttemptReq startAttemptReq) {

        return false;
    }

    private SrcEntity getParentAndUpdateQIds(StartAttemptReq startAttemptReq,
                                             List<String> qIds, AbstractTestCommonModel test) {

        SrcEntity parent = null;
        if (startAttemptReq.entityType == EntityType.TEST
                && !(startAttemptReq.entityId).isEmpty()) {
            if (!(test.parentId).isEmpty()) {
                parent = new SrcEntity(EntityType.TEST, test.parentId);
            }
            boolean addedQids = false;
            if (!(startAttemptReq.setName).isEmpty() || (startAttemptReq.setName != null)) {
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

    private GetUserEntityMeasuresRes populateUserEntityMeasures(int totalQus,
                                                                UserEntityAnalytics userEntityAnalytics) {

        GetUserEntityMeasuresRes res = new GetUserEntityMeasuresRes();
        QuestionMeasures measures = new QuestionMeasures();
        measures.left = totalQus;
        res.measures = measures;
        if (userEntityAnalytics != null) {
            measures.correct = userEntityAnalytics.measures.correct;
            measures.partial = userEntityAnalytics.measures.partial;
            measures.incorrect = userEntityAnalytics.measures.incorrect;
            res._finalizeMeasures();
            res.lastAttempted = userEntityAnalytics.lastUpdated;
        }
        return res;
    }

    @Override
    public VedantuResponse testStatus(StartAttemptReq recordAttemptReq) {
        RecordAttemptRes res = new RecordAttemptRes();
        res.isOnline = true;
        UserEntityAttempt testStatus = _entityStatus(recordAttemptReq.attemptId);
        if (testStatus.testStatus.equals("FINISHED")) {
            logger.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }
        if (testStatus.testStatus.equals("PAUSED")) {
            logger.error("Entity is PAUSED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED);
        }
        if (testStatus.testStatus.equals("RESUMED")) {
            logger.error("Entity is RESUMED");
            throw new VedantuException(VedantuErrorCode.TEST_PAUSED_RESUME_AGAIN);
        }
        testStatus.timeLeft = recordAttemptReq.timeLeft;
        userEntityAttemptRepo.save(testStatus);
        return new VedantuResponse(res);
    }

    private UserEntityAttempt _entityStatus(String attemptId) throws VedantuException {
        // TODO Auto-generated method stub
        UserEntityAttempt user = userEntityAttemptRepo.findById(attemptId).get();
        return user;
    }

    @Override
    public VedantuResponse _testStatus(GetTestInfoReq getTestReq) {

        logger.debug("Inside _testStatus");
        GetTestInfoRes testStatus = new GetTestInfoRes();
        logger.debug("_testStatus log :: userId is " + getTestReq.userId);
        logger.debug("_testStatus log :: TestId is " + getTestReq.id);
        UserEntityAttempt userEntityAttempt = getAttempt(getTestReq.userId, EntityType.TEST, getTestReq.id);
        if (userEntityAttempt == null) {
            logger.debug("_testStatus log :: userEntityAttempt is null");
            testStatus.testStatus = "NOT_ATTEMPTED";
            return new VedantuResponse(testStatus);
        } else {
            logger.debug("_testStatus log :: userEntityAttempt is not null");
            if (testRepo.findById(getTestReq.id).get().autoResumeTest && userEntityAttempt.testStatus.equals("ONGOING")) {
                userEntityAttempt.testStatus = "RESUMED";
                userEntityAttemptRepo.save(userEntityAttempt);
                testStatus.testStatus = userEntityAttempt.testStatus;
            } else {
                testStatus.testStatus = userEntityAttempt.testStatus;
            }
            testStatus.processed = userEntityAttempt.processed;
            return new VedantuResponse(testStatus);
        }
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

    @Override
    public VedantuResponse endStudentAttempt(StartAttemptReq endAttemptReq, long endTime) {
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        String attemptId = getAttemptId(endAttemptReq.studentUserId, endAttemptReq.entityType, endAttemptReq.entityId);

        EndAttemptReq req = new EndAttemptReq(endAttemptReq.studentUserId, endAttemptReq.studentUserId,
                endAttemptReq.entityId, endAttemptReq.getEntityType(), endAttemptReq.getSetName(), attemptId,
                endAttemptReq.orgId);

        return new VedantuResponse(endAttempt(req, endTime, false));

    }

    private String getAttemptId(String userId, EntityType entityType, String entityId) {
        // TODO Auto-generated method stub
        String attemptId = getAttempt(userId, entityType, entityId)._getStringId();
        return attemptId == null ? "" : attemptId;
    }

    public EndAttemptRes endAttempt(EndAttemptReq endAttemptReq, long endTime,
                                    boolean ignoreEndTime) throws VedantuException {

        if (!isEntityAttemptAllowed(endAttemptReq)) {
            logger.error("not allowed end attempt of entity: " + endAttemptReq.entityType);
            throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
        }

        String testStatus = entityStatus(endAttemptReq.attemptId);
        if (testStatus.equals("FINISHED")) {
            logger.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }

        endAttemptReq.attemptId = (endAttemptReq.attemptId.isEmpty() || endAttemptReq.attemptId == null) ? getAttemptId(
                endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId)
                : endAttemptReq.attemptId;

        // Implement a logic to check whether the given endTime is correct or
        // not and provide with near to correct endTime if it is wrong
        long duration = testRepo.findById(endAttemptReq.entityId).get().duration;
        UserEntityAttempt attempt = userEntityAttemptRepo.findById(endAttemptReq.attemptId).get();
        if (endTime == 0 || endTime < attempt.timeCreated) {
            endTime = System.currentTimeMillis();
        }
        if ((attempt.timeCreated - endTime) > duration) {
            endTime = attempt.timeCreated + duration;
        }
//        endTime  = getTestEndTime(endTime, endAttemptReq.entityId, endAttemptReq.attemptId, endAttemptReq.userId, duration);

        EndAttemptRes endAttemptRes = new EndAttemptRes();
        synchronized (endAttemptReq.entityType + endAttemptReq.entityId + endAttemptReq.userId) {

            UserEntityAttempt userEntityAttempt = endAttempt(
                    endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId,
                    endAttemptReq.attemptId, endTime, true, ignoreEndTime);
            if (userEntityAttempt.parent != null) {
                endAttempt(endAttemptReq.userId,
                        userEntityAttempt.parent.type, userEntityAttempt.parent.id,
                        endAttemptReq.attemptId);
            }
            logger.debug("Test Time ended is: " + userEntityAttempt.endTime);
            logger.debug("Test Time created is: " + userEntityAttempt.timeCreated);

            endAttemptRes.info = userEntityAttempt.toBasicInfo();
            if (endAttemptReq.entityType == EntityType.TEST) {
                // generate an endTest event so that, Server wont get parallel requests to process all end tests at once.
                // With this, we shall generate analytics in first come first serve basis
                EndTestDetails endTestDetails = new EndTestDetails(userEntityAttempt._getStringId(),
                        userEntityAttempt.userId, userEntityAttempt.entity.id,
                        userEntityAttempt.entity.type, endAttemptReq.setName,
                        userEntityAttempt.timeCreated, duration, endAttemptReq.orgId, "USER");
                long processTime = userEntityAttempt.timeCreated;
               questionComponent.generateEventAysc(userEntityAttempt.userId, endTestDetails, EventType.END_TEST,
                       processTime);
            }
        }

        return endAttemptRes;
    }

    private String entityStatus(String attemptId) throws VedantuException {
        // TODO Auto-generated method stub
        UserEntityAttempt user = _entityStatus(attemptId);
        if (user == null) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        return user.testStatus;
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
                                        String attemptId, long endTime, boolean checkIfAlreadyEnded, boolean ignoreEndTime)
            throws VedantuException {

        logger.debug("endAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId + ", attemptId: " + attemptId);

        UserEntityAttempt userEntityAttempt = userEntityAttemptRepo.findById(attemptId).get();
        if (null == userEntityAttempt) {
            logger.error("endAttempt userEntityAttempt not found for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        if (checkIfAlreadyEnded
                && (userEntityAttempt.finished || (!ignoreEndTime && userEntityAttempt.endTime > 0 && userEntityAttempt.endTime <= endTime))) {
            logger.error("endAttempt userEntityAttempt already ended for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }

        userEntityAttempt.endTime = endTime > 0 ? endTime : System.currentTimeMillis();
        userEntityAttempt.finished = true;
        userEntityAttempt.testStatus = "FINISHED";
        userEntityAttemptRepo.save(userEntityAttempt);

        logger.info("endAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
                                        String attemptId) throws VedantuException {

        return endAttempt(userId, entityType, entityId, attemptId, 0);
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
                                        String attemptId, long endTime) throws VedantuException {

        return endAttempt(userId, entityType, entityId, attemptId, endTime, false);
    }

    public UserEntityAttempt endAttempt(String userId, EntityType entityType, String entityId,
                                        String attemptId, long endTime, boolean checkIfAlreadyEnded) throws VedantuException {

        return endAttempt_(userId, entityType, entityId, attemptId, endTime, checkIfAlreadyEnded,
                false);
    }

    public UserEntityAttempt endAttempt_(String userId, EntityType entityType, String entityId,
                                         String attemptId, long endTime, boolean checkIfAlreadyEnded, boolean ignoreEndTime)
            throws VedantuException {

        logger.debug("endAttempt userId: " + userId + ", entityType: " + entityType + ", entityId"
                + entityId + ", attemptId: " + attemptId);

        UserEntityAttempt userEntityAttempt = userEntityAttemptRepo.findById(attemptId).get();
        if (null == userEntityAttempt) {
            logger.error("endAttempt userEntityAttempt not found for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        if (checkIfAlreadyEnded
                && (userEntityAttempt.finished || (!ignoreEndTime && userEntityAttempt.endTime > 0 && userEntityAttempt.endTime <= endTime))) {
            logger.error("endAttempt userEntityAttempt already ended for attemptId: " + attemptId);
            throw new VedantuException(VedantuErrorCode.ALREADY_ATTEMPTED);
        }

        userEntityAttempt.endTime = endTime > 0 ? endTime : System.currentTimeMillis();
        userEntityAttempt.finished = true;
        userEntityAttempt.testStatus = "FINISHED";
        userEntityAttemptRepo.save(userEntityAttempt);

        logger.info("endAttempt userEntityAttempt: " + userEntityAttempt);

        return userEntityAttempt;
    }

    @Override
    public VedantuResponse resumeStudentTest(StartAttemptReq resumeAttemptReq) {
        // TODO Auto-generated method stub
        if (resumeAttemptReq.entityType.equals(EntityType.TEST)) {
            if (resumeAttemptReq.userId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            } else if (resumeAttemptReq.entityId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
            // Remove entry from userEntityAttempts
            UserEntityAttempt userEntityAttempt = getAttempt(resumeAttemptReq.studentUserId, resumeAttemptReq.entityType, resumeAttemptReq.entityId);
            if (userEntityAttempt.finished) {
                throw new VedantuException(VedantuErrorCode.TEST_ENDED);
            } else {
                userEntityAttempt.testStatus = "RESUMED";
            }
            userEntityAttemptRepo.save(userEntityAttempt);
        } else {
            logger.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }
        return null;
    }

    @Override
    public VedantuResponse getStudentsListFromEntityAttempts(GetEntityResultAnalyticsReq req) {
        // TODO Auto-generated method stub
        GetEntityAttemptAnalyticsRes res = new GetEntityAttemptAnalyticsRes();
        List<GetEntityAttemptsStudentsListRes> listres = new ArrayList<GetEntityAttemptsStudentsListRes>();
        AtomicLong totalHits = new AtomicLong();
        Test test = testRepo.findById(req.entity.id).get();
        if (test == null) {
            logger.error("no entity found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        List<String> studentUserIds = new ArrayList<String>();
        if (!(req.queryText).isEmpty()) {
            List<OrgMember> orgMembers = getUserIdsFromMemberIdAndNameMatch(req.orgId, req.queryText);
            for (OrgMember orgmember : orgMembers) {
                studentUserIds.add(orgmember.userId);
            }
        }
        res.testProgressCount = getUserTestAttemptStatusCount(req.orgId, req.entity.id, req.entity.type, "ONGOING");
        res.testCompletedCount = getUserTestAttemptStatusCount(req.orgId, req.entity.id, req.entity.type, "FINISHED");
        res.testPausedCount = getUserTestAttemptStatusCount(req.orgId, req.entity.id, req.entity.type, "PAUSED");
        res.testResumedCount = getUserTestAttemptStatusCount(req.orgId, req.entity.id, req.entity.type, "RESUMED");
        if (!(req.queryText).isEmpty()) {
            if (studentUserIds.isEmpty()) {
                res.list = listres;
                return new VedantuResponse(res);
            }
        }
        List<UserEntityAttempt> entityUserAttempts = getUserAttemptsList(
                req.entity.type, req.entity.id, req.orgId, req.start, req.size, totalHits, studentUserIds);
        if (entityUserAttempts == null) {
            logger.error("no attempts found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }

        Set<String> userIds = new HashSet<String>();
        for (UserEntityAttempt user : entityUserAttempts) {
            userIds.add(user.userId);
        }

        Map<String, ModelBasicInfo> userIdToBasicInfoMap = getUserInfoMap(req.orgId, userIds);

        for (UserEntityAttempt user : entityUserAttempts) {
            GetEntityAttemptsStudentsListRes resp = new GetEntityAttemptsStudentsListRes();
            if (!user.userId.equals("PUBLIC")) {
                resp.memberId = orgMemberRepo.findByUserId(user.userId).memberId;
                resp.user = (UserInfo) userIdToBasicInfoMap.get(user.userId);
                resp.testStatus = user.testStatus;
                resp.startTime = user.timeCreated;
                resp.processed = user.processed;
                listres.add(resp);
            }
        }
        res.list = listres;
        res.totalhits = totalHits.longValue();
        return new VedantuResponse(res);
    }

    public List<OrgMember> getUserIdsFromMemberIdAndNameMatch(String orgId, String queryText) {
        // TODO Auto-generated method stub
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("orgId").is(orgId);
        criteria.and("profile").is(OrgMemberProfile.STUDENT);
        if (!(queryText).isEmpty()) {
            criteria.orOperator(
                    Criteria.where(ConstantsGlobal.FIRST_NAME).regex(
                            ".*" + queryText.trim() + ".*", "i"), Criteria.where(ConstantsGlobal.LAST_NAME).regex(".*" + queryText.trim() + ".*", "i"),
                    Criteria.where(ConstantsGlobal.MEMBER_ID)
                            .regex(".*" + queryText.trim() + ".*", "i"));

        }
        query.addCriteria(criteria);
        List<OrgMember> orgMemberList = mongoTemplate.find(query, OrgMember.class);
        return orgMemberList;
    }

    public long getUserTestAttemptStatusCount(String orgId, String entityId, EntityType entityType, String testStatus) {
        // TODO Auto-generated method stub
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("entity.type").is(entityType);
//        criteria.and("entity.id").is(entityId);
        criteria.and("orgId").is(orgId);
        criteria.and("testStatus").is(testStatus);
        query.addCriteria(criteria);
        List<UserEntityAttempt> userEntityAttemptList = mongoTemplate.find(query, UserEntityAttempt.class);
        long count = userEntityAttemptList.size();
        return count;
    }

    public List<UserEntityAttempt> getUserAttemptsList(EntityType entityType, String entityId, String orgId, int start, int size, AtomicLong hits, List<String> userIds) {

        logger.debug("getAttempts entityType: " + entityType + ", entityId"
                + entityId);
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("entity.type").is(entityType);
//        criteria.and("entity.id").is(entityId);
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

    private Map<String, ModelBasicInfo> populateOrgMemberInfo(List<OrgMember> orgMembers,
                                                              boolean excludeOrgMappingInfo) {

        Set<String> centerIds = new HashSet<String>();
        Set<String> sectionIds = new HashSet<String>();
        Set<String> programIds = new HashSet<String>();

        if (!excludeOrgMappingInfo) {
            for (OrgMember orgMember : orgMembers) {
                if (orgMember.mappings == null) {
                    continue;
                }
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    programIds.add(mapping.programId);
                    centerIds.add(mapping.centerId);
                    sectionIds.add(mapping.sectionId);
                    // if (mapping.courseIds != null) {
                    // courseIds.addAll(mapping.courseIds);
                    // }
                }
            }
        }

        logger.debug("programIds : " + programIds + " excludeMappingInfo : "
                + excludeOrgMappingInfo);
        logger.debug("centerIds : " + centerIds);
        logger.debug("sectionIds : " + sectionIds);
        Map<String, ModelBasicInfo> orgComponentBasicInfoMap = new HashMap<String, ModelBasicInfo>();
        // collect program info
        if (!excludeOrgMappingInfo) {

            List<OrgProgram> orgPrograms = orgProgramRepo.findAllByIdIn(programIds);
            if (!orgPrograms.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicInfosMap(orgPrograms));


            // collect center info
            List<OrgCenter> orgCenters = orgCenterRepo.findAllByIdIn(centerIds);
            if (!orgCenters.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicCenterInfosMap(orgCenters));

            // collect section info

            List<OrgSection> orgSections = orgSectionRepo.findAllByIdIn(sectionIds);
            if (!orgSections.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicSectionInfosMap(orgSections));


        }
        Map<String, ModelBasicInfo> userInfoMap = new HashMap<String, ModelBasicInfo>();

        for (OrgMember orgMember : orgMembers) {
            OrgMemberBasicInfo orgMemberBasicInfo = (OrgMemberBasicInfo) orgMember.toBasicInfo();
            if (!excludeOrgMappingInfo && orgMember.mappings != null) {
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    OrgStructureBasicInfo program = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.programId);
                    logger.debug("programId : " + mapping.programId);
                    if (program == null) {
                        continue;
                    }

                    OrgProgramBasicInfo programInfo = orgMemberBasicInfo.mappings
                            ._getOrAddProgram(program);

                    OrgStructureBasicInfo progCenter = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.centerId);
                    OrgProgramCenterBasicInfo progCenterInfo = programInfo
                            ._getOrAddProgramCenter(progCenter);

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.sectionId);
                    OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo
                            ._getOrAddProgramSection(progSection);
                    logger.debug("OrgProgramSectionBasicInfo :" + progSectionInfo);
                }
            }
            userInfoMap.put(orgMember.userId, orgMemberBasicInfo);
        }

        return userInfoMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMap(List<OrgProgram> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgProgram orgProgram : results) {
                if (null == orgProgram) {
                    continue;
                }
                infosMap.put(orgProgram._getStringId(), (B) toProgramBasicInfo(orgProgram));

            }
        }
        return infosMap;
    }

    public ModelBasicInfo toProgramBasicInfo(OrgProgram orgProgram) {

        Optional<OrgDepartment> department = orgDepartmentRepo.findById(orgProgram.getDepartmentId().trim());
        if (!department.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE, "Department is not found");

        return new OrgProgramBasicInfo(orgProgram._getStringId(), orgProgram.getRecordState(), orgProgram.getcName(), orgProgram.getCode(),
                orgProgram._getEntityType(), orgProgram.getDepartmentId(), department.get().getName(), department.get().getCode(), orgProgram.getCourseIds(), orgProgram.isOffline);
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicCenterInfosMap(List<OrgCenter> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgCenter orgCenter : results) {
                if (null == orgCenter) {
                    continue;
                }
                infosMap.put(orgCenter._getStringId(), (B) new OrgStructureBasicInfo(orgCenter));

            }
        }
        return infosMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicSectionInfosMap(List<OrgSection> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgSection orgSection : results) {
                if (null == orgSection) {
                    continue;
                }
                infosMap.put(orgSection._getStringId(), (B) new OrgStructureBasicInfo(orgSection));

            }
        }
        return infosMap;
    }

    @Override
    public VedantuResponse getentityLeaderBoard(GetEntityLeaderBoardReq getEntityLeaderBoardReq) {
        if (getEntityLeaderBoardReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetEntityResultAnalyticsRes getEntityLeaderBoardRes = analyticsComponent.getEntityResultAnalytics(
                getEntityLeaderBoardReq, !getEntityLeaderBoardReq.miniInfo);

        return new VedantuResponse(getEntityLeaderBoardRes);
    }

    @Override
    public VedantuResponse getuserEntityAnalytics(GetUserEntityAnalyticsReq getUserEntityAnalyticsReq) {
        if (getUserEntityAnalyticsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetUserEntityResultAnalyticsSingleEntityRes getUserEntityAnalyticsRes = analyticsComponent.getUserEntityAnalytics(getUserEntityAnalyticsReq);
        return new VedantuResponse(getUserEntityAnalyticsRes);
    }

    @Override
    public VedantuResponse getuserEntityResultAnalytics(GetUserEntityResultAnalyticsReq getUserEntityResultAnalyticsReq) {
        if (getUserEntityResultAnalyticsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetUserEntityResultAnalyticsListRes getAnalyticsResultRes = analyticsComponent.getUserEntityResultAnalytics(getUserEntityResultAnalyticsReq);

        return new VedantuResponse(getAnalyticsResultRes);
    }

    @Override
    public VedantuResponse getuserEntityQuestionAttempts(GetUserEntityQuestionAttemptStatsReq getUserEntityQuestionAttemptStatsReq) {
        if (getUserEntityQuestionAttemptStatsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetUserEntityQuestionAttemptInfoListRes getUserEntityQusAttamptsRes = analyticsComponent.getUserEntityQuestionAttemptInfos(getUserEntityQuestionAttemptStatsReq);

        return new VedantuResponse(getUserEntityQusAttamptsRes);
    }

    @Override
    public VedantuResponse getentityQuestionAttempts(GetEntityQuestionsAttemptStatReq getEntityQuestionsAttemptStatReq) {
        if (getEntityQuestionsAttemptStatReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetEntityQuestionAttemptInfoListRes getUserEntityQuestionAttamptsRes = analyticsComponent.getEntityQusAttemptInfoDetails(getEntityQuestionsAttemptStatReq);

        return new VedantuResponse(getUserEntityQuestionAttamptsRes);
    }

    @Override
    public VedantuResponse getstudentsQuestionsAnsweredList(GetQuestionAnalyticsReq getQuestionAnalyticsReq) {
        if (getQuestionAnalyticsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        List<UserQuestionAnalytics> getQuestionCorrectWrongStudentsList = analyticsComponent.getStudentAnalyticsList(getQuestionAnalyticsReq);
        List<UserQuestionAnalytics> finalRes = new ArrayList<UserQuestionAnalytics>();
        for (int i = 0; i < getQuestionCorrectWrongStudentsList.size(); i++) {
            UserQuestionAnalytics info = getQuestionCorrectWrongStudentsList.get(i);

            GetUserSelfFullProfileRes res = contentComponent.getUserFullProfile(info.userId);
            info.userName = res.info.firstName + " " + (StringUtils.isEmpty(res.info.lastName) ? "" : res.info.lastName);
            info.userProfilePic = res.info.thumbnail;
            finalRes.add(info);
        }
        return new VedantuResponse(finalRes);
    }

    @Override
    public VedantuResponse getentityMarkDistribution(GetEntityMarkDistributionReq getEntityMarkDistributionReq) {
        if (getEntityMarkDistributionReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetEntityMarkDistributionRes getEntityMarkDistributionRes = analyticsComponent.getEntityMarkDistribution(getEntityMarkDistributionReq);

        return new VedantuResponse(getEntityMarkDistributionRes);
    }

    @Override
    public VedantuResponse getentityScheduleAnalytics(GetEntityScheduleAnalyticsReq getEntityScheduleAnalyticsReq) {
        if (getEntityScheduleAnalyticsReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetEntityScheduleAnalyticsRes getEntityAnalyticsScheduleRes = analyticsComponent.getEntityAnalyticsSchedule(getEntityScheduleAnalyticsReq);

        return new VedantuResponse(getEntityAnalyticsScheduleRes);
    }

    @Override
    public VedantuResponse getentityScheduleInfo(GetEntityScheduleInfoReq getEntityScheduleInfoReq) {
        if (getEntityScheduleInfoReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ListResponse<EntityScheduleInfo> getEntityScheduleRes = analyticsComponent.getEntityScheduleInfoRes(
                getEntityScheduleInfoReq.orgId, getEntityScheduleInfoReq.entity.id);


        return new VedantuResponse(getEntityScheduleRes);

    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicUserInfosMap(List<User> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(results)) {
            for (User user : results) {
                if (null == user) {
                    continue;
                }
                infosMap.put(user._getStringId(), (B) new UserExtendedInfo(user));

            }
        }
        return infosMap;
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
                                                      Collection<String> userIds) {

        return getUserInfoMap(orgId, userIds, false);
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId, Collection<String> userIds,
                                                      boolean excludeOrgMappingInfo) {

        logger.info("getUserInfoMap orgId:" + orgId + ", userIds: " + userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<String, ModelBasicInfo>();
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        boolean isOrgReq = !StringUtils.isEmpty(orgId);

        if (isOrgReq) {
            criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
            criteria.and(ConstantsGlobal.USER_ID).in(userIds);

        } else {
            criteria.and(ConstantsGlobal._ID).in(userIds);
        }
        query.addCriteria(criteria);
        List<OrgMember> orgMemberList = mongoTemplate.find(query, OrgMember.class);
        List<User> users = mongoTemplate.find(query, User.class);

        Map<String, ModelBasicInfo> userIdToBasicInfoMap = isOrgReq
                ? populateOrgMemberInfo(orgMemberList, excludeOrgMappingInfo)
                : toBasicUserInfosMap(users);

        logger.debug("userIds map : " + userIdToBasicInfoMap);
        return userIdToBasicInfoMap;

    }

    @Override
    public VedantuResponse startAttempt(StartAttemptReq startAttemptReq) {
        if (ObjectIdUtils.hasInvalidId(startAttemptReq.entityId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        StartAttemptRes startAttemptRes = null;
        try {
            startAttemptRes = analyticsComponent.startAttempt(startAttemptReq, true);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(startAttemptRes);
    }

    @Override
    public VedantuResponse pauseStudentAttempt(StartAttemptReq pauseAttemptReq) {
        if (pauseAttemptReq.entityType.equals(EntityType.TEST)) {
            if (pauseAttemptReq.studentUserId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            } else if (pauseAttemptReq.entityId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
            // Remove entry from userEntityAttempts
            UserEntityAttempt userEntityAttempt = getAttempt(pauseAttemptReq.studentUserId, pauseAttemptReq.entityType, pauseAttemptReq.entityId);
            if (userEntityAttempt.finished) {
                throw new VedantuException(VedantuErrorCode.TEST_ENDED);
            } else {
                userEntityAttempt.testStatus = "PAUSED";
//                userEntityAttempt.timeLeft = userEntityAttempt.timeLeft - (System.currentTimeMillis() - userEntityAttempt.lastUpdated);
            }
            userEntityAttemptRepo.save(userEntityAttempt);
        } else {
            logger.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }
        return null;

    }

    @Override
    public VedantuResponse resetStudentTest(GetEntityResultAnalyticsReq getAnalyticsResultReq) {
        // TODO Auto-generated method stub
        if (getAnalyticsResultReq.entity.type.equals(EntityType.TEST)) {
            if (getAnalyticsResultReq.studentUserId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            } else if (getAnalyticsResultReq.entity.id.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }

            if (getAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity.type, getAnalyticsResultReq.entity.id).processed) {
                // Remove entry from entityUserActionMapping
                removeEntityUserActionMapping(getAnalyticsResultReq.studentUserId, UserActionType.ATTEMPTED, getAnalyticsResultReq.entity);
                // Update test measures for over_all analytics
                updateParticularTestOverallAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity.id);
                // Remove entry from userEntityAttempts
                removeUserEntityAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userEntityAnalytics
                removeUserEntityAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userQuestionAnalytics
                removeUserQuestionAnalytics(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
                // Remove entry from userQuestionAttempts
                removeUserQuestionAttempt(getAnalyticsResultReq.studentUserId, getAnalyticsResultReq.entity);
            } else {
                throw new VedantuException(VedantuErrorCode.ANALYTICS_GENERATION_UNDER_PROCESS);
            }

        } else {
            logger.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }

        return new VedantuResponse(new GetEntityResultAnalyticsRes());
    }

    public EntityUserActionMapping removeEntityUserActionMapping(String userId,
                                                                 UserActionType actionType, SrcEntity target) throws VedantuException {
        logger.debug("UserId is " + userId + " actionType is " + actionType + " target is " + target);
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(actionType);
        criteria.and(ConstantsGlobal.TARGET).is(target);
        query.addCriteria(criteria);
        EntityUserActionMapping EntityUserActionMapping = mongoTemplate.findOne(query, EntityUserActionMapping.class);
        logger.debug("deleted userActionMapping : " + EntityUserActionMapping);
        if (EntityUserActionMapping == null) {
            logger.debug("EntityUserActionMapping not found");
        } else {
            entityUserActionMappingRepo.delete(EntityUserActionMapping);
        }
        return EntityUserActionMapping;
    }

    public void updateParticularTestOverallAnalytics(String userId, String testId) throws VedantuException {

        SrcEntity ent = new SrcEntity();
        ent.type = EntityType.TEST;
        ent.id = testId;
        UserEntityAnalytics presentTestAnalytics = getAnalytics(userId, "", ent, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        ent.id = AcademicDimensionType.OVERALL.name();
        UserEntityAnalytics overAllAnalytics = getAnalytics(userId, "", ent, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        if (presentTestAnalytics == null || overAllAnalytics == null) {
            throw new VedantuException(VedantuErrorCode.ANALYTICS_NOT_FOUND);
        } else {
            Criteria criteria = new Criteria();
            Query query = new Query();
            criteria.and(ConstantsGlobal.USER_ID).is(userId);
            criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(ent.type);
            criteria.and("finished").is(true);
            query.addCriteria(criteria);
            long attemptCount = mongoTemplate.count(query, UserEntityAttempt.class);
            double percentage = Math.round(((overAllAnalytics.percentage * attemptCount) - presentTestAnalytics.percentage) / (attemptCount - 1));
            overAllAnalytics.percentage = percentage;
            overAllAnalytics.measures.attempts -= presentTestAnalytics.measures.attempts;
            overAllAnalytics.measures.correct -= presentTestAnalytics.measures.correct;
            overAllAnalytics.measures.partial -= presentTestAnalytics.measures.partial;
            overAllAnalytics.measures.incorrect -= presentTestAnalytics.measures.incorrect;
            overAllAnalytics.measures.left -= presentTestAnalytics.measures.left;
            overAllAnalytics.measures.score -= presentTestAnalytics.measures.score;
            overAllAnalytics.measures.timeTaken -= presentTestAnalytics.measures.timeTaken;
            userEntityAnalyticsRepo.save(overAllAnalytics);
        }
    }

    public UserEntityAnalytics getAnalytics(String userId, String attemptId, SrcEntity entity,
                                            AcademicDimensionType acadDimType, String acadDimId) {
        logger.debug("getAnalytics userId: " + userId + ", acadDimType: " + acadDimType
                + ", acadDimId: " + acadDimId);
        Criteria criteria = new Criteria();
        Query query = new Query();
        if (!StringUtils.isEmpty(attemptId)) {
            criteria.and(ConstantsGlobal.ATTEMPT_ID).is(attemptId);
        }
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
        criteria.and(ACAD_DIM_TYPE).is(acadDimType);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        query.addCriteria(criteria);
        List<UserEntityAnalytics> userEntityAnalyticsList = mongoTemplate.find(query, UserEntityAnalytics.class);
        UserEntityAnalytics userEntityAnalytics = mongoTemplate.findOne(query, UserEntityAnalytics.class);
        logger.info("getAnalytics entityUserAnalytics: " + userEntityAnalytics);
        return userEntityAnalytics;
    }

    public UserEntityAttempt removeUserEntityAttempt(String userId, SrcEntity target) throws VedantuException {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ENTITY).is(target);
        query.addCriteria(criteria);
        UserEntityAttempt userEntityAttempt = mongoTemplate.findOne(query, UserEntityAttempt.class);
        logger.debug("deleted userEntityAttempt : " + userEntityAttempt);
        if (userEntityAttempt == null) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        } else {
            userEntityAttemptRepo.delete(userEntityAttempt);
        }
        return userEntityAttempt;
    }

    public void removeUserEntityAnalytics(String userId, SrcEntity target) throws VedantuException {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ENTITY).is(target);
        query.addCriteria(criteria);
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(query, UserEntityAnalytics.class);
        logger.debug("deleted userEntityAnalytics ");
        if (userEntityAnalytics.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        } else {
            for (UserEntityAnalytics userEntityAnalytic : userEntityAnalytics) {
                userEntityAnalyticsRepo.delete(userEntityAnalytic);
            }
        }
    }

    public void removeUserQuestionAnalytics(String userId, SrcEntity target) throws VedantuException {

        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and("parentEntity").is(target);
        query.addCriteria(criteria);
        List<UserQuestionAnalytics> userQuestionAnalytics = mongoTemplate.find(query, UserQuestionAnalytics.class);
        logger.debug("deleted UserQuestionAnalytics ");
        if (userQuestionAnalytics.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        } else {
            for (UserQuestionAnalytics userQuestionAnalytic : userQuestionAnalytics) {
                userQuestionAnalyticsRepo.delete(userQuestionAnalytic);
            }
        }
    }

    public void removeUserQuestionAttempt(String userId, SrcEntity target) throws VedantuException {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and("parentEntity").is(target);
        query.addCriteria(criteria);
        List<UserQuestionAttempt> userQuestionAttempt = mongoTemplate.find(query, UserQuestionAttempt.class);
        logger.debug("deleted UserQuestionAttempt ");
        if (userQuestionAttempt.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        } else {
            for (UserQuestionAttempt userQuestionAttempts : userQuestionAttempt) {
                userQuestionAttemptRepo.delete(userQuestionAttempts);
            }
        }
    }

    @Override
    public VedantuResponse regenerateStudentTestAnalytics(GetEntityResultAnalyticsReq getAnalyticsResultReq) {

        ResetQuestionAttemptRes resetQuestionAttemptRes = new ResetQuestionAttemptRes(false, 0);
        if (getAnalyticsResultReq.entity.type.equals(EntityType.TEST)) {
            if (getAnalyticsResultReq.studentUserId.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_USER_ID);
            } else if (getAnalyticsResultReq.entity.id.isEmpty()) {
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
            // Getting the user attempt from UserEntityAttempt
            UserEntityAttempt userAttempt = getAttempt(getAnalyticsResultReq.studentUserId,
                    getAnalyticsResultReq.entity.type,
                    getAnalyticsResultReq.entity.id);
            // If attempt is not found
            if (userAttempt == null) {
                throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
            }

            if (userAttempt.processed) {
                // Update test measures for over_all analytics
                updateParticularTestOverallAnalytics(
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity.id);
                // Remove entry from userEntityAnalytics
                removeUserEntityAnalytics(
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity);
                // Remove entry from userQuestionAnalytics
                removeUserQuestionAnalytics(
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity);

                long endTime = 0;
                // Making ideal condition as of ending the test
                userAttempt.processed = false;
                userAttempt.testStatus = "ONGOING";
                userAttempt.finished = false;
                endTime = userAttempt.endTime;
                userAttempt.endTime = 0;
                userEntityAttemptRepo.save(userAttempt);

                // Getting all the question attempts of particular user for
                // particular test.
                List<UserQuestionAttempt> userQuestionAttempts = getAllAttempts(getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity);
                logger.debug("userQuestionAttempts for regenerateAnalytics"
                        + Arrays.toString(userQuestionAttempts.toArray()));
                removeUserQuestionAttempt(
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity);
                logger.debug("HuserQuestionAttempts for regenerateAnalytics after deleting attempts"
                        + Arrays.toString(userQuestionAttempts.toArray()));
                if (!userQuestionAttempts.isEmpty()) {
                    RecordAttemptReq recordAttemptReq = new RecordAttemptReq();
                    recordAttemptReq.callingUserId = getAnalyticsResultReq.studentUserId;
                    recordAttemptReq.userId = getAnalyticsResultReq.studentUserId;
                    recordAttemptReq.entityId = getAnalyticsResultReq.entity.id;
                    recordAttemptReq.entityType = getAnalyticsResultReq.entity.type;
                    recordAttemptReq.setName = null;
                    recordAttemptReq.attemptId = userAttempt._getStringId();

                    for (UserQuestionAttempt userQuestionAttempt : userQuestionAttempts) {
                        // changing is finalized to true for further operations
                        // userQuestionAttempt.isFinalized = false;
                        // UserQuestionAttemptDAO.INSTANCE
                        // .save(userQuestionAttempt);
                        recordAttemptReq.qId = userQuestionAttempt.qId;
                        recordAttemptReq.answerGiven = userQuestionAttempt.answerGiven;
                        recordAttemptReq.timeTaken = userQuestionAttempt.timeTaken;
                        recordAttemptReq.matrixAnswer = userQuestionAttempt.matrixAnswerGiven;

                        recordAttempts(recordAttemptReq);
                    }
                }

                // Generating new end attempt request
                EndAttemptReq endAttemptReq = new EndAttemptReq(
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.studentUserId,
                        getAnalyticsResultReq.entity.id,
                        getAnalyticsResultReq.entity.type, null,
                        userAttempt._getStringId(), userAttempt.orgId);
                endAttempt(endAttemptReq, endTime);

            } else {
                throw new VedantuException(
                        VedantuErrorCode.ANALYTICS_GENERATION_UNDER_PROCESS);
            }

        } else {
            logger.debug("Invalid Entity type");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }

        return new VedantuResponse(resetQuestionAttemptRes);
    }

    public EndAttemptRes endAttempt(EndAttemptReq endAttemptReq, long endTime)
            throws VedantuException {

        return endAttempt_(endAttemptReq, endTime, false);
    }

    public EndAttemptRes endAttempt_(EndAttemptReq endAttemptReq, long endTime,
                                     boolean ignoreEndTime) throws VedantuException {

        if (!isEntityAttemptAllowed(endAttemptReq)) {
            logger.error("not allowed end attempt of entity: " + endAttemptReq.entityType);
            throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
        }

        String testStatus = entityStatus(endAttemptReq.attemptId);
        if (testStatus.equals("FINISHED")) {
            logger.error("Entity is Finished");
            throw new VedantuException(VedantuErrorCode.TEST_ENDED);
        }

        endAttemptReq.attemptId = (endAttemptReq.attemptId.isEmpty() || endAttemptReq.attemptId == null) ? getAttemptId(
                endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId)
                : endAttemptReq.attemptId;

        // Implement a logic to check whether the given endTime is correct or
        // not and provide with near to correct endTime if it is wrong
        long duration = testRepo.findById(endAttemptReq.entityId).get().duration;
        UserEntityAttempt attempt = userEntityAttemptRepo.findById(endAttemptReq.attemptId).get();
        if (endTime == 0 || endTime < attempt.timeCreated) {
            endTime = System.currentTimeMillis();
        }
        if ((attempt.timeCreated - endTime) > duration) {
            endTime = attempt.timeCreated + duration;
        }
//        endTime  = getTestEndTime(endTime, endAttemptReq.entityId, endAttemptReq.attemptId, endAttemptReq.userId, duration);

        EndAttemptRes endAttemptRes = new EndAttemptRes();
        synchronized ((endAttemptReq.entityType + endAttemptReq.entityId + endAttemptReq.userId)) {

            UserEntityAttempt userEntityAttempt = endAttempt(
                    endAttemptReq.userId, endAttemptReq.entityType, endAttemptReq.entityId,
                    endAttemptReq.attemptId, endTime, true, ignoreEndTime);
            if (userEntityAttempt.parent != null) {
                endAttempt(endAttemptReq.userId,
                        userEntityAttempt.parent.type, userEntityAttempt.parent.id,
                        endAttemptReq.attemptId);
            }
            logger.debug("Test Time ended is: " + userEntityAttempt.endTime);
            logger.debug("Test Time created is: " + userEntityAttempt.timeCreated);

            endAttemptRes.info = userEntityAttempt.toBasicInfo();
            if (endAttemptReq.entityType == EntityType.TEST) {
                // generate an endTest event so that, Server wont get parallel requests to process all end tests at once.
                // With this, we shall generate analytics in first come first serve basis
                EndTestDetails endTestDetails = new EndTestDetails(userEntityAttempt._getStringId(),
                        userEntityAttempt.userId, userEntityAttempt.entity.id,
                        userEntityAttempt.entity.type, endAttemptReq.setName,
                        userEntityAttempt.timeCreated, duration, endAttemptReq.orgId, "USER");
                long processTime = userEntityAttempt.timeCreated;
                questionComponent.generateEventAysc(userEntityAttempt.userId, endTestDetails, EventType.END_TEST,
                        processTime);
            }
        }

        return endAttemptRes;
    }

    public List<UserQuestionAttempt> getAllAttempts(String userId, SrcEntity target) {

        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("userId").is(userId);
        criteria.and("parentEntity.id").is(target.id);
        criteria.and("parentEntity.type").is(target.type);
        List<UserQuestionAttempt> userQuestionAttempts = mongoTemplate.find(query, UserQuestionAttempt.class);
        logger.info("getAttempts userQuestionAttempts.sizes: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
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
                && (!recordAttemptReq.entityId.equalsIgnoreCase(recordAttemptReq.qId))) {
            logger.error("recordAttempt mismatch in id for entityType: "
                    + recordAttemptReq.entityType + ", entityId: " + recordAttemptReq.entityId
                    + ", qId: " + recordAttemptReq.qId);
            throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_ID);
        }

        final SrcEntity parentEntity = new SrcEntity(recordAttemptReq.entityType,
                recordAttemptReq.entityId);

        if (EntityType.QUESTION == recordAttemptReq.entityType) {
            List<UserQuestionAttempt> prevAttempts = getAttempts(
                    recordAttemptReq.userId, parentEntity, recordAttemptReq.qId);
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
                    answer = answerRepo.findById(question.answerId).get();
                    if (answer == null) {
                        logger.error("user[" + recordAttemptReq.userId + "], question ["
                                + recordAttemptReq.qId + "] does not have a verified answer");
                        throw new VedantuException(VedantuErrorCode.UNATTEMPTABLE_ENTITY);
                    }
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
                Test test = testRepo.findById(recordAttemptReq.entityId).get();
                isPartialMarksAllowed = isPartialMarkingEnabled(test, question.type.name());
                isOneOrMoreAllowed = isPartialMarksAllowed || isOneOrMoreAnswersAllowed(test, question.type.name());
            }
            if (CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
                logger.debug("recordAttempt : Getting correct answer");
                isCorrect = question.type.isCorrect(EnumBasket.Judgement.JUDGE,
                        recordAttemptReq.getAnswerGiven(), answer.answer,
                        EnumBasket.Status.COMPLETE, isPartialMarksAllowed, isOneOrMoreAllowed);
                logger.debug("recordAttempt : isCorrect is " + isCorrect.toString());
            }

            if (answer.optionalCorrectAnswers != null && isCorrect == AnswerCorrectness.INCORRECT) {
                logger.debug("recordAttempt : Checking for optional correct answers if answer is incorrect");
                for (Map.Entry<Integer, List<String>> answers : answer.optionalCorrectAnswers
                        .entrySet()) {
                    //CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven()) &&
                    logger.debug("recordAttempt : Found optional correct answers, and iterating inside it");
                    if (CollectionUtils.isNotEmpty(recordAttemptReq.getAnswerGiven())) {
                        isCorrect = question.type.isCorrect(EnumBasket.Judgement.JUDGE,
                                recordAttemptReq.getAnswerGiven(), answers.getValue(),
                                EnumBasket.Status.COMPLETE, isPartialMarksAllowed, isOneOrMoreAllowed);
                    }
                    if (isCorrect == AnswerCorrectness.CORRECT || isCorrect == AnswerCorrectness.PARTIAL) {
                        break;
                    }
                }
            }

//            } else {
//                LOGGER.debug("recordAttempt : Given question is matrix");
//                isCorrect = QuestionType.isEqualMatrix(recordAttemptReq.getMatrixAnswer(),
//                        answer.matrixAnswer);
//            }
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

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, String qId) {

        return getAttempts(userId, parentEntity, qId, null);
    }

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity, String qId,
                                                 Boolean isJudgeable) {

        return getAttempts(userId, parentEntity, Arrays.asList(qId), isJudgeable);
    }

    public List<UserQuestionAttempt> getAttempts(String userId, SrcEntity parentEntity,
                                                 List<String> qIds, Boolean isJudgeable) {

        logger.debug("getAttempts userId: " + userId + ", parentEntity: " + parentEntity
                + ", qIds: " + qIds);
        if (qIds == null) {
            qIds = new ArrayList<String>();
        }
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("userId").is(userId);
        criteria.and("parentEntity.type").is(parentEntity.type);
        criteria.and("parentEntity.id").is(parentEntity.id);
        query.with(Sort.by(Sort.Direction.ASC, ConstantsGlobal.TIME_CREATED));
        if (isJudgeable != null) {
            criteria.and("isJudgeable").is(isJudgeable.booleanValue());
        }
        query.addCriteria(criteria);
        List<UserQuestionAttempt> userQuestionAttempts = mongoTemplate.find(query, UserQuestionAttempt.class);

        logger.info("getAttempts userQuestionAttempts.size: "
                + CollectionUtils.size(userQuestionAttempts));

        return userQuestionAttempts;
    }

    public Question getQuestion(String id) throws VedantuException {

        Question question = questionRepo.findById(id).get();
        if (question == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND,
                    "no question found with id:" + id);
        }
        return question;
    }

    public Answer getQuestionAnswer(String qId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.QID).is(qId);
        query.addCriteria(criteria);
        Answer answer = mongoTemplate.findOne(query, Answer.class);
        return answer;
    }

    public UserQuestionAttempt addAttempt(String userId, String attemptId, SrcEntity parentEntity,
                                          String qId, List<String> answerGiven, Map<String, List<String>> matrixAnswerGiven,
                                          QuestionType type, boolean isJudgeable, AnswerCorrectness isCorrect, int score, long timeTaken) {

        logger.debug("addAttempt userId: " + userId + ", attemptId: " + attemptId
                + ", parentEntity: " + parentEntity + ", qId: " + qId + ", answerGiven: {"
                + (answerGiven + ", ") + "}, isJudgeable:" + isJudgeable
                + ", isCorrect: " + isCorrect + ", score: " + score + ", timeTaken: " + timeTaken);

        UserQuestionAttempt userQuestionAttempt = new UserQuestionAttempt(userId, attemptId,
                parentEntity, qId, answerGiven, isJudgeable, isCorrect, score, timeTaken);
        userQuestionAttempt.matrixAnswerGiven = matrixAnswerGiven;
        userQuestionAttempt.type = type;

        userQuestionAttemptRepo.save(userQuestionAttempt);
        logger.info("addAttempt saved userQuestionAttempt: " + userQuestionAttempt);

        return userQuestionAttempt;
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
                addUserQuestionAnalytics(userQuestionAttempt);
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

    public UserQuestionAnalytics addUserQuestionAnalytics(
            UserQuestionAttempt userQuestionAttempt) {

        if (userQuestionAttempt == null) {
            return null;
        }
        // update UserQuestionAnalytics
        UserQuestionAnalytics userQuestionAnalytics = addAnalytics(userQuestionAttempt.userId, userQuestionAttempt.attemptId,
                userQuestionAttempt.parentEntity, userQuestionAttempt.qId,
                userQuestionAttempt.answerGiven, userQuestionAttempt.matrixAnswerGiven,
                userQuestionAttempt.isJudgeable, userQuestionAttempt.isCorrect,
                userQuestionAttempt.score, userQuestionAttempt.timeTaken);
        return userQuestionAnalytics;
    }

    public UserQuestionAnalytics addAnalytics(String userId, String attemptId,
                                              SrcEntity parentEntity, String qId, List<String> answerGiven,
                                              Map<String, List<String>> matrixAnswerGiven, boolean isJudgeable, AnswerCorrectness isCorrect,
                                              double score, long timeTaken) {

        logger.debug("addAnalytics userId: " + userId + ", attemptId: " + attemptId
                + ", parentEntity: " + parentEntity + ", qId: " + qId + ", answerGiven: {"
                + answerGiven + ", " + "},matrixAnswerGiven: {"
                + matrixAnswerGiven + "}, isJudgeable:" + isJudgeable + ", isCorrect: " + isCorrect
                + ", timeTaken: " + timeTaken);

        UserQuestionAnalytics userQuestionAnalytics = getAnalytics(userId, parentEntity, qId);
        if (userQuestionAnalytics == null) {
            userQuestionAnalytics = new UserQuestionAnalytics(userId, attemptId, parentEntity, qId,
                    answerGiven, isJudgeable, isCorrect, score, timeTaken);
            // userQuestionAnalytics.totalMarks = totalMarks;
            userQuestionAnalytics.matrixAnswerGiven = matrixAnswerGiven;

            userQuestionAnalyticsRepo.save(userQuestionAnalytics);
            logger.info("addAttempt saved userQuestionAnalytics: " + userQuestionAnalytics);
        }
        return userQuestionAnalytics;
    }

    public UserQuestionAnalytics getAnalytics(String userId, SrcEntity parentEntity, String qId) {

        logger.debug("getAnalytics userId: " + userId + ", parentEntity: " + parentEntity
                + ", qId: " + qId);
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("userId").is(userId);
        criteria.and("parentEntity.type").is(parentEntity.type);
        criteria.and("parentEntity.id").is(parentEntity.id);
        criteria.and("qId").is(qId);
        query.addCriteria(criteria);
        UserQuestionAnalytics userQuestionAnalytics = (UserQuestionAnalytics) mongoTemplate.find(query, UserQuestionAnalytics.class);
        logger.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

        return userQuestionAnalytics;
    }

    private boolean finalizeQuestionAttempt(String userId, Question question,
                                            SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
                                            Map<String, List<String>> matrixAnswerGiven, String attemptId) {

        Set<AcademicDimension> acadDims = getAcadDimensions(question.boardIds);
        return finalizeQuestionAttempt(userId, question, parentEntity, measures, answerGiven,
                matrixAnswerGiven, acadDims);
    }

    private boolean finalizeQuestionAttempt(String userId, Question question,
                                            SrcEntity parentEntity, EntityMeasures measures, List<String> answerGiven,
                                            Map<String, List<String>> matrixAnswerGiven, Set<AcademicDimension> acadDims) {

        // NOTE: can be used for LEFT

        // update QuestionAnalytics
        final String answerGivenKey = toAnswerKey(question, answerGiven,
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

    public Set<AcademicDimension> getAcadDimensions(Set<String> brdIds) {

        Set<AcademicDimension> acadDims = new HashSet<AcademicDimension>();
        acadDims.add(new AcademicDimension(AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name()));

        Map<String, BoardBasicInfo> boardBasicInfos = getBasicInfosByIds(brdIds);
        if (MapUtils.isEmpty(boardBasicInfos)) {
            logger.debug("finalizeQuestionAttempt no boards found for brdIds: " + brdIds);
        } else {
            for (Map.Entry<String, BoardBasicInfo> entry : boardBasicInfos.entrySet()) {
                BoardBasicInfo boardBasicInfo = entry.getValue();
                if (null == boardBasicInfo) {
                    continue;
                }
                acadDims.add(new AcademicDimension(AcademicDimensionType
                        .getType(boardBasicInfo.type), boardBasicInfo.id));
            }
        }
        return acadDims;
    }

    public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {

        logger.debug("getBasicInfosByIds ids: {" + ids + ", " + "}");
        List<Board> results = boardRepo.findByIdIn(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids), true));
        Map<String, BoardBasicInfo> basicInfoMap = toBasicInfosMap(results);
        logger.info("getBasicInfosByIds basicInfoMap: {" + basicInfoMap + ", "
                + "}");
        return basicInfoMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMap(Collection<Board> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Board t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (B) t.toBasicInfo());
            }
        }
        return infosMap;
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

        questionAnalytics.getMeasures().setAttempts(measures.getAttempts());
        questionAnalytics.getMeasures().setCorrect(measures.getCorrect());
        questionAnalytics.getMeasures().setPartial(measures.getPartial());
        questionAnalytics.getMeasures().setIncorrect(measures.getIncorrect());
        questionAnalytics.getMeasures().setLeft(measures.getLeft());
        questionAnalytics.getMeasures().setTimeTaken(measures.timeTaken);
        if (!(answerGivenKey).isEmpty()) {
            // . is replace with _ as map key can not contain . in mongo
//            answerGivenKey = answerGivenKey.replace(".", AnswerGivenCount.NUMERIC_DOT_REPLACER);
            questionAnalytics.getAnswerGivenCount().put(answerGivenKey, measures.getAttempts());
//            updateOps.inc("answerGivenCount." + answerGivenKey, measures.attempts);
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
        userAnalytics.getMeasures().setAttempts(measures.attempts);
        userAnalytics.getMeasures().setCorrect(measures.correct);
        userAnalytics.getMeasures().setIncorrect(measures.incorrect);
        userAnalytics.getMeasures().setLeft(measures.left);
        userAnalytics.getMeasures().setScore(measures.score);
        userAnalytics.getMeasures().setTimeTaken(measures.timeTaken);
        userAnalyticsRepo.save(userAnalytics);
        return true;
    }

    private void finalizeAssignmentAttempt(UserQuestionAttempt userQuestionAttempt,
                                           Question question, String orgId) throws VedantuException {

        UserEntityAttempt entityAttempt = getAttempt(
                userQuestionAttempt.userId, userQuestionAttempt.parentEntity.type,
                userQuestionAttempt.parentEntity.id);
        if (entityAttempt == null) {
            Assignment assignment = getAssignment(userQuestionAttempt.parentEntity.id);
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
        UserQuestionAnalytics userQuestionAnalytics = addUserQuestionAnalytics(userQuestionAttempt);
        logger.info("userQuestionAnalytics: " + userQuestionAnalytics);
        Set<AcademicDimension> acadDims = getAcadDimensions(question.boardIds);
        if (entityAttempt != null) {
            entityAttempt.endTime = System.currentTimeMillis();
            userEntityAttemptRepo.save(entityAttempt);
        }
        finalizeQuestionAttempt(userQuestionAttempt.userId, question,
                userQuestionAttempt.parentEntity, measures, userQuestionAttempt.answerGiven,
                userQuestionAttempt.matrixAnswerGiven, acadDims);
        for (AcademicDimension acadDim : acadDims) {
            updateUserEntityAnalytics(userQuestionAttempt.userId,
                    userQuestionAttempt.parentEntity, acadDim.type, acadDim.id, measures, 0, orgId);
        }
    }

    public Assignment getAssignment(String id) throws VedantuException {

        Assignment assignment = assignmentRepo.findById(id).get();
        if (assignment == null) {
            logger.error("no assignment found with id: " + id);
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND,
                    "no assignment found with id: " + id);
        }
        return assignment;
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

    public boolean updateUserEntityAnalytics(String userId, SrcEntity entity,
                                             AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures,
                                             double percentageScore, String orgId) {

        UserEntityAnalytics userEntityAnalytics = addAnalytics(
                userId, entity, acadDimType, acadDimId, measures, percentageScore, orgId);
        logger.debug("finalize UserEntityAnalytics acadDim:{type: " + acadDimType + ", id:"
                + acadDimId + "}" + ", userEntityAnalytics: " + userEntityAnalytics);

        boolean addedEntityAnalytics = addAnalytics(entity,
                acadDimType, acadDimId, measures);
        logger.debug("finalize EntityAnalytics acadDim:{type: " + acadDimType + ", id:" + acadDimId
                + "}" + ", added: " + addedEntityAnalytics);

        return addedEntityAnalytics && userEntityAnalytics != null;
    }

    public UserEntityAnalytics addAnalytics(String userId, SrcEntity entity,
                                            AcademicDimensionType acadDimType, String acadDimId, EntityMeasures measures,
                                            double percentageScore, String orgId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
        criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(entity.type);
        criteria.and(ACAD_DIM_TYPE).is(acadDimType);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        query.addCriteria(criteria);
        UserEntityAnalytics userEntityAnalytics = mongoTemplate.findOne(query, UserEntityAnalytics.class);
        if (userEntityAnalytics == null) {
            userEntityAnalytics = new UserEntityAnalytics(userId, new AcademicDimension(
                    acadDimType, acadDimId), measures, null, entity, percentageScore, orgId);
        } else {
            userEntityAnalytics.measures.attempts += measures.attempts;
            userEntityAnalytics.measures.correct += measures.correct;
            userEntityAnalytics.measures.partial += measures.partial;
            userEntityAnalytics.measures.incorrect += measures.incorrect;
            userEntityAnalytics.measures.left += measures.left;
            userEntityAnalytics.measures.score += measures.score;
            userEntityAnalytics.measures.timeTaken += measures.timeTaken;
            // if ((userEntityAnalytics.percentage > 0 && percentageScore < 0)
            // || (userEntityAnalytics.percentage < 0 && percentageScore > 0)) {
            // // this is just to reset the percentage score in case of duplicate score
            // userEntityAnalytics.percentage = Math.abs(percentageScore);
            // } else

            // get total test attempted by the user
            Criteria criteria1 = new Criteria();
            Query query1 = new Query();
            criteria1.and(ConstantsGlobal.USER_ID).is(userId);
            criteria1.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
            criteria1.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(entity.type);
            criteria1.and("finished").is(true);
            query1.addCriteria(criteria1);

            long attemptCount = mongoTemplate.count(query1, UserEntityAttempt.class);
            if (attemptCount == 0) {
                attemptCount = 1;// 1st attempt
            }
            userEntityAnalytics.percentage = ((userEntityAnalytics.percentage * (attemptCount - 1)) + percentageScore)
                    / attemptCount;
            userEntityAnalytics.orgId = orgId;
        }
        userEntityAnalytics.lastUpdated = System.currentTimeMillis();
        userEntityAnalyticsRepo.save(userEntityAnalytics);
        // update analytics to corresponding overall data
        // entity{TYPE:TEST,id:OVERALL}
        if (entity.id != AcademicDimensionType.OVERALL.name()) {
            addAnalytics(userId, new SrcEntity(entity.type, AcademicDimensionType.OVERALL.name()),
                    acadDimType, acadDimId, measures, percentageScore, orgId);
        }

        return userEntityAnalytics;
    }

    public boolean addAnalytics(SrcEntity entity, AcademicDimensionType acadDimType,
                                String acadDimId, EntityMeasures measures) {

        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("entity").is(entity);
        criteria.and(ACAD_DIM_TYPE).is(acadDimType);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        query.addCriteria(criteria);
        EntityAnalytics entityAnalytics = mongoTemplate.findOne(query, EntityAnalytics.class);
        if (entityAnalytics == null) {
            return false;
        }
        entityAnalytics.getMeasures().setAttempts(measures.getAttempts());
        entityAnalytics.getMeasures().setScore(measures.getScore());
        entityAnalytics.getMeasures().setCorrect(measures.getCorrect());
        entityAnalytics.getMeasures().setIncorrect(measures.getIncorrect());
        entityAnalytics.getMeasures().setLeft(measures.getLeft());
        entityAnalytics.getMeasures().setTimeTaken(measures.timeTaken);
        final boolean createIfNotPresent = true;
        entityAnalyticsRepo.save(entityAnalytics);
        if (measures.score > entityAnalytics.measures.maxScore) {
            entityAnalytics.getMeasures().setMaxScore(measures.score);
            logger.debug("updating maxScore : " + measures.score);
            entityAnalyticsRepo.save(entityAnalytics);
        }

        logger.debug("update enalytics result");

        return true;
    }

    @Override
    public VedantuResponse recomputeEntitynalytics(StartAttemptReq recomputeAnalyticsReq) {

        UserEntityAttempt userEntityAttempt = getAttempt(
                recomputeAnalyticsReq.userId, recomputeAnalyticsReq.entityType,
                recomputeAnalyticsReq.entityId);
        if (userEntityAttempt == null) {
            logger.error("no attempts found for userId : " + recomputeAnalyticsReq.userId
                    + " entityType: " + recomputeAnalyticsReq.entityType + ", entityId: "
                    + recomputeAnalyticsReq.entityId);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        logger.info("recomputing entity analytics for userId : " + recomputeAnalyticsReq.userId
                + " entityType: " + recomputeAnalyticsReq.entityType + ", entityId: "
                + recomputeAnalyticsReq.entityId);
        Map<String, Question> qIdToQuestionMap = toInfosMap(questionRepo.findAllById(ObjectIdUtils
                .toObjectIds(userEntityAttempt.qIds)));

        List<UserQuestionAttempt> attempts = getAttempts(
                userEntityAttempt.userId, userEntityAttempt.entity, userEntityAttempt.qIds, true);

        // also has OVERALL
        Map<String, Integer> acadDimToTotalQusCountMap = new HashMap<String, Integer>();

        // acadDimentionWise totalMarks map
        Map<String, Integer> acadDimToTotalMarksMap = new HashMap<String, Integer>();
        Map<String, Marks> qIdsToMarksMap = getEntityQuestionMarksDistribution(
                userEntityAttempt.entity, acadDimToTotalQusCountMap, acadDimToTotalMarksMap);

        // this will also compute the score corresponding to an entity for every
        // question
        Map<String, UserQuestionAttempt> qIdToFinalAttempts = toFinalAttempts(
                qIdsToMarksMap, attempts);

        // also has OVERALL
        Map<String, AcademicDimension> boardwiseAcadDimMap = getBoardswiseAcademicDimensions(qIdToQuestionMap);

        // also has OVERALL
        Map<String, EntityMeasures> boardwiseEntityMeasuresMap = new HashMap<String, EntityMeasures>();
        Map<String, EntityMeasures> questionWiseMeasuresMap = new HashMap<String, EntityMeasures>();
        for (Map.Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {

            Question question = entry.getValue();

            UserQuestionAttempt userQuestionAttempt = qIdToFinalAttempts.get(entry.getKey());
            Criteria criteria = new Criteria();
            Query query = new Query();
            // update UserQuestionAnalytics
            UserQuestionAnalytics userQuestionAnalytics = null;
            if (userQuestionAttempt != null) {
                criteria.and("userId").is(userQuestionAttempt.userId);
                criteria.and("parentEntity.type").is(userQuestionAttempt.parentEntity.type);
                criteria.and("parentEntity.id").is(userQuestionAttempt.parentEntity.id);
                criteria.and("qId").is(userQuestionAttempt.qId);
                query.addCriteria(criteria);
                List<UserQuestionAnalytics> userQuestionAnalyticsList = mongoTemplate.find(query, UserQuestionAnalytics.class);
                if (userQuestionAnalyticsList.size() > 0) {
                    userQuestionAnalytics = userQuestionAnalyticsList.get(0);
                    for (int i = 1; i < userQuestionAnalyticsList.size(); i++) {
                        logger.info("markDeleted userQuestionAnalytics: "
                                + userQuestionAnalyticsList.get(i));
                        userQuestionAnalyticsList.get(i).setRecordState(VedantuRecordState.DELETED);
                        userQuestionAnalyticsRepo.save(userQuestionAnalyticsList.get(i));

                    }
                }
            }
            logger.info("processEndAttempt userQuestionAnalytics: " + userQuestionAnalytics);

            int attemptCount = 0;
            int correct = 0;
            int incorrect = 0;
            int partial = 0;
            int left = 1;
            double score = 0;
            long timeTaken = 0;
            if (userQuestionAttempt != null) {
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
                attemptCount = 1;
                left = 0;
                timeTaken = userQuestionAttempt.timeTaken;
                score = userQuestionAttempt.score;
            } else {
                // if the user has not attempted the question and the question
                // was declared as
                // bonus question then allow him the marks of the question
                Marks mark = qIdsToMarksMap.get(question._getStringId());
                if (mark != null && mark.status == QuestionResultStatus.BONUS) {
                    score = mark.positive;
                }
            }
            final EntityMeasures qusMeasures = new EntityMeasures(attemptCount, correct, partial, incorrect,
                    left, timeTaken, score);
            questionWiseMeasuresMap.put(question._getStringId(), qusMeasures);
            updateEntityMeasuresMap(boardwiseEntityMeasuresMap, question.boardIds, qusMeasures);
        }
        if (boardwiseEntityMeasuresMap.isEmpty()) {
            return new VedantuResponse(boardwiseEntityMeasuresMap);
        }
        long duplicateCount = 0;
        for (Map.Entry<String, EntityMeasures> entry : boardwiseEntityMeasuresMap.entrySet()) {

            AcademicDimension acadDim = boardwiseAcadDimMap.get(entry.getKey());
            if (acadDim == null) {
                continue;
            }
            EntityMeasures measures = entry.getValue();
            measures.left = (acadDimToTotalQusCountMap.get(entry.getKey()) == null ? 0
                    : acadDimToTotalQusCountMap.get(entry.getKey()) - measures.attempts);
            double percentage = acadDimToTotalMarksMap.get(acadDim.id) == null ? 0
                    : (measures.score * 100)
                    / (acadDimToTotalMarksMap.get(acadDim.id) != null
                    && acadDimToTotalMarksMap.get(acadDim.id) != 0 ? acadDimToTotalMarksMap
                    .get(acadDim.id) : 1);

            // this will calculate the deviation of analytics{attempts, left,
            // correct etc}
            UserEntityAnalytics userEntityBoardAnalyttics = getAnalytics(userEntityAttempt.userId, null, userEntityAttempt.entity,
                    acadDim.type, acadDim.id);

            if (acadDim.id.equals(AcademicDimensionType.OVERALL.name())) {
                logger.debug("userEntityBoardAnalyttics: " + userEntityBoardAnalyttics
                        + ", current measures:" + measures);
                duplicateCount = (userEntityBoardAnalyttics.measures.attempts + userEntityBoardAnalyttics.measures.left)
                        / (measures.attempts + measures.left);
                logger.info("duplicateCount for user[" + userEntityAttempt.userId + "]: "
                        + duplicateCount);
            }
            measures.attempts -= userEntityBoardAnalyttics.measures.attempts;
            measures.correct -= userEntityBoardAnalyttics.measures.correct;
            measures.partial -= userEntityBoardAnalyttics.measures.partial;
            measures.incorrect -= userEntityBoardAnalyttics.measures.incorrect;
            measures.left -= userEntityBoardAnalyttics.measures.left;
            measures.score -= userEntityBoardAnalyttics.measures.score;
            measures.timeTaken -= userEntityBoardAnalyttics.measures.timeTaken;

            logger.info("correction measures : " + measures + ", scorePercentage: " + percentage);
            updateUserEntityAnalytics(userEntityAttempt.userId,
                    userEntityAttempt.entity, acadDim.type, acadDim.id, measures, percentage,
                    recomputeAnalyticsReq.orgId);
            if (userEntityAttempt.parent != null
                    && !(userEntityAttempt.parent.id).isEmpty() || (userEntityAttempt.parent.id) != null) {
                // if test has parent then, update analytics of parent test
                updateUserEntityAnalytics(userEntityAttempt.userId,
                        userEntityAttempt.parent, acadDim.type, acadDim.id, measures, percentage,
                        recomputeAnalyticsReq.orgId);
            }
        }
        // now fix the over all question analytics and user over all analytics

        duplicateCount--;
        logger.info("duplicateCount for user[" + userEntityAttempt.userId + "]: " + duplicateCount);
        for (Map.Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {
            UserQuestionAttempt userQuestionAttempt = qIdToFinalAttempts.get(entry.getKey());
            Set<AcademicDimension> acadDims = getAcadDimensionsSubset(
                    qIdToQuestionMap.get(entry.getKey()).boardIds, boardwiseAcadDimMap);
            EntityMeasures qusMeasures = questionWiseMeasuresMap.get(entry.getValue()
                    ._getStringId());
            // update UserAnalytics & QuestionAnalytics
            if (userQuestionAttempt != null && qusMeasures != null) {
                // if the question is attempted multiple time than this method
                // is need to be called
                // multiple times
                final EntityMeasures qusMeasuresCorrection = new EntityMeasures(
                        -(int) (qusMeasures.attempts * duplicateCount),
                        -(int) (qusMeasures.correct * duplicateCount),
                        -(int) (qusMeasures.partial * duplicateCount),
                        -(int) (qusMeasures.incorrect * duplicateCount),
                        -(int) (qusMeasures.left * duplicateCount),
                        -(int) (qusMeasures.timeTaken * duplicateCount),
                        -(int) (qusMeasures.score * duplicateCount));
                finalizeQuestionAttempt(userQuestionAttempt.userId,
                        qIdToQuestionMap.get(entry.getKey()), userQuestionAttempt.parentEntity,
                        qusMeasuresCorrection, userQuestionAttempt.answerGiven,
                        userQuestionAttempt.matrixAnswerGiven, acadDims);
            }
        }
        return new VedantuResponse(boardwiseEntityMeasuresMap);
    }

    public final Map<String, Question> toInfosMap(Collection<Question> results) {

        Map<String, Question> infosMap = new LinkedHashMap<String, Question>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Question t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), t);
            }
        }
        return infosMap;
    }

    private Map<String, Marks> getEntityQuestionMarksDistribution(SrcEntity entity,
                                                                  Map<String, Integer> acadDimnToTotalQusCountMap,
                                                                  Map<String, Integer> acadDimToTotalMarksMap) {

        Map<String, Marks> markDistribution = new HashMap<String, Marks>();

        if (entity.type != EntityType.TEST) {
            return markDistribution;
        }
        Test test = testRepo.findById(entity.getId()).get();//entity.id
        if (test == null) {
            return markDistribution;
        }
        acadDimnToTotalQusCountMap.put(AcademicDimensionType.OVERALL.name(), test.qusCount);
        acadDimToTotalMarksMap.put(AcademicDimensionType.OVERALL.name(), test.totalMarks);
        if (test.metadata != null) {
            for (TestMetadata mdata : test.metadata) {
                acadDimnToTotalQusCountMap.put(mdata.id, mdata.qusCount);
                acadDimToTotalMarksMap.put(mdata.id, mdata.totalMarks);
                if (mdata.children != null) {
                    for (BoardQus child : mdata.children) {
                        acadDimnToTotalQusCountMap.put(child.id, child.qusCount);
                        acadDimToTotalMarksMap.put(child.id, child.totalMarks);
                    }
                }
                if (mdata.marks != null) {
                    markDistribution.putAll(mdata.marks);
                }
            }
        }
        return markDistribution;
    }

    public Map<String, UserQuestionAttempt> toFinalAttempts(
            Map<String, Marks> qIdsToMarksMap, List<UserQuestionAttempt> attempts) {

        Map<String, UserQuestionAttempt> qIdToFinalAttempts = new HashMap<String, UserQuestionAttempt>();
        for (UserQuestionAttempt attempt : attempts) {
            UserQuestionAttempt finalAttempt = qIdToFinalAttempts.get(attempt.qId);
            if (finalAttempt == null || finalAttempt.timeCreated < attempt.timeCreated) {
                qIdToFinalAttempts.put(attempt.qId, attempt);
            }
        }

        for (Map.Entry<String, UserQuestionAttempt> entry : qIdToFinalAttempts.entrySet()) {
            UserQuestionAttempt userQuestionAttempt = entry.getValue();
            Marks marks = qIdsToMarksMap.get(entry.getKey());
            if (marks == null) {
                continue;
            }
            double score;
            logger.info("Answer correct? " + userQuestionAttempt.isCorrect.name());
            switch (userQuestionAttempt.isCorrect) {
                case CORRECT:
                    score = marks.positive;
                    break;
                case INCORRECT:
                    score = -marks.negative;
                    break;
                case PARTIAL:
                    Question ques = questionRepo.findById(userQuestionAttempt.qId).get();
                    Answer answer = getQuestionAnswer(userQuestionAttempt.qId);
                    if (answer == null) {
                        logger.error("Answer is null");
                        score = 0;
                        break;
                    }
                    Set<String> correctAnswers = new HashSet<String>(answer.answer);
                    double partial = (double) (marks.positive) / ques.options.size();
                    score = calculatePartialScore(userQuestionAttempt.answerGiven, correctAnswers, userQuestionAttempt.qId, partial);
                    break;
                default:
                    score = 0;
                    break;
            }

            if (marks.status == QuestionResultStatus.BONUS) {
                score = marks.positive;
            } else if (marks.status == QuestionResultStatus.CANCELLED) {
                score = 0;
            }
            userQuestionAttempt.score = score;
            userQuestionAttempt.isFinalized = true;
            // save this UserQuestionAttempt
            logger.debug("HEMAN score is " + score);
            logger.info("saving user final attempts: " + userQuestionAttempt);
            userQuestionAttemptRepo.save(userQuestionAttempt);
        }
        return qIdToFinalAttempts;
    }

    public Map<String, AcademicDimension> getBoardswiseAcademicDimensions(
            Map<String, Question> qIdToQuestionMap) {

        Set<String> brdIds = new HashSet<String>();
        for (Map.Entry<String, Question> entry : qIdToQuestionMap.entrySet()) {
            if (entry.getValue().boardIds != null) {
                brdIds.addAll(entry.getValue().boardIds);
            }
        }
        Map<String, AcademicDimension> acadDimnMap = new HashMap<String, AcademicDimension>();
        Set<AcademicDimension> acadDims = getAcadDimensions(brdIds);
        for (AcademicDimension acadDim : acadDims) {
            acadDimnMap.put(acadDim.id, acadDim);
        }
        return acadDimnMap;
    }

    private void updateEntityMeasuresMap(
            Map<String, EntityMeasures> boardwiseEntityMeasuresMap, Collection<String> brdIds,
            EntityMeasures questionMeasures) {

        updateEntityMeasuresMap(boardwiseEntityMeasuresMap, AcademicDimensionType.OVERALL.name(),
                questionMeasures);
        for (String brdId : brdIds) {
            updateEntityMeasuresMap(boardwiseEntityMeasuresMap, brdId, questionMeasures);
        }
    }

    @Override
    public VedantuResponse getUserEntityAnalyticsBySubject(GetUserEntityAnalyticsBySubjectReq request) {
        GetUserEntityAnalyticsBySubjectRes response = new GetUserEntityAnalyticsBySubjectRes();
        List<StudentSubjectWiseResult> results = new ArrayList<StudentSubjectWiseResult>();
        Test test = testRepo.findById(request.test.id).get();
        response.totalMarks = test.totalMarks;
        response.testName = test.name;
        Organization org = organizationRepo.findById(request.orgId).get();
        response.orgName = org.fullName;
        for (TestMetadata subjectData : test.metadata) {
            response.subjectMaxMarksMap.put(subjectData.name, subjectData.totalMarks);
            List<UserEntityAnalytics> userEntityAnalytics = getAllAnalytics(request.test.id, AcademicDimensionType.COURSE, subjectData.id,
                    request.orgId);
            for (UserEntityAnalytics userEntityAnalytic : userEntityAnalytics) {
                OrgMember orgMember = orgMemberRepo.findByUserId(userEntityAnalytic.userId);
                logger.debug("userEntityAnalytic Id is " + userEntityAnalytic.userId);
                if (orgMember == null) {
                    continue;
                }
                StudentSubjectWiseResult results1 = new StudentSubjectWiseResult(
                        orgMember.getFullName(), orgMember.memberId);
                UserAnalyticsResult marks = new UserAnalyticsResult(subjectData.name,
                        userEntityAnalytic.measures.score);
                if (results.contains(results1)) {
                    int index = results.indexOf(results1);
                    results.get(index).results.add(marks);
                } else {
                    results1.results.add(marks);
                    UserEntityAttempt attempt = getAttempt(userEntityAnalytic.userId, EntityType.TEST, request.test.id);
                    results1.phoneNumber = orgMemberRepo.findByUserId(userEntityAnalytic.userId).contactNumber;
                    results1.endTime = attempt.endTime;
                    results1.startTime = attempt.timeCreated;
                    results.add(results1);
                }
            }
        }
        long marksSumOfAllStudents = 0;
        for (StudentSubjectWiseResult studentSubject : results) {
            UserAnalyticsResult overallResult = new UserAnalyticsResult("OVERALL", 0);
            int marks = 0;
            for (UserAnalyticsResult singleSubject : studentSubject.results) {
                marks += singleSubject.subjectMarks;
            }
            overallResult.subjectMarks = marks;
            marksSumOfAllStudents += marks;
            studentSubject.results.add(overallResult);
        }
        int length = test.metadata.size();
        for (int i = 0; i <= length; i++) {
            try {
                Collections.sort(results, new StudentSubjectWiseResult.SubjectComparator(i));
            } catch (IndexOutOfBoundsException e) {
                logger.debug("Exception came " + e);
            }
            int index = 1, rank = 1;
            double currMarks = Integer.MIN_VALUE;
            for (StudentSubjectWiseResult studentSubject : results) {
                logger.debug("Student name " + studentSubject.userName);
                try {
                    double marks = studentSubject.results.get(i).subjectMarks;
                    if (currMarks != marks) {
                        currMarks = marks;
                        rank = index;
                    }
                    studentSubject.results.get(i).rankOfStudent = rank;
                    if (index == results.size() - 1) {
                        studentSubject.results.get(i).rankOfStudent = index;
                    }
                    index++;
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("Internal Exception came " + e);
                }
            }
        }
        StudentSubjectWiseResult topper = results.get(0);
        response.highestMarks = topper.results.get(length).subjectMarks;
        Double averageMarks = Double.valueOf(new DecimalFormat("#.##")
                .format((double) marksSumOfAllStudents / (double) results.size()));
        response.averageMarks = averageMarks;
        response.results = results;
        return new VedantuResponse(response);
    }

    public List<UserEntityAnalytics> getAllAnalytics(String entityId,
                                                     AcademicDimensionType acadDimType, String acadDimId, String orgId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entityId);
        criteria.and("orgId").is(orgId);
        criteria.and(ACAD_DIM_TYPE).is(acadDimType);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        query.addCriteria(criteria);
        List<UserEntityAnalytics> entityUserAnalyticsList = mongoTemplate.find(query, UserEntityAnalytics.class);
        return entityUserAnalyticsList;
    }

    @Override
    public VedantuResponse getAttemptedEntities(GetAttemptedEntitiesReq req) {
        GetAttemptedEntitiesRes getAttemptedEntitiesRes = new GetAttemptedEntitiesRes();
        String resultForUserId = req._getResultForUserId();
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(resultForUserId);
        if (req.type != null) {
            criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(req.type.name());
        }

        if (CollectionUtils.isNotEmpty(req.ids)) {
            criteria.and(ConstantsGlobal.ENTITY_DOT_ID).in(req.ids.toArray());
        }
        criteria.and("finished").is(true);
        criteria.and(ConstantsGlobal.END_TIME).is(req.attemptedAfter);
        query.with(Sort.by(Sort.Direction.ASC, ConstantsGlobal.END_TIME));
        query.addCriteria(criteria);
        List<UserEntityAttempt> entityAttempts = mongoTemplate.find(query, UserEntityAttempt.class);
        getAttemptedEntitiesRes.totalHits = entityAttempts.size();
        for (UserEntityAttempt entityAttempt : entityAttempts) {
            GetAttemptedEntityRes attemptedEntity = new GetAttemptedEntityRes(
                    entityAttempt.entity.type, entityAttempt.entity.id, entityAttempt.endTime);
            getAttemptedEntitiesRes.list.add(attemptedEntity);
        }
        return new VedantuResponse(getAttemptedEntitiesRes);
    }

    @Override
    public VedantuResponse syncTabletAnalytics(SyncTabletAnalyticsReq req) {
        StartAttemptRes startAttemptRes = startAttempt(req, true, req.startTime, req.endTime);
        logger.debug("startAttemptRes :  " + startAttemptRes);

        if (startAttemptRes.isReattempt) {
            String msg = "attempt already is in progress, hence " + req.callingApp
                    + " is not allowed to sync analytics for now, try after some time";
            logger.error(msg);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_IN_PROGRESS, msg);
        }

        req.prepareRecordQuestionAttemptReq(startAttemptRes.info.id);
        logger.debug("submitting question attempt info");

        boolean recordedQuestionAttempt = false;

        SyncTabletAnalyticsRes res = new SyncTabletAnalyticsRes();

        synchronized ((req.entityId + req.entityType + req.userId).intern()) {

            if (req.qusAttemptReqs != null) {
                for (RecordAttemptReq qusAttemptReq : req.qusAttemptReqs) {
                    try {
                        if (CollectionUtils.isNotEmpty(qusAttemptReq.getAnswerGiven())
                                || MapUtils.isNotEmpty(qusAttemptReq.getMatrixAnswer())) {
                            recordAttempt(qusAttemptReq);
                            recordedQuestionAttempt = true;
                        }
                    } catch (VedantuException e) {
                        recordedQuestionAttempt = false;
                        logger.error(e.getMessage(), e);
                    }
                }
            } else {
                // if no question was attempted on tablet
                recordedQuestionAttempt = true;
            }

            if (recordedQuestionAttempt) {
                EndAttemptReq endAttemptReq = new EndAttemptReq(req.callingUserId, req.userId,
                        req.entityId, req.entityType, req.setName, startAttemptRes.info.id,
                        req.orgId);
                endAttemptReq.callingApp = req.callingApp;
                endAttemptReq.callingAppId = req.callingAppId;
                EndAttemptRes endAttemptRes = endAttempt(endAttemptReq, req.endTime, true);
                logger.info("endAttempt res : " + endAttemptRes);
                res.info = endAttemptRes.info;
            }
        }
        res.processed = true;
        return new VedantuResponse(res);
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
                    LibraryContentLink cLink = getLibraryContentLink(new SrcEntity(EntityType.TEST,
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
        AbstractContentStatsModel entityModel = analyticsComponent.getAttemptedEntity(new SrcEntity(
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
        Criteria criteria = new Criteria();
        Query query = new Query();
        logger.debug("Querying for " + LibraryContentLink.class);
        if (targetEntity != null) {
            if (targetEntity.type != null) {
                criteria.and("target.type").is(targetEntity.getType());
                if (targetEntity.getId() != null) {
                    criteria.and("target.id").is(targetEntity.getId());
                }
            }

        }
        if (content != null) {
            if (content.type != null) {
                criteria.and("source.type").is(content.getType());
                if (content.getId() != null) {
                    criteria.and("source.id").is(content.getId());

                }
            }

        }

        if (linkType != null) {
            criteria.and("linkType").is(linkType);

        }

        if (recordState != null) {
            criteria.and("recordState").is(recordState);
        }
        query.skip(start);
        query.limit(size);
        logger.debug("Query: " + query.toString());
        query.addCriteria(criteria);
        List<LibraryContentLink> libraryLinks = mongoTemplate.find(query, LibraryContentLink.class);
        if (totalHits != null) {
            long count = libraryLinks.size();
            totalHits.set(count);
            logger.debug("Total matched results " + totalHits);
        }

        return libraryLinks;
    }

    public ModuleSchedules getGlobalSchedule(SrcEntity target, SrcEntity globalSource, SrcEntity globalEntity) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and("target.type").is(target.type);
        criteria.and("target.id").is(target.id);
        criteria.and("globalSource.type").is(globalSource.type);
        criteria.and("globalSource.id").is(globalSource.id);
        criteria.and("globalEntity.type").is(globalEntity.type);
        criteria.and("globalEntity.id").is(globalEntity.id);
        criteria.and("recordState").is(VedantuRecordState.ACTIVE);
        query.addCriteria(criteria);
        ModuleSchedules moduleSchedules = (ModuleSchedules) mongoTemplate.find(query, ModuleSchedules.class);
        return moduleSchedules;
    }

    @Override
    public VedantuResponse getUserEntityRank(GetUserEntityRankReq req) {
        GetUserEntityRankRes res = new GetUserEntityRankRes();
        UserEntityAnalytics userEntityAnalytics = getAnalytics(
                req.targetUserId, null, req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        if (userEntityAnalytics == null) {
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED, "user " + req.targetUserId
                    + " has not attempted entity: " + req.entity);
        }
        res.user = getUserInfo(req.orgId, req.targetUserId);
        Test test = testRepo.findById(req.entity.id).get();
        if (test.showAIR) {
            res.showAIR = true;
            res.AIR = getRank(req.entity.id,
                    userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                    AcademicDimensionType.OVERALL.name());
        }
        res.rank = getRank(req.orgId, req.entity.id,
                userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                AcademicDimensionType.OVERALL.name());
        return new VedantuResponse(res);
    }

    public int getRank(String entityId, double score, long timeTaken, String acadDimId) {

        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entityId);
        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(acadDimId);
        criteria.and("measures.score").gte(score);
        String groupQueryString = "{\"$group\" : {_id: {score: \"$measures.score\", timeTaken : \"$measures.timeTaken\"}}}";
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC.name(), "_id.score"));
        query.with(Sort.by(Sort.Direction.ASC.name(), "_id.timeTaken"));
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(query, UserEntityAnalytics.class);
        int rank = 1;
        for (UserEntityAnalytics result : userEntityAnalytics) {
            UserEntityAnalytics _id = result;
            if (_id.getMeasures().getScore() == score && _id.getMeasures().getTimeTaken() >= timeTaken) {
                break;
            }
            rank++;
        }
        return rank;
    }

    public int getRank(String orgId, String entityId, double score, long timeTaken, String acadDimId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entityId);
        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(acadDimId);
        criteria.and("measures.score").gte(score);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC.name(), "_id.score"));
        query.with(Sort.by(Sort.Direction.ASC.name(), "_id.timeTaken"));
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(query, UserEntityAnalytics.class);
        logger.debug("leader board results : " + userEntityAnalytics);
        int rank = 1;
        final double TOLERANCE = 0.0000001d;
        for (UserEntityAnalytics result : userEntityAnalytics) {
            UserEntityAnalytics _id = result;
            if (Math.abs(score - Double.parseDouble(String.valueOf(_id.getMeasures().getScore()))) <= TOLERANCE /*&& (Long) _id.get("timeTaken") >= timeTaken*/) {
                break;
            }
            rank++;
        }
        return rank;
    }

    public UserInfo getUserInfo(String orgId, String userId) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId));

        return (UserInfo) userInfos.get(userId);
    }

    @Override
    public VedantuResponse getUserEntityMeasures(GetUserEntityMeasuresReq req) {
        AbstractTestCommonModel test = (AbstractTestCommonModel) analyticsComponent.getAttemptedEntity(req.entity);

        UserEntityAnalytics userEntityAnalytics = getAnalytics(
                req.targetUserId, null, req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        GetUserEntityMeasuresRes res = populateUserEntityMeasures(test.qusCount,
                userEntityAnalytics);
        res.user = getUserInfo(req.orgId, req.targetUserId);

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getEntityMeasures(GetEntityMeasuresReq req) {


        AbstractTestCommonModel test = (AbstractTestCommonModel) analyticsComponent.getAttemptedEntity(req.entity);
        Criteria criteria = new Criteria();
        Query query = new Query();
        query = getQuestionSetOrderQuery(req.getOrderBy(), req.getSortOrder());
//        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(req.entity.id);
        criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(req.entity.type.name());
//        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(AcademicDimensionType.OVERALL.name());
        GetEntityMeasuresRes res = new GetEntityMeasuresRes();
        query.skip(req.size);
        query.limit(req.start);
        query.addCriteria(criteria);
        List<UserEntityAnalytics> analytics = mongoTemplate.find(query, UserEntityAnalytics.class);
        res.totalHits = analytics.size();
        Set<String> userIds = new HashSet<String>();

        for (UserEntityAnalytics userEntityAnalytics : analytics) {
            userIds.add(userEntityAnalytics.userId);
        }

        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(req.orgId, userIds);

        for (UserEntityAnalytics userEntityAnalytics : analytics) {
            GetUserEntityMeasuresRes userMeasuresRes = populateUserEntityMeasures(test.qusCount,
                    userEntityAnalytics);
            userMeasuresRes.user = (UserInfo) userInfoMap.get(userEntityAnalytics.userId);
            res.list.add(userMeasuresRes);
        }
        return new VedantuResponse(res);
    }

    private Query getQuestionSetOrderQuery(String orderBy, String sortOrder) {
        if (StringUtils.isEmpty(orderBy)) {
            return null;
        }
        Query query = new Query();
        Sort.Direction sOrder = Sort.Direction.valueOf(sortOrder);

        DBObject sortQuery = new BasicDBObject();

        if ((orderBy.equalsIgnoreCase("attempts"))) {
            query.with(Sort.by(sortOrder, "measures.attempts"));
            query.with(Sort.by(sortOrder, "measures.correct"));
        } else if (orderBy.equalsIgnoreCase("correct")) {
            query.with(Sort.by(sortOrder, "measures.correct"));

            query.with(Sort.by(sortOrder, "measures.attempts"));
        }

        return query;
    }


    @Override
    public VedantuResponse getUserEntityQuestionsAttemptStatInfo(GetEntityQuestionsAttemptStatReq req) {
        logger.debug("getUserEntityQuestionsAttemptInfoStat : Request Came ");
        String forUserId = req._getResultForUserId();
        logger.debug("getUserEntityQuestionsAttemptInfoStat : Before getAttemptedEntity");
        analyticsComponent.getAttemptedEntity(req.entity);
        logger.debug("getUserEntityQuestionsAttemptInfoStat : After getAttemptedEntity");
        UserEntityAttempt entityAttempt = getAttempt(forUserId,
                req.entity.type, req.entity.id);
        if (entityAttempt == null) {
            logger.debug("getUserEntityQuestionsAttemptInfoStat : Test not attempted, Checking whether test is LIVE or NOT");
            if (req.target != null) {
                AtomicLong totalHits = new AtomicLong();
                LibraryContentLink cLink = getLibraryContentLink(new SrcEntity(
                        EntityType.TEST, req.entity.id), new SrcEntity(req.target.type,
                        req.target.id), UserActionType.ADDED, VedantuRecordState.ACTIVE, totalHits);
                if (cLink.getSchedule() != null) {
                    if (cLink.getSchedule().startTime != null) {
                        if (cLink.getSchedule().startTime.getTime() - System.currentTimeMillis() > 0) {
                            logger.debug("cLink time " + cLink.getSchedule().startTime.getTime() + " System Time " + System.currentTimeMillis());
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE, "This test starts on @ " + cLink.getSchedule().startTime.getTime());
                        }
                    }
                    if (cLink.getSchedule().closeTime != null) {
                        if (System.currentTimeMillis() - cLink.getSchedule().closeTime.getTime() > 0) {
                            logger.debug("cLink time " + cLink.getSchedule().closeTime.getTime() + " System Time " + System.currentTimeMillis());
                            throw new VedantuException(VedantuErrorCode.TEST_IS_NOT_LIVE, "This test was ended on @ " + cLink.getSchedule().closeTime.getTime());
                        }
                    }
                }
            } else {
                throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "SectionId is missing");
            }
        }
        if (entityAttempt == null) {
            String msg = "user[" + forUserId + "] has not attempted entity [" + req.entity + "]";
            logger.error(msg);
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED, msg);
        }

        if (!entityAttempt.finished || entityAttempt.endTime <= 0) {
            String msg = "user[" + forUserId + "] attempted for entity [" + req.entity
                    + "] is in progress";
            logger.error(msg);
            throw new VedantuException(VedantuErrorCode.ATTEMPT_IN_PROGRESS, msg);
        }

        GetUserEntityQuestionAttemptStatInfoListRes res = new GetUserEntityQuestionAttemptStatInfoListRes();
        res.totalHits = entityAttempt.qIds == null ? 0 : entityAttempt.qIds.size();
        res.startTime = entityAttempt.timeCreated;
        res.endTime = entityAttempt.endTime;
        UserEntityAnalytics userEntityAnalytics = getAnalytics(
                forUserId, entityAttempt._getStringId(), req.entity, AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        res.timeTaken = userEntityAnalytics == null ? 0 : userEntityAnalytics.measures.timeTaken;

        List<String> resultQueryQids = entityAttempt.qIds;
        final boolean defaultOrder = StringUtils.isEmpty(req.orderBy);
        int lastQusNo = req.start;
        if (defaultOrder) {
            req.start = Math.min(Math.max(0, req.start), resultQueryQids.size());
            req.size = Math.min(Math.max(0, req.size), (resultQueryQids.size() - req.start));
            if (req.size == 0) {
                req.size = resultQueryQids.size() - req.start;
            }
            logger.info("default order start: " + req.start + ", size:" + req.size);
            resultQueryQids = resultQueryQids.subList(req.start, req.start + req.size);
            req.start = 0; // set it to zero so that MONGO will not skip the
            // results as we have already skiped
        }

        // now fetch question attempts for entity==req.entity
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.USER_ID).is(req._getResultForUserId());
        criteria.and("parentEntity.id").is(req.entity.id);
        criteria.and("parentEntity.type").is(req.entity.type.name());
        criteria.and(ConstantsGlobal.QID).is(resultQueryQids.toArray());
        query.limit(req.getSize());
        query.skip(req.getStart());
        query.addCriteria(criteria);
        List<UserQuestionAnalytics> questionAnalytics = mongoTemplate.find(query, UserQuestionAnalytics.class);
        Map<String, UserQuestionAnalytics> questionAnalyticsMap = new HashMap<String, UserQuestionAnalytics>();
        for (UserQuestionAnalytics qa : questionAnalytics) {
            questionAnalyticsMap.put(qa.qId, qa);
        }
        Map<String, ContentSearchDetails> questionContentInfoMap = null;
        Map<String, Answer> answerMap = null;
        if (req.downloadQuestions) {
            answerMap = analyticsComponent.getQuestionAnswerMap(resultQueryQids);
            Map<String, QuestionSearchIndexDetails> questionInfoMap = questionComponent.getQuestionsMap(resultQueryQids);

            List<ContentSearchDetails> questionAnnotatedInfos = new ArrayList<ContentSearchDetails>();

            for (Map.Entry<String, QuestionSearchIndexDetails> entry : questionInfoMap.entrySet()) {
                try {
                    questionAnnotatedInfos.add(entry.getValue().__getContentSearchDetails());
                } catch (JSONException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            questionInfoMap.clear();
            questionInfoMap = null;

            questionComponent.annotateExtraInfo("", req.userId, req.orgId, req.entity.type, questionAnnotatedInfos, true);

            questionContentInfoMap = new HashMap<String, ContentSearchDetails>();

            for (ContentSearchDetails qDetails : questionAnnotatedInfos) {
                logger.debug("question search details : " + qDetails);
                questionContentInfoMap.put(qDetails.id, qDetails);
            }
        }

        for (int i = 0; i < resultQueryQids.size(); i++) {
            UserQuestionAnalytics userQA = questionAnalyticsMap.get(resultQueryQids.get(i));
            QuestionAttemptStatsInfo qAanalyticInfo = null;

            if (userQA == null) {
                qAanalyticInfo = new QuestionAttemptStatsInfo(lastQusNo + i + 1, req.entity,
                        resultQueryQids.get(i), AttemptStatus.LEFT, null,
                        null, AnswerCorrectness.INCORRECT, 0, 0, 0);
            } else {
                qAanalyticInfo = new QuestionAttemptStatsInfo(i + 1, userQA.parentEntity,
                        userQA.qId, AttemptStatus.ATTEMPTED, userQA.answerGiven,
                        userQA.matrixAnswerGiven, userQA.isCorrect, userQA.score, userQA.timeTaken,
                        userQA.timeCreated);
            }

            if (questionContentInfoMap != null
                    && questionContentInfoMap.get(resultQueryQids.get(i)) != null) {
                ContentSearchDetails cSearchDetails = questionContentInfoMap.get(resultQueryQids
                        .get(i));
                if (answerMap != null) {
                    GetSolutionsReq solutionsReq = new GetSolutionsReq();
                    solutionsReq.qId = cSearchDetails.id;
                    solutionsReq.userId = cSearchDetails.userId;
                    GetSolutionsRes solutionRes = questionComponent.getSolutions(solutionsReq);
                    AbstractContentManager.annotateQuestionAnswerInfo(
                            answerMap.get(resultQueryQids.get(i)), solutionRes, cSearchDetails);
                }
                qAanalyticInfo.content = cSearchDetails;

            }
            res.list.add(qAanalyticInfo);
        }
        return new VedantuResponse(res);
    }


    @Override
    public VedantuResponse endAttempt(EndAttemptReq endAttemptReq) {
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId, endAttemptReq.attemptId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        EndAttemptRes endAttemptRes = null;
        try {
            endAttemptRes = endAttempt(endAttemptReq, System.currentTimeMillis());
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(endAttemptRes);

    }


    @Override
    public VedantuResponse recordAttempt(RecordAttemptReq recordAttemptReq) {
        if (EntityType.QUESTION == recordAttemptReq.entityType || EntityType.ASSIGNMENT == recordAttemptReq.entityType) {
            if (ObjectIdUtils.hasInvalidId(recordAttemptReq.entityId, recordAttemptReq.qId)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ID);

            }
        } else {
            if (ObjectIdUtils.hasInvalidId(recordAttemptReq.entityId, recordAttemptReq.attemptId,
                    recordAttemptReq.qId)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ID);

            }
        }
        RecordAttemptRes recordAttemptRes = null;
        try {
            recordAttemptRes = analyticsComponent.recordAttempt(recordAttemptReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(recordAttemptRes);

    }


    @Override
    public VedantuResponse resetQuestionAttempt(ResetQuestionAttemptReq resetQuestionAttemptReq) {
        ResetQuestionAttemptRes resetQuestionAttemptRes = null;
        try {
            resetQuestionAttemptRes = analyticsComponent
                    .resetQuestionAttempt(resetQuestionAttemptReq);
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(resetQuestionAttemptRes);
    }

    @Override
    public VedantuResponse getQuestionAnalytics(GetQuestionAnalyticsReq getQuestionAnalyticsReq) {
        if (ObjectIdUtils.hasInvalidId(getQuestionAnalyticsReq.entityId,
                getQuestionAnalyticsReq.qId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        GetQuestionAnalyticsRes getQuestionAnalyticsRes = null;
        try {
            getQuestionAnalyticsRes = analyticsComponent
                    .getQuestionAnalytics(getQuestionAnalyticsReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getQuestionAnalyticsRes);
    }

    @Override
    public VedantuResponse getEntityResultAnalytics(GetEntityResultAnalyticsReq getEntityResultAnalyticsReq) {
        GetEntityResultAnalyticsRes getAnalyticsResultRes = null;
        try {
            getAnalyticsResultRes = analyticsComponent.getEntityResultAnalytics(
                    getEntityResultAnalyticsReq, true);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getAnalyticsResultRes);
    }

    @Override
    public VedantuResponse getUserEntityAttemptStatusInfo(GetUserEntityAttemptStatusInfoReq req) {

        UserEntityAttempt userEntityAttempt = getAttempt(
                req._getResultForUserId(), req.entity.type, req.entity.id);
        GetUserEntityAttemptStatusInfoRes res = new GetUserEntityAttemptStatusInfoRes();
        if (userEntityAttempt != null) {
            res.attempted = true;
            res.completed = userEntityAttempt.endTime > 0;
            res.startTime = userEntityAttempt.timeCreated;
            res.endTime = userEntityAttempt.endTime;

            // below code is being added for allowing UI for showing POST
            // TEST PAGE in case of off-line test

//            if (req.entity.type == EntityType.TEST) {
//                AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);
//                res.type = test.type;
//                res.mode = test.mode;
//            }
        }

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getUserAnalyticsStats(GetUserAnalyticsStatsReq req) {

        String userId = req._getResultForUserId();
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = analyticsComponent.getUserEntityAnalyticsMap(
                Arrays.asList(AcademicDimensionType.OVERALL.name()), Arrays.asList(userId),
                req.entityType, true);
        Set<String> brdIds = new HashSet<String>();
        GetUserAnalyticsStatsRes res = new GetUserAnalyticsStatsRes();
        for (Map.Entry<String, UserEntityAnalytics> entry : userEntityAnalyticsMap.entrySet()) {
            String[] keys = entry.getKey().split("_");
            if (!(keys[2].equalsIgnoreCase(AcademicDimensionType.OVERALL.name()))) {
                brdIds.add(keys[2]);
            }
        }

        Map<String, BoardBasicInfo> boardInfo = getBasicInfosByIds(brdIds);
        for (Map.Entry<String, UserEntityAnalytics> entry : userEntityAnalyticsMap.entrySet()) {
            String[] keys = entry.getKey().split("_");
            if ((keys[2].equalsIgnoreCase(AcademicDimensionType.OVERALL.name()))) {
                res.measures = entry.getValue().measures;
                res.percentage = entry.getValue().percentage;
                Criteria criteria = new Criteria();
                Query query = new Query();
                criteria.and(ConstantsGlobal.USER_ID).is(userId);
                criteria.and(ConstantsGlobal.PARENT).is(null);
                criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(req.entityType.name());
                query.addCriteria(criteria);
                res.totalAttempts = mongoTemplate.count(query, UserEntityAttempt.class);
            } else {
                res.addBoardAnalytics(boardInfo.get(keys[2]), entry.getValue().measures,
                        entry.getValue().percentage);
            }
        }
        return new VedantuResponse(res);
    }
}


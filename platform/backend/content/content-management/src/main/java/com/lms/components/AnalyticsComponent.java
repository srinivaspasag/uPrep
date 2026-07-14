package com.lms.components;


import com.amazonaws.services.ecs.model.SortOrder;
import com.lms.api.IAnalyticsBoardMember;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.MarkDistribution;
import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.utils.ObjectMapperUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.AcademicDimensionType;
import com.lms.enums.AnswerCorrectness;
import com.lms.enums.AttemptStatus;
import com.lms.enums.TestResultVisibility;
import com.lms.managers.AnalyticsManager;
import com.lms.models.*;
import com.lms.models.analytics.EntityAnalytics;
import com.lms.models.analytics.QuestionAnalytics;
import com.lms.models.analytics.UserEntityAnalytics;
import com.lms.models.analytics.UserQuestionAnalytics;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojo.*;
import com.lms.pojos.*;
import com.lms.pojos.requests.*;
import com.lms.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.lms.pojos.requests.analytics.*;
import com.lms.pojos.responce.GetEntityResultAnalyticsRes;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.analytics.*;
import com.lms.pojos.responce.analytics.answers.BoardWiseQuestionsAttemptInfos;
import com.lms.pojos.responce.analytics.answers.QuestionAttemptInfo;
import com.lms.pojos.responce.analytics.answers.QuestionAttemptStatsInfoDetail;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.pojos.tests.BoardAnalyticsInfo;
import com.lms.pojos.tests.BoardQus;
import com.lms.pojos.tests.EntityInfo;
import com.lms.pojos.tests.TestQuestionSet;
import com.lms.repository.*;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.utils.EntityQuestionAttemptInfoComparator;
import com.mongodb.BasicDBObject;
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
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AnalyticsComponent extends AnalyticsManager {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsComponent.class);
    public final int INCLUDE_FIELD = 1;
    public final int EXCLUDE_FIELD = 0;
    public final int NO_START = 0;
    public final int NO_LIMIT = 0;
    public final String IN_QUERY = "$in";
    private static final String ACAD_DIM_TYPE = "acadDim.type";
    private static final String ACAD_DIM_ID = "acadDim.id";
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private EntityAnalyticsRepo entityAnalyticsRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private UserEntityAnalyticsRepo userEntityAnalyticsRepo;
    @Autowired
    private EntityHighscoreRepo entityHighscoreRepo;
    @Autowired
    private AnalyticsManager analyticsManager;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private AnswerRepo answerRepo;
    @Autowired
    private UserQuestionAnalyticsRepo userQuestionAnalyticsRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private OrgCenterRepo centerRepo;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;


    public GetEntityResultAnalyticsRes getEntityResultAnalytics(
            GetEntityResultAnalyticsReq req, boolean addUsersBoardWiseAnalytics)
            throws VedantuException {

        GetEntityResultAnalyticsRes res = new GetEntityResultAnalyticsRes();

        Optional<Test> test1 = testRepo.findById(req.getEntity().getId());
        if (!test1.isPresent()) {
            logger.error("no entity found entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND);
        }
        Test test = test1.get();

        Set<String> entityIds = new HashSet<String>();
        res.info = toMiniInfo(test, entityIds);
        EntityAnalytics entityAnalytics = getEntityAnalytics(
                req.entity, AcademicDimensionType.OVERALL, AcademicDimensionType.OVERALL.name());
        if (entityAnalytics == null || entityAnalytics.measures == null) {
            return res;
        }

        final String acadDimId = AcademicDimensionType.OVERALL.name();

        Set<String> userIds = new HashSet<String>();
        Query analyticQuery = new Query();
        Criteria criteria = new Criteria();
        // DBObject analyticQuery = new BasicDBObject(ConstantsGlobal.ENTITY + "."+ ConstantsGlobal.ID, new BasicDBObject(MongoManager.IN_QUERY,new String[] { req.entity.id }));
        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(acadDimId);
        logger.debug("OrgId Before adding into analyticQuery " + req.orgId);
        criteria.and(ConstantsGlobal.ORG_ID).is(req.orgId);

        int rank = 0;
        long prevTimeTaken = 0;
        double prevScore = -Integer.MAX_VALUE;
        Query sortQuery = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and("measures.score").is(SortOrder.DESC.name());
        criteria1.and("measures.timeTaken").is(SortOrder.ASC.name());
        analyticQuery.addCriteria(criteria.andOperator(criteria1));
        //analyticQuery.addCriteria(criteria1);
        sortQuery.addCriteria(criteria1);

        if (req.start > 0) {
            // than find out the last user displayed in the result list and
            // her corresponding rank

            List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(analyticQuery, UserEntityAnalytics.class);
            if (!userEntityAnalytics.isEmpty()) {
                UserEntityAnalytics lastUserAnalytics = userEntityAnalytics.get(0);
                prevScore = lastUserAnalytics.measures.score;
                prevTimeTaken = lastUserAnalytics.measures.timeTaken;
                logger.debug("OrgId Before GetRank " + req.orgId);
                rank = getRank(req.orgId, req.entity.id, prevScore,
                        prevTimeTaken, AcademicDimensionType.OVERALL.name());
            }
        }

        // this will only get the overall acadDimId results
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(analyticQuery, UserEntityAnalytics.class);

        res.totalHits = userEntityAnalytics.stream().count();
        for (UserEntityAnalytics uA : userEntityAnalytics) {

            if (req.isDetailedResultSheet) {
                logger.debug("***********        " + uA.measures.score + "         *************");
                if (uA.measures.score <= req.maxScore && uA.measures.score >= req.minScore) {
                    userIds.add(uA.userId);
                }
            } else {
                userIds.add(uA.userId);
            }
        }

        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, userIds, addUsersBoardWiseAnalytics);
        logger.debug("collecting data for users : " + userIds);

        Map<String, UserEntityAttempt> userEntityAttemptInfoMap = getUserEntityAttemptInfoMap(
                entityIds, userIds);
        logger.debug("OrgId Before adding into getUserInfoMap " + req.orgId);
        Map<String, ModelBasicInfo> userIdToBasicInfoMap = getUserInfoMap(StringUtils.isEmpty(req.orgId) ? null : req.orgId, userIds);
        int rankFlag = 0;
        for (UserEntityAnalytics uA : userEntityAnalytics) {
            // if(req.isDetailedResultSheet){
            // LOGGER.debug("***********        "+uA.measures.score+"         *************");
            // if(uA.measures.score <= req.maxScore && uA.measures.score >=
            // req.minScore){
            // if (uA.measures.score != prevScore || uA.measures.timeTaken !=
            // prevTimeTaken) {
            // rank++;
            // prevScore = uA.measures.score;
            // prevTimeTaken = uA.measures.timeTaken;
            // }
            // GetUserEntityResultAnalyticsRes analytics = new
            // GetUserEntityResultAnalyticsRes();
            // analytics.rank = rank;
            // analytics.id = res.info.id;
            // analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
            // analytics.info = __getUserAnalyticsInfo(uA.userId,
            // req.entity.type, res.info,
            // userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true,
            // true, null);
            // res.list.add(analytics);
            // }
            // }else{
            if (uA.measures.score != prevScore || uA.measures.timeTaken != prevTimeTaken) {
                rank++;
                prevScore = uA.measures.score;
                prevTimeTaken = uA.measures.timeTaken;
            }
            if (req.isDetailedResultSheet) {
                if (uA.measures.score <= req.maxScore && uA.measures.score >= req.minScore) {
                    GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
                    analytics.rank = getRank(req.orgId, req.entity.id, uA.measures.score,
                            uA.measures.timeTaken, AcademicDimensionType.OVERALL.name());
                    analytics.id = res.info.id;
                    analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
                    analytics.info = __getUserAnalyticsInfo(uA.userId, req.entity.type, res.info,
                            userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true,
                            null);
                    res.list.add(analytics);
                }
            } else {
                GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
                analytics.rank = getRank(req.orgId, req.entity.id, uA.measures.score,
                        uA.measures.timeTaken, AcademicDimensionType.OVERALL.name());
                analytics.id = res.info.id;
                analytics.user = (UserInfo) userIdToBasicInfoMap.get(uA.userId);
                analytics.info = __getUserAnalyticsInfo(uA.userId, req.entity.type, res.info,
                        userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true, null);
                res.list.add(analytics);
            }

            // }
        }
        return res;

    }

    private TestMiniInfo toMiniInfo(AbstractTestCommonModel test, Set<String> entityIds) {

        TestMiniInfo miniInfo = new TestMiniInfo(test.name, test.code, test._getStringId(),
                test.qusCount, test.duration, test.totalMarks);
        miniInfo.attempts = test.attempts;
        entityIds.add(miniInfo.id);
        miniInfo.metadata = test.metadata;
        miniInfo.resultVisibility = test.resultVisibility;

        if (test.childrenIds != null) {
            List<Test> testList = testRepo.findAllByIdIn(test.childrenIds);
            Map<String, Test> tests = toInfosMap(testList);
            for (String testId : test.childrenIds) {
                miniInfo.addChild(toMiniInfo(tests.get(testId), entityIds));
            }
        }
        removeExtracData(miniInfo);
        return miniInfo;
    }

    public final Map<String, Test> toInfosMap(Collection<Test> results) {

        Map<String, Test> infosMap = new LinkedHashMap<String, Test>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Test t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), t);
            }
        }
        return infosMap;
    }

    private void removeExtracData(TestMiniInfo testInfo) {

        // this block remove, not required data from respose object
        for (TestMetadata mdata : testInfo.metadata) {
            mdata.marks = null;
            mdata.qIds = null;
            if (mdata.details != null) {
                for (TestDetails details : mdata.details) {
                    details.qIds = null;
                }
            }
            if (mdata.children != null) {
                for (BoardQus b : mdata.children) {
                    b.qIds = null;
                }
            }
        }
    }

    public EntityAnalytics getEntityAnalytics(SrcEntity entity, AcademicDimensionType acadDimType,
                                              String acadDimId) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ENTITY).is(entity);
        criteria.and(ACAD_DIM_TYPE).is(acadDimType);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        query.addCriteria(criteria);
        EntityAnalytics entityAnalytics = mongoTemplate.findOne(query, EntityAnalytics.class);
//     EntityAnalytics entityAnalytics = entityAnalyticsRepo.findByEntityTypeAndEntityIdAndAcadDimTypeAndAcadDimId(entity.getType(), entity.getId().trim(), acadDimType, acadDimId);
        return entityAnalytics;


    }

    public List<UserEntityAnalytics> getUserEntityAnalyticsList(SrcEntity entity, String acadDimId, String orgId, AtomicLong hits) {
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entity.id);
        criteria.and(ACAD_DIM_ID).is(acadDimId);
        criteria.and("orgId").is(orgId);
        query.addCriteria(criteria);
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(query, UserEntityAnalytics.class);
        if (hits != null) {
            hits.set(userEntityAnalytics.size());
        }
        return userEntityAnalytics;
    }

    public int getRank(String orgId, String entityId, double score, long timeTaken, String acadDimId) {

        Query query = new Query();
        Criteria criteria = new Criteria();


        // QueryImpl<UserEntityAnalytics> query = (QueryImpl<UserEntityAnalytics>) getQuery()
        if (orgId != null)
            criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(entityId);
        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(acadDimId);
        criteria.and("measures.score").gte(score);
        query.addCriteria(criteria);

        Query matchQuery = new Query();
        matchQuery.addCriteria(criteria);

        String groupQueryString = "{\"$group\" : {_id: {score: \"$measures.score\", timeTaken : \"$measures.timeTaken\"}}}";


        Query sortQuery = new Query();
        Criteria criteria1 = new Criteria();
        criteria.and("_id.score").is(SortOrder.DESC.name());
        criteria.and("_id.timeTaken").is(SortOrder.ASC.name());
        sortQuery.addCriteria(criteria1);
        matchQuery.addCriteria(criteria);
        matchQuery.with(Sort.by(Sort.Direction.DESC, "_id.score"));
        matchQuery.with(Sort.by(Sort.Direction.ASC, "_id.timeTaken"));
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(matchQuery, UserEntityAnalytics.class);


        int rank = 1;
        final double TOLERANCE = 0.0000001d;
        for (UserEntityAnalytics result : userEntityAnalytics) {
            // DBObject _id = (DBObject) result.get("_id");
            String id = result.getId().toString();
            if (Math.abs(score - Double.parseDouble(String.valueOf(result))) <= TOLERANCE) {
                break;
            }
            rank++;
        }
        return rank;
    }

    private Map<String, UserEntityAnalytics> getUserEntityAnalyticsMap(
            Collection<String> entityIds, Collection<String> userIds, boolean addBoardWiseAnalytics) {

        return getUserEntityAnalyticsMap(entityIds, userIds, null, addBoardWiseAnalytics);
    }

    public Map<String, UserEntityAnalytics> getUserEntityAnalyticsMap(
            Collection<String> entityIds, Collection<String> userIds, EntityType entityType,
            boolean addBoardWiseAnalytics) {

        Query analyticQuery = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).in(entityIds.toArray());
        if (entityType != null) {
            criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(entityType.name());
        }
        criteria.and(ConstantsGlobal.USER_ID).in(userIds.toArray());
        if (!addBoardWiseAnalytics) {
            criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(AcademicDimensionType.OVERALL.name());
        }
        analyticQuery.addCriteria(criteria);
        List<UserEntityAnalytics> userEntityAnalytics = mongoTemplate.find(analyticQuery, UserEntityAnalytics.class);
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = new HashMap<String, UserEntityAnalytics>();
        for (UserEntityAnalytics uA : userEntityAnalytics) {
            String key = __getUserEntityAnalyticsMapKey(uA.entity.id, uA.userId, uA.acadDim.id);
            userEntityAnalyticsMap.put(key, uA);
        }
        return userEntityAnalyticsMap;

    }

    private String __getUserEntityAnalyticsMapKey(String entityId, String userId,
                                                  String acadDimId) {

        return entityId + "_" + userId + "_" + acadDimId;
    }

    private Map<String, UserEntityAttempt> getUserEntityAttemptInfoMap(
            Collection<String> entityIds, Collection<String> userIds) {
        Query analyticQuery = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY_ID).in(entityIds.toArray());
        criteria.and(ConstantsGlobal.USER_ID).in(entityIds.toArray());

        criteria.and(ConstantsGlobal.USER_ID).in(userIds.toArray());

        List<UserEntityAttempt> userEntityAnalytics = mongoTemplate.find(analyticQuery, UserEntityAttempt.class);
        Map<String, UserEntityAttempt> userEntityAnalyticsMap = new HashMap<String, UserEntityAttempt>();
        for (UserEntityAttempt uA : userEntityAnalytics) {
            String key = __getUserEntityAttemptMapKey(uA.entity.id, uA.userId);
            userEntityAnalyticsMap.put(key, uA);
        }
        return userEntityAnalyticsMap;

    }

    private String __getUserEntityAttemptMapKey(String entityId, String userId) {

        return entityId + "_" + userId;
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
                                                      Collection<String> userIds) {

        return getUserInfoMap(orgId, userIds, false);
    }

    private UserAnalyticsInfoRes __getUserAnalyticsInfo(String userId,
                                                        EntityType entityType, TestMiniInfo testInfo,
                                                        Map<String, UserEntityAnalytics> userEntityAnalyticsMap,
                                                        Map<String, UserEntityAttempt> userEntityAttemptInfoMap,
                                                        Map<String, EntityAnalytics> entityAnalyticsMap, boolean addTotalMarks,
                                                        boolean addRankInAllDim, Map<String, Integer> entityLastRankMap) {

        String orgId = userId.equals("PUBLIC") ? "" : orgMemberRepo.findByUserId(userId).getOrgId();

        EntityInfo entity = new EntityInfo(entityType, testInfo.id, testInfo.name);
        final String acadDimId = AcademicDimensionType.OVERALL.name();
        EntityAnalytics entityAnalytics = entityAnalyticsMap == null ? null : entityAnalyticsMap
                .get(__getEntityAnalyticsMapKey(entity.id, acadDimId));
        entity.measures = entityAnalytics == null ? null : entityAnalytics.measures;
        entity.totalAttempts = StringUtils.isEmpty(orgId) ? getAnalyticsCount(null, entity, acadDimId) : getAnalyticsCount(orgId, entity, acadDimId);

        Integer lastRank = entityLastRankMap == null ? null : entityLastRankMap
                .get(__getEntityAnalyticsMapKey(entity.id, acadDimId));
        entity.lastRank = lastRank == null ? 0 : lastRank.intValue();
        UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(entity.id, userId, acadDimId));
        UserAnalyticsInfoRes userAnalyticsRes = null;
        if (userEntityAnalytics != null) {
            userAnalyticsRes = new UserAnalyticsInfoRes(entity,
                    userEntityAnalytics.measures);
        } else {
            userAnalyticsRes = new UserAnalyticsInfoRes(entity,
                    new EntityMeasures());
        }
        UserEntityAttempt attemptInfo = userEntityAttemptInfoMap == null ? null
                : userEntityAttemptInfoMap.get(__getUserEntityAttemptMapKey(entity.id, userId));
        if (attemptInfo != null) {
            userAnalyticsRes.startTime = attemptInfo.timeCreated;
            userAnalyticsRes.endTime = attemptInfo.endTime;
        }
        if (addTotalMarks) {
            userAnalyticsRes.qusCount = testInfo.qusCount;
            userAnalyticsRes.totalMarks = testInfo.totalMarks;
        }
        if (addRankInAllDim) {
            userAnalyticsRes.rank = StringUtils.isEmpty(orgId) ? getRank(null, entity.id, userAnalyticsRes.measures.score,
                    userAnalyticsRes.measures.timeTaken, acadDimId) : getRank(orgId, entity.id, userAnalyticsRes.measures.score,
                    userAnalyticsRes.measures.timeTaken, acadDimId);
        }
        if (testInfo.children != null) {
            for (TestMiniInfo childInfo : testInfo.children) {
                UserAnalyticsInfoRes childAnalyticInfo = __getUserAnalyticsInfo(userId, entityType,
                        childInfo, userEntityAnalyticsMap, userEntityAttemptInfoMap,
                        entityAnalyticsMap, addTotalMarks, addRankInAllDim, entityLastRankMap);
                userAnalyticsRes.addChildAnalytics(childAnalyticInfo);
            }
        }

        for (IAnalyticsBoardMember boardMember : testInfo.metadata) {
            userAnalyticsRes.addBoardAnalytics(__getUserEntityBoardAnalyticsInfo(entity.id, userId,
                    boardMember, userEntityAnalyticsMap, entityAnalyticsMap, addTotalMarks,
                    addRankInAllDim, entityLastRankMap));
        }
        return userAnalyticsRes;
    }

    private String __getEntityAnalyticsMapKey(String entityId, String acadDimId) {

        return entityId + "_" + acadDimId;
    }

    public long getAnalyticsCount(String orgId, SrcEntity entity, String acadDimId) {
        List<UserEntityAnalytics> userAnalytics = null;
        if (orgId != null)
            userAnalytics = userEntityAnalyticsRepo.findByOrgIdAndEntityIdAndAcadDimId(orgId, entity.id, acadDimId);

        userAnalytics = userEntityAnalyticsRepo.findByEntityIdAndAcadDimId(entity.id, acadDimId);
        return userAnalytics.stream().count();
    }

    private UserBoardAnalyticsInfoRes __getUserEntityBoardAnalyticsInfo(String entityId,
                                                                        String userId, IAnalyticsBoardMember iBoardMember,
                                                                        Map<String, UserEntityAnalytics> userEntityAnalyticsMap,
                                                                        Map<String, EntityAnalytics> entityAnalyticsMap, boolean addTotalMarks,
                                                                        boolean addRankInAllDim, Map<String, Integer> entityLastRankMap) {
        String orgId = userId.equals("PUBLIC") ? "" : orgMemberRepo.findByUserId(userId).getOrgId();
        UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(entityId, userId, iBoardMember._getEntity().id));
        if (userEntityAnalytics == null) {
            return null;
        }
        UserBoardAnalyticsInfoRes boardAnalytics = new UserBoardAnalyticsInfoRes(
                iBoardMember._getEntity(), userEntityAnalytics.measures);
        if (addTotalMarks) {
            boardAnalytics.totalMarks = iBoardMember._getTotalMarks();
            boardAnalytics.qusCount = iBoardMember._getQusCount();
            EntityAnalytics entityAnalytics = entityAnalyticsMap == null ? null
                    : entityAnalyticsMap.get(__getEntityAnalyticsMapKey(entityId,
                    iBoardMember._getEntity().id));
            boardAnalytics.entity.measures = entityAnalytics == null ? null
                    : entityAnalytics.measures;
            Integer lastRank = entityLastRankMap == null ? null : entityLastRankMap
                    .get(__getEntityAnalyticsMapKey(entityId, iBoardMember._getEntity().id));
            boardAnalytics.entity.lastRank = lastRank == null ? 0 : lastRank.intValue();
        }
        if (addRankInAllDim) {
            boardAnalytics.rank = StringUtils.isEmpty(orgId) ? getRank(null, iBoardMember._getEntity().id, boardAnalytics.measures.score,
                    boardAnalytics.measures.timeTaken, iBoardMember._getEntity().id)
                    : getRank(orgId, iBoardMember._getEntity().id,
                    boardAnalytics.measures.score, boardAnalytics.measures.timeTaken,
                    iBoardMember._getEntity().id);
        }
        if (iBoardMember._getChildrenBoards() != null) {
            for (IAnalyticsBoardMember boardMember : iBoardMember._getChildrenBoards()) {
                boardAnalytics.addChildAnalytics(__getUserEntityBoardAnalyticsInfo(entityId,
                        userId, boardMember, userEntityAnalyticsMap, entityAnalyticsMap,
                        addTotalMarks, addRankInAllDim, entityLastRankMap));
            }
        }
        return boardAnalytics;

    }

    public GetUserEntityResultAnalyticsSingleEntityRes getUserEntityAnalytics(
            GetUserEntityAnalyticsReq req) throws VedantuException {

        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);

        GetUserEntityResultAnalyticsSingleEntityRes res = new GetUserEntityResultAnalyticsSingleEntityRes();
        final String acadDimId = AcademicDimensionType.OVERALL.name();

        // res.totalAttempts == how many users has attempted this test
        res.totalAttempts = getAnalyticsCount(req.orgId, req.entity, acadDimId);
        res.AIAttempts = getAnalyticsCount(null, req.entity, acadDimId);
        final String userId = req._getResultForUserId();

        Set<String> entityIds = new HashSet<String>();
        TestMiniInfo testInfo = toMiniInfo(test, entityIds);
        res.id = req.entity.id;
        res.user = getUserInfo(req.orgId, userId);
        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, Arrays.asList(userId), true);
        Map<String, UserEntityAttempt> userEntityAttemptInfoMap = getUserEntityAttemptInfoMap(
                entityIds, Arrays.asList(userId));
        UserEntityAnalytics userAnalytics = userEntityAnalyticsMap
                .get(__getUserEntityAnalyticsMapKey(req.entity.id, userId, acadDimId));
        if (userAnalytics != null) {
            if (test.showAIR) {
                res.showAIR = true;
                res.AIR = getRank(null, req.entity.id,
                        userAnalytics.measures.score, userAnalytics.measures.timeTaken, acadDimId);
            }
            res.rank = getRank(req.orgId, req.entity.id,
                    userAnalytics.measures.score, userAnalytics.measures.timeTaken, acadDimId);
            res.info = __getUserAnalyticsInfo(userId, req.entity.type, testInfo,
                    userEntityAnalyticsMap, userEntityAttemptInfoMap, null, true, true, null);
        }
        return res;
    }

    public UserInfo getUserInfo(String orgId, String userId, boolean excludeOrgMappingInfo) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId),
                excludeOrgMappingInfo);

        return (UserInfo) userInfos.get(userId);
    }

    public UserInfo getUserInfo(String orgId, String userId) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId));

        return (UserInfo) userInfos.get(userId);
    }

    public GetUserEntityResultAnalyticsListRes getUserEntityResultAnalytics(
            GetUserEntityResultAnalyticsReq req) throws VedantuException {

        String userId = req.__getResultForUserId();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY_DOT_TYPE).is(req.entityType.name());
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        query.addCriteria(criteria);
        // fetch all the entities{entityType} attempted by this user
        List<UserEntityAttempt> results = mongoTemplate.find(query, UserEntityAttempt.class);
        Set<String> entityIds = new HashSet<String>();
        for (UserEntityAttempt uA : results) {
            entityIds.add(uA.entity.id);
        }
        Query entityQuery = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and(ConstantsGlobal._ID).in(ObjectIdUtils.toObjectIds(new ArrayList<String>(entityIds)));
        entityQuery.addCriteria(criteria1);
        // TODO: enable this for assignment too
        List<Test> tests = mongoTemplate.find(entityQuery, Test.class);
        Map<String, TestMiniInfo> testMiniInfoMap = new HashMap<String, TestMiniInfo>();
        for (Test t : tests) {
            TestMiniInfo miniInfo = toMiniInfo(t, entityIds);
            testMiniInfoMap.put(t._getStringId(), miniInfo);
        }

        Map<String, UserEntityAnalytics> userEntityAnalyticsMap = getUserEntityAnalyticsMap(
                entityIds, Arrays.asList(userId), true);
        Map<String, EntityAnalytics> entityAnalyticsMap = getEntityAnalyticsMap(entityIds);
        Map<String, Integer> entityLastRankMap = getEntityLastRankMap(entityIds);

        GetUserEntityResultAnalyticsListRes res = new GetUserEntityResultAnalyticsListRes();
        res.totalHits = results.stream().count();
        for (UserEntityAttempt uA : results) {
            GetUserEntityResultAnalyticsRes analytics = new GetUserEntityResultAnalyticsRes();
            String key = __getUserEntityAnalyticsMapKey(uA.entity.id, uA.userId,
                    AcademicDimensionType.OVERALL.name());
            UserEntityAnalytics userEntityAnalytics = userEntityAnalyticsMap.get(key);
            TestMiniInfo tMiniInfo = testMiniInfoMap.get(uA.entity.id);
            if (userEntityAnalytics == null || tMiniInfo == null) {
                continue;
            }

            if (tMiniInfo.resultVisibility != TestResultVisibility.HIDDEN) {
                analytics.rank = getRank(req.orgId, uA.entity.id,
                        userEntityAnalytics.measures.score, userEntityAnalytics.measures.timeTaken,
                        userEntityAnalytics.acadDim.id);
            }
            analytics.id = uA.entity.id;
            analytics.resultVisibility = tMiniInfo.resultVisibility;
            analytics.info = __getUserAnalyticsInfo(uA.userId, uA.entity.type, tMiniInfo,
                    userEntityAnalyticsMap, null, entityAnalyticsMap, true, true, entityLastRankMap);
            analytics.info.endTime = uA.endTime;
            analytics.info.startTime = uA.timeCreated;
            res.list.add(analytics);
        }
        return res;
    }

    private Map<String, EntityAnalytics> getEntityAnalyticsMap(Collection<String> entityIds) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY).in(entityIds.toArray());
        criteria.and(ConstantsGlobal.ID).in(entityIds.toArray());
        query.addCriteria(criteria);
        List<EntityAnalytics> userEntityAnalytics = mongoTemplate.find(query, EntityAnalytics.class);
        Map<String, EntityAnalytics> entityAnalyticsMap = new HashMap<String, EntityAnalytics>();
        for (EntityAnalytics eA : userEntityAnalytics) {
            String key = __getEntityAnalyticsMapKey(eA.entity.id, eA.acadDim.id);
            entityAnalyticsMap.put(key, eA);
        }
        logger.debug("returning getEntityAnalyticsMap: " + entityAnalyticsMap);
        return entityAnalyticsMap;

    }

    private Map<String, Integer> getEntityLastRankMap(Collection<String> entityIds) {

        Query match = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY).in(entityIds.toArray());
        criteria.and(ConstantsGlobal.ID).in(entityIds.toArray());

        criteria.and(ConstantsGlobal.ENTITY_ID).is("$entity.id");
        criteria.and("acadDimId").is("$acadDim.id");

        criteria.and("lastRank").is(new BasicDBObject("$sum", 1));
        match.addCriteria(criteria);
        List<EntityHighscore> aggregationOutput = mongoTemplate.find(match, EntityHighscore.class);

        Map<String, Integer> userEntityRankMap = new HashMap<String, Integer>();

        for (EntityHighscore result : aggregationOutput) {
            String id = result.getId().toString();
            if (id == null) {
                continue;
            }
            String key = __getEntityAnalyticsMapKey(result.entity.id, result.acadDim.id);
            //TODO:need to clarify h
            userEntityRankMap.put(key, (int) Math.round(result.getScore()));
        }
        return userEntityRankMap;

    }

    public GetUserEntityQuestionAttemptInfoListRes getUserEntityQuestionAttemptInfos(
            GetUserEntityQuestionAttemptStatsReq req) throws VedantuException {

        String userId = req._getResultForUserId();
        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);

        UserEntityAttempt userEntityAttempts = analyticsManager.getAttempt(userId,
                req.entity.type, req.entity.id);
        if (userEntityAttempts == null && test instanceof Test) {
            logger.error("user: " + userId + " has not attempted entity: " + req.entity);
            throw new VedantuException(VedantuErrorCode.NOT_ATTEMPTED);
        }

        List<String> qIds = userEntityAttempts != null ? userEntityAttempts.qIds : null;
        if (qIds == null) {
            qIds = test.__getAllQIds();
        }
        logger.debug("qIds : " + qIds + ", test: " + test);
        // TODO: check if the endAttempt is not done --> should we show the
        // analytics or send an erroCode=ATTEMPT_NOT_COMPLETED

        Map<String, UserQuestionAnalytics> questionAnalyticsMap = getQuestionAnalyticsMap(
                req.entity, userId);
        Map<String, Answer> answerMap = getQuestionAnswerMap(qIds);
        Map<String, QuestionSearchIndexDetails> questionInfoMap = questionComponent.getQuestionsMap(qIds, true);

        GetUserEntityQuestionAttemptInfoListRes questionList = new GetUserEntityQuestionAttemptInfoListRes();
        for (TestMetadata mdata : test.metadata) {
            List<String> mDataQIdsOrder = new ArrayList<String>();
            for (String qId : qIds) {
                if (mdata.qIds.contains(qId)) {
                    mDataQIdsOrder.add(qId);
                }
            }
            qIds.removeAll(mDataQIdsOrder);
            BoardWiseQuestionsAttemptInfos boardQuestion = new BoardWiseQuestionsAttemptInfos(
                    mdata.name, mdata.id);

            for (String qId : mDataQIdsOrder) {
                QuestionSearchIndexDetails detail = questionInfoMap.get(qId);
                Answer answer = answerMap.get(qId);
                UserQuestionAnalytics questionAnalytics = questionAnalyticsMap.get(qId);
                if (detail == null) {
                    continue;
                }

                IQuestionAnswer qAnswer = QuestionComponent.getUserQuestionAnswerGiven(detail, questionAnalytics,
                        answer);

                AttemptStatus attemptStatus = questionAnalytics != null ? AttemptStatus.ATTEMPTED
                        : AttemptStatus.LEFT;
                QuestionAttemptInfo qAttemptInfo = new QuestionAttemptInfo(detail, qAnswer,
                        questionAnalytics == null ? AnswerCorrectness.INCORRECT : questionAnalytics.isCorrect,
                        attemptStatus);
                boardQuestion.addQuestionAttemptInfo(qAttemptInfo);
            }
            questionList.addBoardWiseQuestions(boardQuestion);
        }
        return questionList;
    }

    private Map<String, UserQuestionAnalytics> getQuestionAnalyticsMap(SrcEntity entity,
                                                                       String userId) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("parentEntity.id").is(entity.id);
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        query.addCriteria(criteria);
        List<UserQuestionAnalytics> questionAnalytics = mongoTemplate.find(query, UserQuestionAnalytics.class);
        Map<String, UserQuestionAnalytics> questionAnalyticsMap = new HashMap<String, UserQuestionAnalytics>();
        for (UserQuestionAnalytics qA : questionAnalytics) {
            questionAnalyticsMap.put(qA.qId, qA);
        }
        return questionAnalyticsMap;
    }

    public Map<String, Answer> getQuestionAnswerMap(Collection<String> qIds) {
        Map<String, Answer> answerMap = new HashMap<String, Answer>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.QID).in(qIds);
        query.addCriteria(criteria);
        List<Answer> answers = mongoTemplate.find(query, Answer.class);
        for (Answer ans : answers) {
            answerMap.put(ans.qId, ans);
        }
        for (String qId : qIds) {
            if (!answerMap.containsKey(qId)) {
                Optional<Question> question = questionRepo.findById(qId);
                if (question.get().answerId != null && !question.get().answerId.isEmpty()) {
                    Optional<Answer> answer = answerRepo.findById(question.get().answerId);
                    answerMap.put(qId, answer.get());
                }
            }
        }
        return answerMap;
    }

    public GetEntityQuestionAttemptInfoListRes getEntityQusAttemptInfoDetails(
            GetEntityQuestionsAttemptStatReq req) throws VedantuException {

        logger.info("getEntityQusAttemptInfos request: " + req);
        AbstractTestCommonModel test = (AbstractTestCommonModel) getAttemptedEntity(req.entity);
        logger.info("getEntityQusAttemptInfos got response from getAttemptedEntity");
        TestQuestionSet qusSet = test.__getQuestionSet(req.setName);
        logger.info("getEntityQusAttemptInfos got response from __getQuestionSet");
        // this will define the question no for the specific set
        List<String> qIds = qusSet == null ? test.__getAllQIds() : qusSet.qIds;
        logger.info("getEntityQusAttemptInfos got response from __getAllQIds");
        // create a question order map with qId to qusIndex+1 no.. it's a
        // replacement for qIds.indexOf(Object) for fast iteration
        Map<String, Integer> qIdOrderNoMap = new HashMap<String, Integer>();
        logger.info("getEntityQusAttemptInfos before qIdOrderNoMap");
        for (int i = 0; i < qIds.size(); i++) {
            qIdOrderNoMap.put(qIds.get(i), Integer.valueOf(i + 1));
        }
        logger.info("getEntityQusAttemptInfos after qIdOrderNoMap");
        List<String> resultQueryQids = getQIdsSubList(req.brdId, test, qIds);
        logger.info("getEntityQusAttemptInfos got response from  getQIdsSubList");
        final boolean defaultOrder = StringUtils.isEmpty(req.orderBy);
        long totalHits = 0;
        if (defaultOrder) {
            logger.info("getEntityQusAttemptInfos inside defaultOrder");
            totalHits = resultQueryQids.size();
            req.setStart(Math.min(Math.max(0, req.start), resultQueryQids.size()));
            req.size = Math.min(Math.max(0, req.size), (resultQueryQids.size() - req.start));
            if (req.size == 0) {
                req.size = resultQueryQids.size() - req.start;
            }
            logger.info("getEntityQusAttemptInfos default order start: " + req.start + ", size:" + req.size);
            resultQueryQids = resultQueryQids.subList(req.start, req.start + req.size);
            logger.info("getEntityQusAttemptInfos inside defaultOrder after subList");
            req.start = 0; // set it to zero so that MONGO will not skip the
            // results as we have already skiped
        }
        logger.debug("getEntityQusAttemptInfos qIds : " + resultQueryQids);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("parentEntity.id").is(req.entity.id);
        criteria.and(ConstantsGlobal.QID).in(resultQueryQids.toArray());
        logger.info("getEntityQusAttemptInfos before questionAnalytics");
        query.addCriteria(criteria);
        List<QuestionAnalytics> questionAnalytics = mongoTemplate.find(query, QuestionAnalytics.class);
        logger.info("getEntityQusAttemptInfos after questionAnalytics");
        logger.debug("getEntityQusAttemptInfos QuesttionAnalytics hits : " + questionAnalytics + " query : " + query);
        // collect the resulted qIds to fetch their details data
        List<String> finalQids = new ArrayList<String>();
        for (QuestionAnalytics qA : questionAnalytics) {
            finalQids.add(qA.qId);
        }
        if (CollectionUtils.isEmpty(finalQids)) {
            finalQids = resultQueryQids;
        }
        GetEntityQuestionAttemptInfoListRes res = new GetEntityQuestionAttemptInfoListRes();
        res.totalHits = defaultOrder ? totalHits : questionAnalytics.stream().count();

        logger.debug("getEntityQusAttemptInfos finalQIds : " + finalQids);
        logger.info("getEntityQusAttemptInfos before questionInfoMap");
        Map<String, QuestionSearchIndexDetails> questionInfoMap = questionComponent.getQuestionsMap(finalQids, true);
        logger.info("getEntityQusAttemptInfos after questionInfoMap");
        logger.info("getEntityQusAttemptInfos before getAnalyticsCount");
        long count = getAnalyticsCount(req.orgId, req.entity, AcademicDimensionType.OVERALL.name());
        logger.info("getEntityQusAttemptInfos after getAnalyticsCount");
        test.attempts = count;
        logger.info("getEntityQusAttemptInfos before adding qStatsInfo");
        for (QuestionAnalytics qA : questionAnalytics) {

            QuestionAttemptStatsInfoDetail qStatsInfo = new QuestionAttemptStatsInfoDetail(
                    qIdOrderNoMap.get(qA.qId), questionInfoMap.get(qA.qId), qA.measures);
//            Need to Fix Code.
            logger.info("getEntityQusAttemptInfos before getting correct list for question " + qA.qId);
            qStatsInfo.measures.correct = getUsersListWithOrgFilter(getAllUsersAnalytics(qA.qId, AnswerCorrectness.CORRECT, req.entity), req.orgId).size();
            logger.info("getEntityQusAttemptInfos after getting correct list for question " + qA.qId);
            logger.info("getEntityQusAttemptInfos before getting partial list for question " + qA.qId);
            qStatsInfo.measures.partial = getUsersListWithOrgFilter(getAllUsersAnalytics(qA.qId, AnswerCorrectness.PARTIAL, req.entity), req.orgId).size();
            logger.info("getEntityQusAttemptInfos after getting partial list for question " + qA.qId);
            logger.info("getEntityQusAttemptInfos before getting incorrect list for question " + qA.qId);
            qStatsInfo.measures.incorrect = getUsersListWithOrgFilter(getAllUsersAnalytics(qA.qId, AnswerCorrectness.INCORRECT, req.entity), req.orgId).size();
            logger.info("getEntityQusAttemptInfos after getting incorrect list for question " + qA.qId);
            qStatsInfo.measures.left = (test.attempts - (qStatsInfo.measures.correct + qStatsInfo.measures.incorrect + qStatsInfo.measures.partial));
            res.list.add(qStatsInfo);
        }
        logger.info("getEntityQusAttemptInfos after adding qStatsInfo");
        if (defaultOrder) {
            Collections.sort(res.list, new EntityQuestionAttemptInfoComparator());
            logger.info("getEntityQusAttemptInfos after sort");
        }
        return res;
    }

    private List<String> getQIdsSubList(String brdId, AbstractTestCommonModel test,
                                        List<String> qIds) {

        if (!StringUtils.isEmpty(brdId)) {
            Set<String> brdQids = new HashSet<String>(test.__getAllQIds(brdId));
            List<String> updatedQids = new ArrayList<String>();
            for (String qId : qIds) {
                if (brdQids.contains(qId)) {
                    updatedQids.add(qId);
                }
            }
            qIds = updatedQids;
        }
        return qIds;
    }

    public List<UserQuestionAnalytics> getAllUsersAnalytics(String qId, AnswerCorrectness isCorrect, SrcEntity parentEntity) {

        logger.debug("getAnalytics qId: " + qId);

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("isCorrect").is(isCorrect);
        criteria.and("qId").is(qId);
        criteria.and("parentEntity.type").is(parentEntity.type);
        criteria.and("parentEntity.id").is(parentEntity.id);
        query.addCriteria(criteria);

        List<UserQuestionAnalytics> userQuestionAnalytics = mongoTemplate.find(query, UserQuestionAnalytics.class);

        logger.info("getAnalytics userQuestionAnalytics: " + userQuestionAnalytics);

        return userQuestionAnalytics;
    }

    public List<UserQuestionAnalytics> getUsersListWithOrgFilter(List<UserQuestionAnalytics> users, String orgId) {
        List<UserQuestionAnalytics> newList = new ArrayList<UserQuestionAnalytics>();
        for (UserQuestionAnalytics user : users) {
            if (orgMemberRepo.findByOrgIdAndUserId(orgId, user.userId) == null) {

            } else {
                newList.add(user);
            }
        }
        return newList;
    }


    public StartAttemptRes startAttempt(StartAttemptReq startAttemptReq, boolean incAttemptCount) {
        // TODO Auto-generated method stub
        return startAttempt(startAttemptReq, incAttemptCount, 0, 0);
    }


    public ResetQuestionAttemptRes resetQuestionAttempt(ResetQuestionAttemptReq resetQuestionAttemptReq) {
        // TODO Auto-generated method stub
        return resetQuestionAttemptFromManager(resetQuestionAttemptReq);
    }


    public GetQuestionAnalyticsRes getQuestionAnalytics(GetQuestionAnalyticsReq getQuestionAnalyticsReq) {
        // TODO Auto-generated method stub
        return getQuestionAnalyticsFromManager(getQuestionAnalyticsReq);
    }

    public RecordAttemptRes recordAttempt(RecordAttemptReq recordAttemptReq) {
        // TODO Auto-generated method stub
        return recordAttempts(recordAttemptReq);
    }

    public List<UserQuestionAnalytics> getStudentAnalyticsList(GetQuestionAnalyticsReq req) {
        List<UserQuestionAnalytics> res = new ArrayList<UserQuestionAnalytics>();
        res = getUsersListWithOrgFilter(getAllUsersAnalytics(req.qId, req.isCorrect, req.parentEntity), req.orgId);
        return res;
    }

    public GetEntityMarkDistributionRes getEntityMarkDistribution(
            GetEntityMarkDistributionReq req) throws VedantuException {

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ENTITY_DOT_ID).is(req.entity.id);
        criteria.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(!StringUtils.isEmpty(req.brdId) ? req.brdId
                : AcademicDimensionType.OVERALL.name());
        query.addCriteria(criteria);
        Query fields = new Query();
        Criteria criteria1 = new Criteria();
        criteria1.and(ConstantsGlobal.SCORE).is(INCLUDE_FIELD);
        query.addCriteria(criteria1);
        EntityHighscore minEntityScore = (EntityHighscore) mongoTemplate.find(query, EntityHighscore.class);

        EntityHighscore maxEntityScore = (EntityHighscore) mongoTemplate.find(query, EntityHighscore.class);
        List<UserEntityAnalytics> usersList = getAnalyticsList(
                req.entity, !StringUtils.isEmpty(req.brdId) ? req.brdId
                        : AcademicDimensionType.OVERALL.name(), req.orgId);
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        for (UserEntityAnalytics user : usersList) {
            if (min > user.measures.score)
                min = user.measures.score;
            if (max < user.measures.score)
                max = user.measures.score;
        }
        double minScore = minEntityScore == null ? 0 : min;
        double maxScore = maxEntityScore == null ? 0 : max;
        double bucketRange = (maxScore - minScore) / (req.bucketCount < 1 ? 1 : req.bucketCount);
        if ((maxScore - minScore) % (req.bucketCount < 1 ? 1 : req.bucketCount) > 0) {
            bucketRange++;
        }
        bucketRange = Math.max(bucketRange, 1);

        criteria.and(ConstantsGlobal.SCORE).lte(maxScore);
        query.addCriteria(criteria);

        logger.info("maxScore: " + maxScore + ", minScore: " + minScore + ",bucketRange: "
                + bucketRange + ", entity: " + req.entity);

        int totalScore = 0;// will be used to calculate avgScore
        GetEntityMarkDistributionRes res = new GetEntityMarkDistributionRes();
        List<EntityHighscore> scores = mongoTemplate.find(query, EntityHighscore.class);

        Set<String> usersSet = new HashSet<String>();
        for (UserEntityAnalytics userList : usersList) {
            usersSet.add(userList.userId);
        }
        Iterator<EntityHighscore> highscores = scores.iterator();
        while (highscores.hasNext()) {
            EntityHighscore highscore = highscores.next();
            if (highscore.userIds == null || highscore.userIds.isEmpty()) {
                continue;
            }
            Iterator<String> users = highscore.userIds.iterator();
            while (users.hasNext()) {
                String user = users.next();
                if (!usersSet.contains(user)) {
                    users.remove();
                }
            }
            if (highscore.userIds.isEmpty())
                highscores.remove();
        }

        double currentBucketStart = minScore;
        // 1st create buckets then add the data there, and in the last remove
        // the empty bucket from
        // the end

        for (int i = 0; i < req.bucketCount; i++) {
            double start = currentBucketStart;
            double end = currentBucketStart + bucketRange;
            res.list.add(new MarkDistribution(start, end, 0));
            currentBucketStart = end;
        }

        int activeBucket = 0;
        for (EntityHighscore highscore : scores) {
            int count = highscore.userIds == null ? 0 : highscore.userIds.size();
            totalScore += highscore.score * count;
            res.totalHits += count;
            MarkDistribution markDist = res.list.get(activeBucket);
            if (highscore.score >= markDist.to) {
                activeBucket = getNextBucket(res.list, highscore.score, activeBucket);
                markDist = res.list.get(activeBucket);
            }
            markDist.count += count;
        }
        // now remove empty buckets from the end
        for (int i = res.list.size() - 1; i >= 0; i--) {
            if (res.list.size() > i && res.list.get(i).count < 1) {
                res.list.remove(i);
            } else {
                break;
            }
        }
        res.avgScore = totalScore / (res.totalHits < 1 ? 1 : res.totalHits);
        return res;
    }

    public List<UserEntityAnalytics> getAnalyticsList(SrcEntity entity, String acadDimId, String orgId) {
        List<UserEntityAnalytics> users = userEntityAnalyticsRepo.findByOrgIdAndEntityIdAndAcadDimId(orgId, entity.id, acadDimId);
        return users;
    }

    private int getNextBucket(List<MarkDistribution> list, double score, int currectBucket) {

        for (int i = currectBucket; i < list.size(); i++) {
            MarkDistribution markDist = list.get(i);
            if (score > markDist.from && score <= markDist.to) {
                return i;
            }
        }
        return currectBucket;
    }

    public GetEntityScheduleAnalyticsRes getEntityAnalyticsSchedule(GetEntityScheduleAnalyticsReq req) throws VedantuException {

        GetEntityScheduleAnalyticsRes res = new GetEntityScheduleAnalyticsRes();
        // get all the sections of this program
        Set<String> targetIds = !StringUtils.isEmpty(req.sectionId) ? new HashSet<String>(
                Arrays.asList(req.sectionId)) : getProgramSections(req.orgId,
                req.programId, StringUtils.isEmpty(req.centerId) ? null : Arrays.asList(req.centerId));
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("source.type").is(EntityType.MODULE);
        //   Query<LibraryContentLink> query = LibraryContentLinksDAO.INSTANCE.createQuery();
        criteria.and("target.id").in(targetIds);
        criteria.and("target.type").is(EntityType.SECTION);
        criteria.and(ConstantsGlobal.LINK_TYPE).is(UserActionType.ADDED);
        criteria.and(ConstantsGlobal.RECORD_STATE).is(VedantuRecordState.ACTIVE);
        logger.debug("module link fetcher query: " + query);
        query.addCriteria(criteria);
        List<LibraryContentLink> libraryContentLinks = mongoTemplate.find(query, LibraryContentLink.class);
        Iterator<LibraryContentLink> links = libraryContentLinks.iterator();

        Map<SrcEntity, Set<String>> moduleEntityToSectionIdsMap = new HashMap<SrcEntity, Set<String>>();

        while (links.hasNext()) {
            LibraryContentLink link = links.next();
            targetIds.add(link.source.id);
            if (moduleEntityToSectionIdsMap.get(link.source) == null) {
                moduleEntityToSectionIdsMap.put(link.source, new HashSet<String>());
            }
            moduleEntityToSectionIdsMap.get(link.source).add(link.target.id);
        }

        // 2. aggregate all the testId in library/targetIds, if query is there
        // than filter it
        // with query from es {returned ids.size()==totalHits}


        SearchListResponse<EntityAnalyticsBasicInfo> tests = getEntityInfos(null, req.entityType, EntityAnalyticsBasicInfo.class, null);
        res.totalHits = tests.totalHits;

        Map<String, EntityAnalyticsBasicInfo> entityMap = new HashMap<String, EntityAnalyticsBasicInfo>();

        for (EntityAnalyticsBasicInfo entity : tests.list) {
            entity.__fillBoardsTotalMarks();
            entityMap.put(entity.id, entity);
        }

        logger.debug("entityMap: " + entityMap);
        if (entityMap.isEmpty()) {
            logger.debug("no entity found for req : " + req);
            return res;
        }
        Map<String, EntityAnalytics> entityAnalyticsMap = getEntityAnalyticsMap(entityMap.keySet());
        Map<String, EntityTopper> entityTopperMap = getEntityToppers(req.orgId, entityMap.keySet(),
                AcademicDimensionType.OVERALL.name(), entityMap);

        List<LibraryContentLink> aggregationOutput = getEntityScheduleAggregationOutput(
                entityMap.keySet(), targetIds, req.start, req.size, true);
        logger.debug("aggregationOutput : " + aggregationOutput);
        if (aggregationOutput == null) {
            return res;
        }

        // in the aggregation result fetch mapping of module to set of
        // sectionIds

        for (LibraryContentLink d : aggregationOutput) {
            logger.debug("db object : " + d);
            String testId = d.getId().toString();
            ScheduleInfo scheduleInfo = ObjectMapperUtils.convertValue(
                    d.getSchedule(), ScheduleInfo.class);
            List<DBObject> targets = (List<DBObject>) d.getTarget();
            Set<String> sectnIds = new HashSet<String>();

            for (DBObject entity : targets) {
                SrcEntity srcEntity = new SrcEntity(EntityType.valueOfKey((String) entity
                        .get(ConstantsGlobal.TYPE)), (String) entity.get(ConstantsGlobal.ID));
                if (srcEntity.type == EntityType.SECTION) {
                    sectnIds.add(srcEntity.id);
                } else {
                    Set<String> sIds = moduleEntityToSectionIdsMap.get(srcEntity);
                    if (sIds != null) {
                        sectnIds.addAll(sIds);
                    }
                }
            }

            EntityAnalyticsBasicInfo entity = entityMap.get(testId);
            if (entity == null) {
                continue;
            }

            EntityAnalytics entityAnalytics = entityAnalyticsMap.get(__getEntityAnalyticsMapKey(
                    entity.id, AcademicDimensionType.OVERALL.name()));

            if (entityAnalytics != null) {
                entity.measures = entityAnalytics.measures;
                entity.totalAttempts = entity.attempts;
            }

            EntityAnalyticsScheduleInfo infos = new EntityAnalyticsScheduleInfo(scheduleInfo,
                    getProgramBySectionIds(sectnIds, true),
                    entityTopperMap.get(entity.id), entity);

            // add the boards analytics measures here
            if (infos.entity.boards != null) {
                for (BoardAnalyticsInfo boardInfo : infos.entity.boards) {
                    EntityAnalytics boardAnalytics = entityAnalyticsMap
                            .get(__getEntityAnalyticsMapKey(entity.id, boardInfo.id));
                    if (boardAnalytics != null) {
                        boardInfo.measures = boardAnalytics.measures;
                    }
                }
            }

            // sort data by only course
            // if(StringUtils.isNotEmpty(req.courseId) &&
            // StringUtils.isEmpty(req.topicId)){
            // if(infos.entity.boards != null){
            // for(BoardAnalyticsInfo boardInfo : infos.entity.boards){
            // if(req.courseId.equals(boardInfo.id)){
            // LOGGER.debug("infos object sorted according to course : " +
            // infos);
            // res.list.add(infos);
            // break;
            // }
            // }
            // }
            // }
            // // sort data by course and topic wise
            // else if(StringUtils.isNotEmpty(req.courseId) &&
            // StringUtils.isNotEmpty(req.topicId)){
            // if(infos.entity.boards != null) {
            // for (BoardAnalyticsInfo boardInfo : infos.entity.boards) {
            // if (req.topicId .equals(boardInfo.id)) {
            // LOGGER.debug("infos object sorted according to topics : " +
            // infos);
            // res.list.add(infos);
            // break;
            // }
            // }
            // }
            // }else{
            logger.debug("infos object : " + infos);
            res.list.add(infos);
            // }
        }
        entityMap.clear();
        return res;

    }

    public Set<String> getProgramSections(String orgId,
                                          String programId, List<String> centerIds) throws VedantuException {

        Set<String> sectionIds = new HashSet<String>();
        OrgProgram program = null;

        boolean collectAllSections = false;
        if (CollectionUtils.isEmpty(centerIds)) {
            // if content added for few centres
            collectAllSections = true;
        }
        program = orgProgramRepo.findByOrgIdAndId(orgId, programId);
        if (program == null) {
            return sectionIds;
        }

        if (CollectionUtils.isNotEmpty(program.centersSections)) {
            for (OrgProgramCenterSections centerSection : program.centersSections) {

                if (CollectionUtils.isEmpty(centerSection.sectionIds)) {
                    continue;
                }

                if (collectAllSections) {

                    sectionIds.addAll(centerSection.sectionIds);
                } else if (centerIds.contains(centerSection.centerId)) {
                    sectionIds.addAll(centerSection.sectionIds);

                }
            }
        }

        return sectionIds;
    }

    public ListResponse<EntityScheduleInfo> getEntityScheduleInfoRes(String orgId,
                                                                     String entityId) throws VedantuException {

        ListResponse<EntityScheduleInfo> res = new ListResponse<EntityScheduleInfo>();
        List<LibraryContentLink> libraryContentLink = getEntityScheduleAggregationOutput(
                Arrays.asList(entityId), null, NO_START, NO_LIMIT, false);
        logger.debug("aggregationOutput : " + libraryContentLink);
        List<EntityScheduleInfo> schedules = new ArrayList<EntityScheduleInfo>();
        for (LibraryContentLink d : libraryContentLink) {
            String eId = d.getId().toString();
            logger.debug("entityId: " + eId);
            List<LibraryContentLink> scheduleAggObjs = (List<LibraryContentLink>) d.getSchedule();
            schedules = populateScheduleInfos(scheduleAggObjs);
            break;
        }
        res.totalHits = schedules.size();
        res.list = schedules;
        return res;
    }

    public List<LibraryContentLink> getEntityScheduleAggregationOutput(
            Collection<String> sourceIds, Collection<String> targetIds, int start, int size,
            boolean latestScheduleOnly) {

        if (sourceIds == null && targetIds == null) {
            return null;
        }
        Query match = new Query();
        Criteria criteria = new Criteria();
        if (!CollectionUtils.isEmpty(targetIds))
            criteria.and("target.id").in(targetIds.toArray());
        criteria.and(ConstantsGlobal.RECORD_STATE).is(VedantuRecordState.ACTIVE.name());
        if (CollectionUtils.isNotEmpty(sourceIds)) {
            criteria.and("source.id").in(sourceIds.toArray());
        }
        //  match.addCriteria(criteria);

        Query sort = new Query();
        Criteria criteria1 = new Criteria();
        criteria.and("schedule.startTime").is(SortOrder.ASC);
        //  sort.addCriteria(criteria1);
        Query id = new Query();
        Criteria criteria2 = new Criteria();
        // criteria.and("entityId").is("$source.id");

        Query sId = new Query();
        Criteria criteria3 = new Criteria();
        // criteria.and("month").is(new BasicDBObject("$month", "$schedule.startTime"));
        //criteria.and("day").is(new BasicDBObject("$dayOfMonth", "$schedule.startTime"));
        //criteria.and("year").is(new BasicDBObject("$year", "$schedule.startTime"));
        //sId.addCriteria(criteria3);
        //criteria.and("startTime").is(sId);
        id.addCriteria(criteria2);
        Query group1 = new Query();
        Criteria criteria4 = new Criteria();
        //  criteria.and("_id").is(id);
        // criteria.and("schedule").is(new BasicDBObject("$last", "$schedule"));
        //     criteria.and("targets").is(new BasicDBObject("$push", "$target"));
        group1.addCriteria(criteria4);
        Query group2 = new Query();
        Criteria criteria5 = new Criteria();
        // criteria.andOperator("_id").is("$_id.entityId");
        // criteria.and("schedule").is(new BasicDBObject("$first", "$schedule"));

        if (latestScheduleOnly) {
            // criteria.and("targets").is(new BasicDBObject("$first", "$targets"));
        } else {
            Query scheduleAgg = new Query();
            Criteria criteria6 = new Criteria();
            // criteria.and("schedule").is("$schedule");
            //criteria.and("targets").is("$targets");
            //  scheduleAgg.addCriteria(criteria6);
            // criteria.and("schedules").is(new BasicDBObject("$push", scheduleAgg));
        }

        Query matchQuery = new Query();
        Criteria criteria7 = new Criteria();
        //criteria7.and("$match").is(match);
        //  matchQuery.addCriteria(criteria7);
        List<Criteria> additionalOps = new ArrayList<Criteria>();
        Query sortQuery = new Query();
        Criteria criteria8 = new Criteria();
        //  criteria8.and("$sort").is(sort);
        // matchQuery.addCriteria(criteria);

        Criteria criteria9 = new Criteria();
        //criteria.and("schedule.startTime").is(SortOrder.DESC);

        matchQuery.addCriteria(criteria);

        List<LibraryContentLink> libraryContentLink = mongoTemplate.find(matchQuery, LibraryContentLink.class);
        return libraryContentLink;
    }

    private List<EntityScheduleInfo> populateScheduleInfos(List<LibraryContentLink> scheduleAggObjs) {

        List<EntityScheduleInfo> schedules = new ArrayList<EntityScheduleInfo>();

        for (LibraryContentLink scheduleInfo : scheduleAggObjs) {
            ScheduleInfo scheduleInfo1 = ObjectMapperUtils.convertValue(
                    scheduleInfo.getSchedule(), ScheduleInfo.class);

            List<SrcEntity> targets = (List<SrcEntity>) scheduleInfo.getTarget();
            Set<String> sectnIds = new HashSet<String>();
            for (SrcEntity entity : targets) {
                SrcEntity srcEntity = new SrcEntity(entity.getType(), entity.getId());
                if (EntityType.SECTION.equals(srcEntity.type)) {
                    sectnIds.add(srcEntity.id);
                } else if (EntityType.MODULE.equals(srcEntity.type)) {
                    List<LibraryContentLink> links = libraryContentLinksRepo.findByLinkTypeAndRecordStateAndSourceIdAndTargetType(
                            UserActionType.ADDED, VedantuRecordState.ACTIVE, srcEntity.id, EntityType.SECTION);
                    for (LibraryContentLink link : links) {
                        sectnIds.add(link.target.id);
                    }
                }
            }

            if (sectnIds.isEmpty()) {
                continue;
            }

            EntityScheduleInfo entityScheduleInfo = new EntityScheduleInfo();
            entityScheduleInfo.schedule = scheduleInfo1;
            entityScheduleInfo.programs = getProgramBySectionIds(sectnIds, true);
            schedules.add(entityScheduleInfo);
        }
        return schedules;
    }

    public List<OrgProgramBasicInfo> getProgramBySectionIds(
            Collection<String> sectionIds, boolean addSectionInfos) {

        List<OrgProgramBasicInfo> orgPrograms = new ArrayList<OrgProgramBasicInfo>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("centersSections.sectionIds").in(sectionIds.toArray());
        query.addCriteria(criteria);
        List<OrgProgram> results = mongoTemplate.find(query, OrgProgram.class);

        for (OrgProgram orgProgram : results) {
            OrgProgramBasicInfo info = getOrgProgramBasicInfo(orgProgram,
                    false, addSectionInfos, sectionIds);
            orgPrograms.add(info);
        }
        return orgPrograms;
    }

    public OrgProgramBasicInfo getOrgProgramBasicInfo(
            OrgProgram orgProgram, boolean includeAllCenters,
            boolean addSectionInfo, Collection<String> forSectionIds) {

        OrgProgramBasicInfo programInfo = (OrgProgramBasicInfo) orgProgram
                .toBasicInfo();
        if (!addSectionInfo) {
            return programInfo;
        }
        if (forSectionIds == null) {
            forSectionIds = new HashSet<String>();
        }

        Set<String> centerIds = new HashSet<String>(orgProgram._getCenterIds(
                forSectionIds, includeAllCenters));
        Map<String, OrgStructureBasicInfo> centerMap = getCenterBasicInfosByIds(centerIds);

        Map<String, OrgStructureBasicInfo> sectionMap = getSectionBasicInfosByIds(forSectionIds, true);
        for (OrgProgramCenterSections center : orgProgram.centersSections) {
            OrgStructureBasicInfo orgCenter = centerMap.get(center.centerId);
            if (orgCenter == null) {
                continue;
            }

            OrgProgramCenterBasicInfo centerInfo = programInfo
                    ._getOrAddProgramCenter(orgCenter);
            for (String sectionId : center.sectionIds) {
                OrgStructureBasicInfo orgSection = sectionMap.get(sectionId);
                if (orgSection == null) {
                    continue;
                }
                centerInfo._getOrAddProgramSection(orgSection);
            }
        }
        return programInfo;
    }

    public Map<String, OrgStructureBasicInfo> getCenterBasicInfosByIds(Set<String> ids) {
        List<OrgCenter> results = orgCenterRepo.findAllByIdIn(ids);
        Map<String, OrgStructureBasicInfo> basicInfoMap = toBasicInfosMap(results);
        return basicInfoMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMap(Collection<OrgCenter> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgCenter t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (B) t.toBasicInfo());
            }
        }
        return infosMap;

    }

    public Map<String, OrgStructureBasicInfo> getSectionBasicInfosByIds(Collection<String> ids,
                                                                        boolean addSectionDetailInfo) {

        List<OrgSection> results = orgSectionRepo.findByIdIn(ids);
        //   getByIds(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids)));
        Map<String, OrgStructureBasicInfo> basicInfoMap = toBasicSectionInfosMap(results);
        if (addSectionDetailInfo) {
            addSectionDetailInfo(results, basicInfoMap);
        }
        return basicInfoMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicSectionInfosMap(Collection<OrgSection> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (OrgSection t : results) {
                if (null == t) {
                    continue;
                }
                infosMap.put(t._getStringId(), (B) t.toBasicInfo());
            }
        }
        return infosMap;

    }

    public void addSectionDetailInfo(List<OrgSection> sections,
                                     Map<String, OrgStructureBasicInfo> basicInfoMap) {

        for (OrgSection orgSection : sections) {
            OrgProgramSectionBasicInfo sectionBasicInfo = (OrgProgramSectionBasicInfo) basicInfoMap
                    .get(orgSection._getStringId());
            logger.debug("OrgProgramSectionBasicInfo : " + sectionBasicInfo);
            if (sectionBasicInfo == null) {
                continue;
            }
            sectionBasicInfo.addSectionExtraInfo(orgSection);
        }
    }

    private Map<String, EntityTopper> getEntityToppers(String orgId,
                                                       Collection<String> entityIds, String acadDimId,
                                                       Map<String, EntityAnalyticsBasicInfo> entityAnalyticContentInfo) {

        Map<String, EntityTopper> topperMap = new HashMap<String, EntityTopper>();

        Query qery = new Query();
        Criteria cri = new Criteria();
        cri.and("entity.id").is(entityIds.toArray());

        cri.and(ConstantsGlobal.ACAD_DIM_DOT_ID).is(acadDimId);
        cri.and(ConstantsGlobal.ORG_ID).is(orgId);

        cri.and("_id.score").is(SortOrder.DESC.name());
        cri.and("_id.timeTaken").is(SortOrder.ASC.name());

        List<UserEntityAnalytics> results = mongoTemplate.find(qery.addCriteria(cri), UserEntityAnalytics.class);

        Set<String> userIds = new HashSet<String>();
        for (UserEntityAnalytics d : results) {
            String topperId = d.getUserId();
            if (!StringUtils.isEmpty(topperId)) {
                userIds.add(topperId);
            }
        }
        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds);
        for (UserEntityAnalytics d : results) {
            SrcEntity entity = d.getEntity();
            String entityId = entity.getId();
//          The score obtained may be int or double,so casting it to string;
           /* String scoreObj =  d.get(d.get(ConstantsGlobal.SCORE) == null ? "0" :  d
                    .get(ConstantsGlobal.SCORE).toString());
            int score = (int) Double.parseDouble(scoreObj);*/
            float percentage = (float) d.getPercentage();
                  /*  entityAnalyticContentInfo.get(entityId) != null
                    && entityAnalyticContentInfo.get(entityId).totalMarks != 0 ? (score * 100)
                    / entityAnalyticContentInfo.get(entityId).totalMarks : 0;*/
            String topperId = d.getUserId();
            if (!StringUtils.isEmpty(topperId)) {
                EntityTopper topper = new EntityTopper((UserInfo) userInfoMap.get(topperId),
                        percentage);
                topperMap.put(entityId, topper);
            }
            // we will take the 1st topper from this list, as we are only
            // showing one topper
        }
        return topperMap;
    }

}

package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.AnalyticsComponent;
import com.lms.enums.AcademicDimensionType;
import com.lms.managers.AbstractTestManager;
import com.lms.models.Test;
import com.lms.models.analytics.EntityAnalytics;
import com.lms.models.analytics.UserEntityAnalytics;
import com.lms.models.analytics.UserQuestionAttempt;
import com.lms.pojos.requests.GetTestsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.requests.tests.GetTestInfoReq;
import com.lms.pojos.responce.GetTestInfoRes;
import com.lms.pojos.responce.GetTestRes;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.tests.GetTestQuestionsRes;
import com.lms.pojos.search.details.TestSearchIndexDetails;
import com.lms.repository.TestRepo;
import com.lms.services.TestsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class TestServiceImpl extends AbstractTestManager implements TestsService {

    private final static Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    private TestRepo testRepo;
    @Autowired
    private AnalyticsComponent analyticsComponent;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public VedantuResponse getTestInfo(GetTestInfoReq getTestInfoReq) {
        Test test = testRepo.findById(getTestInfoReq.id).get();
        if (test == null) {
            logger.error("test[" + getTestInfoReq.id + "] not found");
            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND);
        }
        if (test.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetTestInfoRes getTestRes = new GetTestInfoRes();
        getTestRes.fromMongoModel(test);
        if (!StringUtils.isEmpty(test.pdfId)) {
//            Document document = DocumentDAO.INSTANCE.getById(test.pdfId);
//            ObjectId docId  = document.id;
//            String pdfIdforTest = docId.toString();
            getTestRes.pdfId = test.pdfId;
        }
        if (!StringUtils.isEmpty(test.password)) {
            getTestRes.password = test.password;
        }
        getTestRes.enablePartialMarks = test.enablePartialMarks;
        if (!StringUtils.isEmpty(test.resultPassword)) {
            getTestRes.resultPassword = test.resultPassword;
        }
        addTestStatsInfo(getTestRes, getTestInfoReq.orgId);
        getTestRes = (GetTestInfoRes) annotateExtraInfo(
                getTestInfoReq.userId,
                getTestRes.contentSrc != null
                        && getTestRes.contentSrc.type == EntityType.ORGANIZATION ? getTestRes.contentSrc.id
                        : null, EntityType.TEST, getTestRes);
        return new VedantuResponse(getTestRes);
    }

    private void addTestStatsInfo(TestSearchIndexDetails details, String orgId) {

        EntityAnalytics testAnalytics = analyticsComponent.getEntityAnalytics(
                details.__getSrcEntity(), AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        AtomicLong hits = new AtomicLong();
        List<UserEntityAnalytics> userEntityAnalyticsList = analyticsComponent.getUserEntityAnalyticsList(details.__getSrcEntity(),
                AcademicDimensionType.OVERALL.name(), orgId, hits);
        long count = hits.longValue();
        int totalScore = 0;
        long totalTimeTaken = 0;
        for (UserEntityAnalytics user : userEntityAnalyticsList) {
            totalScore += user.measures.score;
            totalTimeTaken += user.measures.timeTaken;
        }
        logger.debug("entityAnalytics count " + count);
        logger.debug("entityAnalytics score " + totalScore);
        logger.debug("entityAnalytics timeTaken " + totalTimeTaken);
//        long count = UserEntityAnalyticsDAO.INSTANCE.getAnalyticsCount(details.__getSrcEntity(),
//                AcademicDimensionType.OVERALL.name(),orgId);
//        int totalScore = UserEntityAnalyticsDAO.INSTANCE.getTotalScore(details.__getSrcEntity(),
//                AcademicDimensionType.OVERALL.name(),orgId);
//        long totalTimeTaken = UserEntityAnalyticsDAO.INSTANCE.getTotalTimeTaken(details.__getSrcEntity(),
//                AcademicDimensionType.OVERALL.name(),orgId);
//
//        LOGGER.debug("Already calculated"+count);
//        LOGGER.debug("Already calculated"+totalScore);
//        LOGGER.debug("Already calculated"+totalTimeTaken);
        details.attempts = count;
        if (testAnalytics != null) {
            testAnalytics.measures.score = totalScore;
            testAnalytics.measures.timeTaken = totalTimeTaken;
            // Here score is total score of students for this test
            // count is total number of students
            details.avgMarks = count != 0 ? testAnalytics.measures.score / count : 0;
            if (details instanceof GetTestInfoRes) {
                ((GetTestInfoRes) details).avgTimeTaken = count != 0 ? testAnalytics.measures.timeTaken
                        / count : 0;
            }
        }

    }

    @Override
    public VedantuResponse getTests(GetTestsReq req) {
        SearchListResponse<GetTestRes> results = getEntityInfos(req, EntityType.TEST,
                GetTestRes.class, null);
        analyticsComponent.annotateExtraInfo(req.sectionId, req.userId, req.orgId, EntityType.TEST, results.list);
        return new VedantuResponse(results);
    }

    public SearchListResponse<GetTestRes> getTestsForContentResponce(GetTestsReq req) {
        SearchListResponse<GetTestRes> results = getEntityInfos(req, EntityType.TEST,
                GetTestRes.class, null);
        analyticsComponent.annotateExtraInfo(req.sectionId, req.userId, req.orgId, EntityType.TEST, results.list);
        return results;
    }

    @Override
    public VedantuResponse getTestQuestions(GetTestDetailsReq req) {
        // TODO: see if start attempt request can be fired from here
        Test test = getTest(req.getId());
        GetTestQuestionsRes res = new GetTestQuestionsRes(test._getStringId(), test.recordState,
                test.name, test.duration, test.type, test.code);
        res.totalTestTime = test.duration;
        if (test.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        if (test.qusCount != test.__getAllQIds().size()) {
            logger.error("details of an incomplete test[" + req.id + "] can not be fetched");
            throw new VedantuException(VedantuErrorCode.INCOMPLETE_TEST);
        }

        if (req.testState != null) {
            if (req.testState.equals("RESUMED")) {
                // Get Test remaining time duration
                long timeTaken = getTimeTaken(req.attemptId);
                res.duration = test.duration - timeTaken;
            }
        }
        if (test.simplifiedBoardNames != null && !test.simplifiedBoardNames.isEmpty()) {
            res.boards = getSimplifiedTestBoardWiseQuestions(test, req);
        } else {
            res.boards = getTestBoardWiseQuestions(test, req);
        }
        res.totalMarks = test.totalMarks;
        res.enableSectionLocking = test.enableSectionLocking;
        res.oneOrMoreMarksQTypes = test.oneOrMoreMarksQTypes;
        res.enablePartialMarks = test.enablePartialMarks;
        res.partialMarksQTypes = test.partialMarksQTypes;
        return new VedantuResponse(res);
    }

    public Test getTest(String id) throws VedantuException {

        Test test = testRepo.findById(id).get();
        if (test == null) {
            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND, "no test found with id: "
                    + id);
        }
        return test;
    }
    public long getTimeTaken(String attemptId) {
        // TODO Auto-generated method stub
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.ATTEMPT_ID).is(attemptId);
        query.addCriteria(criteria);
        long timeTaken = 0;
        List<UserQuestionAttempt> userQuestionAttemptList = mongoTemplate.find(query, UserQuestionAttempt.class);
        for (UserQuestionAttempt userQuestionAttempt : userQuestionAttemptList) {
            timeTaken += userQuestionAttempt.timeTaken;
        }
        return timeTaken;
    }
}

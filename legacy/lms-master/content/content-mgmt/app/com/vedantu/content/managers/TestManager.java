package com.vedantu.content.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.analytics.EntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAnalyticsDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.daos.analytics.UserQuestionAttemptDAO;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.analytics.AcademicDimensionType;
import com.vedantu.content.models.analytics.EntityAnalytics;
import com.vedantu.content.models.analytics.UserEntityAnalytics;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetSubjectiveQuestionUserAttemptsReq;
import com.vedantu.content.pojos.requests.tests.UpdateMarksStatusReq;
import com.vedantu.content.pojos.responses.tests.UpdateMarksStatusRes;
import com.vedantu.content.pojos.requests.tests.GetTestDetailsReq;
import com.vedantu.content.pojos.requests.tests.GetTestInfoReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;
import com.vedantu.content.pojos.responses.tests.GetSubjectiveQuestionUserAttemptsRes;
import com.vedantu.content.pojos.responses.tests.GetTestInfoRes;
import com.vedantu.content.pojos.responses.tests.GetTestQuestionsRes;
import com.vedantu.content.pojos.responses.tests.GetTestRes;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.search.details.TestSearchIndexDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class TestManager extends AbstractTestManager {

    private static final ALogger LOGGER   = Logger.of(TestManager.class);
    public static TestManager    INSTANCE = new TestManager();

    public static GetTestInfoRes getTestInfo(GetTestInfoReq getTestReq) throws VedantuException {

        Test test = TestDAO.INSTANCE.getById(getTestReq.id);
        if (test == null) {
            LOGGER.error("test[" + getTestReq.id + "] not found");
            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND);
        }
        if (test.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        GetTestInfoRes getTestRes = new GetTestInfoRes();
        getTestRes.fromMongoModel(test);
        if(!StringUtils.isEmpty(test.pdfId)){
//            Document document = DocumentDAO.INSTANCE.getById(test.pdfId);
//            ObjectId docId  = document.id;
//            String pdfIdforTest = docId.toString();
            getTestRes.pdfId = test.pdfId;
        }
        if(!StringUtils.isEmpty(test.password)){
            getTestRes.password = test.password;
        }
        getTestRes.enablePartialMarks = test.enablePartialMarks;
        getTestRes.subjectiveTest = test.subjectiveTest;
        getTestRes.isNTAPattern=test.isNTAPattern;
        if(!StringUtils.isEmpty(test.resultPassword)){
            getTestRes.resultPassword = test.resultPassword;
        }
        addTestStatsInfo(getTestRes,getTestReq.orgId);
        getTestRes = (GetTestInfoRes) annotateExtraInfo(
                getTestReq.userId,
                getTestRes.contentSrc != null
                        && getTestRes.contentSrc.type == EntityType.ORGANIZATION ? getTestRes.contentSrc.id
                        : null, EntityType.TEST, getTestRes);
        return getTestRes;
    }

    public static GetTestQuestionsRes getTestQuestions(GetTestDetailsReq req)
            throws VedantuException {

        // TODO: see if start attempt request can be fired from here
        Test test = TestDAO.INSTANCE.getTest(req.id);
        GetTestQuestionsRes res = new GetTestQuestionsRes(test._getStringId(), test.recordState,
                test.name, test.duration, test.type, test.code);
        res.totalTestTime = test.duration;
        if (test.recordState == VedantuRecordState.TEMPORARY) {
            throw new VedantuException(VedantuErrorCode.CONTENT_IN_TEMPORARY_STAGE);
        }
        
        if (test.isNTAPattern) {
        	if (test.qusCount != test.__getAllQIds().size()
                    && (test.actualQusCount != test.__getAllQIds().size())) {
                LOGGER.error("details of an incomplete test[" + req.id + "] can not be fetched");
                throw new VedantuException(VedantuErrorCode.INCOMPLETE_TEST);
            }
        }
        else{
        	if (test.qusCount != test.__getAllQIds().size()) {
                LOGGER.error("details of an incomplete test[" + req.id + "] can not be fetched");
                throw new VedantuException(VedantuErrorCode.INCOMPLETE_TEST);
            }
        }
        
        if(req.testState != null){
            if(req.testState.equals("RESUMED")){
            // Get Test remaining time duration
                long timeTaken = UserQuestionAttemptDAO.INSTANCE.getTimeTaken(req.attemptId);
                res.duration = test.duration - timeTaken;
            }
        }
        if(test.simplifiedBoardNames != null && !test.simplifiedBoardNames.isEmpty()){
            res.boards = getSimplifiedTestBoardWiseQuestions(test,req);
            res.hassSimplifiedBoards=true;
        }else{
            res.boards = getTestBoardWiseQuestions(test, req);
        }
        res.totalMarks = test.totalMarks;
        res.enableSectionLocking = test.enableSectionLocking;
        res.oneOrMoreMarksQTypes = test.oneOrMoreMarksQTypes;
        res.subjectiveTest = test.subjectiveTest;
        res.enablePartialMarks = test.enablePartialMarks;
        res.partialMarksQTypes = test.partialMarksQTypes;
        res.isNTAPattern=test.isNTAPattern;
        return res;
    }

	public static GetTestQuestionsRes getTestSubjectiveQuestions(
			GetTestDetailsReq req) throws VedantuException {
		Test test = TestDAO.INSTANCE.getTest(req.id);
		GetTestQuestionsRes res = new GetTestQuestionsRes(test._getStringId(),
				test.recordState, test.name, test.duration, test.type,
				test.code);
		res.subjectiveTest = test.subjectiveTest;
		SrcEntity parent = new SrcEntity(EntityType.TEST,req.id);
		res.boards = getTestSubjectiveBoardQuestions(test,parent,true);
		return res;
	}

	public static GetSubjectiveQuestionUserAttemptsRes getSubjectiveQuestionUserAttempts(
			GetSubjectiveQuestionUserAttemptsReq req) throws VedantuException {
		Test test = TestDAO.INSTANCE.getTest(req.testId);
		GetSubjectiveQuestionUserAttemptsRes res = new GetSubjectiveQuestionUserAttemptsRes();
		MutableLong totalHits = new MutableLong();
		SrcEntity parent = new SrcEntity(EntityType.TEST,req.testId);
		if (test.subjectiveTest) {
			if (req.loadQuestionInfo) {
				res.boards = getTestSubjectiveBoardQuestions(test,parent,false);
			}
			res.subjectiveQuestionId = req.id;
		}
		res.userQuestionAttempts = getTestSubjectiveQuestionUserAttempts(req, totalHits, parent);
		res.totalhits = totalHits.longValue();
		return res;
	}

    public static SearchListResponse<GetTestRes> getTests(GetTestsReq req) throws VedantuException {

        SearchListResponse<GetTestRes> results = getEntityInfos(req, EntityType.TEST,
                GetTestRes.class, null);
        annotateExtraInfo(req.sectionId, req.userId, req.orgId, EntityType.TEST, results.list);
        return results;
    }

    private static void addTestStatsInfo(TestSearchIndexDetails details, String orgId) {

        EntityAnalytics testAnalytics = EntityAnalyticsDAO.INSTANCE.getEntityAnalytics(
                details.__getSrcEntity(), AcademicDimensionType.OVERALL,
                AcademicDimensionType.OVERALL.name());
        MutableLong hits = new MutableLong();
        List<UserEntityAnalytics> userEntityAnalyticsList = UserEntityAnalyticsDAO.INSTANCE.getUserEntityAnalyticsList(details.__getSrcEntity(),
                AcademicDimensionType.OVERALL.name(),orgId,hits);
        long count = hits.longValue();
        int totalScore = 0;
        long totalTimeTaken = 0;
        for(UserEntityAnalytics user:userEntityAnalyticsList){
            totalScore += user.measures.score;
            totalTimeTaken += user.measures.timeTaken;
        }
        LOGGER.debug("entityAnalytics count "+count);
        LOGGER.debug("entityAnalytics score "+totalScore);
        LOGGER.debug("entityAnalytics timeTaken "+totalTimeTaken);
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
        if(testAnalytics != null){
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
    public boolean update(EditContentReq request) throws VedantuException {

        Test content = TestDAO.INSTANCE.getById(request.entity.id);

        List<String> updateList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(request.name)
                && request.updateList.contains(EditContentReq.NAME)) {
            content.name = request.name;
            updateList.add(Test.NAME);
        }

        if (request.updateList.contains(EditContentReq.DESCRIPTION)) {
            content.desc = request.description;
            updateList.add(Test.DESC);

        }
        updateContent(request.userId, content, updateList);

        return true;
    }

    public TestResultVisibility updateTestResultVisibility(String userId, SrcEntity entity,
            TestResultVisibility resultVisibility, String resultVisibilityMessage)
            throws VedantuException {

        Test test = TestDAO.INSTANCE.getTest(entity.id);
        List<String> updateList = new ArrayList<String>();
        test.resultVisibility = resultVisibility;
        test.resultVisibilityMessage = resultVisibilityMessage;
        updateList.add(ConstantsGlobal.RESULT_VISIBILITY);
        updateList.add(ConstantsGlobal.RESULT_VISIBILITY_MESSAGE);
        updateContent(userId, test, updateList);
        return test.resultVisibility;
    }

    private void updateContent(String userId, Test content, List<String> updateList)
            throws VedantuException {

        TestDAO.INSTANCE.updateModel(content, updateList);
        SrcEntity entity = new SrcEntity(EntityType.TEST, content._getStringId());
        generateEventAysc(userId, content, EventActionType.UPDATE, EventType.INDEX_TEST,
                UserActionType.UPDATED, false);
        // added by Shankar --> update the corresponding library links
        LibraryContentLinksDAO.INSTANCE.updateLastUpdated(entity);
    }

    @Override
    public boolean calculate(String id, boolean recalculate,VedantuBaseMongoModel... contents) throws VedantuException {

        List<Test> tests = new ArrayList<Test>();

        if (StringUtils.isNotEmpty(id)) {
            Test question = TestDAO.INSTANCE.getById(id);

            if (question == null) {
                return false;
            }
            tests.add(question);
        }

        if (contents != null && contents.length != 0) {
            for (VedantuBaseMongoModel content : contents) {
                if (content instanceof Test) {
                    tests.add((Test) content);
                }
            }
        }

        // calculate question image size;

        for (Test test : tests) {
            if( test.size.isFinalized() && !recalculate){
                continue;
            }
            test.size.reset();
            LOGGER.debug(" question ids" + test.__getAllQIds());
            List<Question> questions = QuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(test
                    .__getAllQIds()));
            if (CollectionUtils.isEmpty(questions)) {
                continue;
            }
            QuestionManager questionManager = new QuestionManager();
            for (com.vedantu.content.models.Question question : questions) {
                if (!question.size.isFinalized()) {
                    questionManager.calculate(null,true, question);
                }
                test.size.add(question.size);
            }
            test.size.finalize();
            TestDAO.INSTANCE.updateModel(test, Arrays.asList(Test.SIZE));

        }
        return true;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException, EntityFileStorageException {

        List<DownloadableFileInfo> fileInfos = new ArrayList<DownloadableFileInfo>();
        Test test = TestDAO.INSTANCE.getById(entityId);
        List<String> qids = test.__getAllQIds();

        QuestionManager questionManager = new QuestionManager();
        if (CollectionUtils.isNotEmpty(qids)) {
            for (String qId : qids) {
                List<DownloadableFileInfo> questionFiles = questionManager.getFiles(
                        EntityType.QUESTION, qId);
                if (CollectionUtils.isNotEmpty(questionFiles)) {
                    fileInfos.addAll(questionFiles);
                }
            }
        }
        return fileInfos;
    }

	public static UpdateMarksStatusRes updateMarksStatus(
			UpdateMarksStatusReq updateMarksStatusReq) throws VedantuException {
		// TODO Auto-generated method stub
		UpdateMarksStatusRes updateMarksStatusRes = new UpdateMarksStatusRes();
		Test test = TestDAO.INSTANCE.getById(updateMarksStatusReq.testId);
		if (test == null) {
			LOGGER.error("test[" + updateMarksStatusReq.testId + "] not found");
			throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND);
		}
		List<TestMetadata> listOfMetadata = test.metadata;
		for (TestMetadata metadata : listOfMetadata) {
			Map<String, Marks> marks = metadata.marks;
			for (Map.Entry<String, Marks> entry : marks.entrySet()) {
				String key = entry.getKey();
				Marks mark = entry.getValue();
				if (key.equals(updateMarksStatusReq.questionId)) {
					updateMarksStatusRes.success = true;
					mark.status = updateMarksStatusReq.status;
					// updateMarksStatusRes.map.put(key,mark);

				}
			}
		}
		 if(updateMarksStatusRes.success==false){
		 LOGGER.info("question["+updateMarksStatusReq.questionId +
		 "] not found");
		 throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
		 }
		TestDAO.INSTANCE.save(test);
		return updateMarksStatusRes;
	}
}

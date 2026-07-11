package com.lms.managers;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.components.QuestionComponent;
import com.lms.enums.QuestionType;
import com.lms.models.*;
import com.lms.models.analytics.UserQuestionAttempt;
import com.lms.models.tests.AbstractTestCommonModel;
import com.lms.pojos.TestDetails;
import com.lms.pojos.TestMetadata;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.responce.tests.TestBoardWiseQuestions;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import com.lms.pojos.tests.Marks;
import com.lms.pojos.tests.SimplifiedBoardNames;
import com.lms.pojos.tests.TestQTypeQuestions;
import com.lms.repository.CMDSQuestionRepo;
import com.lms.repository.QuestionRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class AbstractTestManager extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestManager.class);
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;

    public static Map<String, Integer> countOfParagraphQuestionForEachParagraph(
            Map<String, List<String>> paragraphMap) {
        // TODO Auto-generated method stub
        Map<String, Integer> count = new HashMap<String, Integer>();
        for (String paragraphId : paragraphMap.keySet()) {
            if (paragraphMap.get(paragraphId) != null && !paragraphMap.get(paragraphId).isEmpty()) {
                List<String> qIds = paragraphMap.get(paragraphId);
                for (String qId : qIds) {
                    count.put(qId, qIds.size());
                }
            }
        }
        return count;
    }

    public static List<String> getOrderedQuestionIds(Map<String, List<String>> paragraphMap) {
        // TODO Auto-generated method stub
        List<String> qIds = new ArrayList<String>();
        for (String paragraphId : paragraphMap.keySet()) {
            if (paragraphMap.get(paragraphId) != null && !paragraphMap.get(paragraphId).isEmpty()) {
                qIds.addAll(paragraphMap.get(paragraphId));
            }
        }
        return qIds;
    }

    public String getParaContent(String qid) {
        String paraId = "";
        String paraContext = "";
        try {
            Optional<Question> question1 = questionRepo.findById(qid);
            if (!question1.isPresent()) {
                throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND,
                        "no question found with id:" + qid);
            }
            Question question = question1.get();
            paraId = question.paragraphId;
        } catch (VedantuException e) {
            logger.error("Error while fetching para for question " + qid, e);
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("id").is(paraId);
        query.addCriteria(criteria);
        Optional<CMDSQuestion> cmdsQuestion = cmdsQuestionRepo.findById(paraId);

        paraContext = cmdsQuestion.get() + "";

        return paraContext;

    }

    public List<TestBoardWiseQuestions> getTestBoardWiseQuestions(
            AbstractTestCommonModel test, GetTestDetailsReq req) throws VedantuException {

        Map<String, QuestionSearchIndexDetails> questionMap = questionComponent.getQuestionsMap(
                test.__getAllQIds(), false);
        List<TestBoardWiseQuestions> boardWiseQuestionsList = new ArrayList<TestBoardWiseQuestions>();
        for (TestMetadata mdata : test.metadata) {
            TestBoardWiseQuestions boardWiseQuestions = new TestBoardWiseQuestions(mdata.name,
                    mdata.id);
            if (!req.qTypeDistribution) {
                if (CollectionUtils.isNotEmpty(mdata.qIds)) {
                    for (String qid : mdata.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if (qSearch != null) {
                            boardWiseQuestions.addITestQuestion(qSearch);
                        }
                    }
                }
            } else {
                addTestQTypeQuestionsDistritution(mdata, questionMap, boardWiseQuestions, req);
            }
            boardWiseQuestionsList.add(boardWiseQuestions);
        }
        return boardWiseQuestionsList;
    }

    public void addTestQTypeQuestionsDistritution(TestMetadata mdata,
                                                  Map<String, QuestionSearchIndexDetails> questionMap,
                                                  TestBoardWiseQuestions boarWiseQuestions, GetTestDetailsReq req) throws VedantuException {

        if (!CollectionUtils.isNotEmpty(mdata.details)) {
            return;
        }
        final String htmlBreaks = "<br/><br/>";
        for (TestDetails details : mdata.details) {
            TestQTypeQuestions question = new TestQTypeQuestions();
            question.type = details.type;
            question.totalMarks = details.qusCount * details.marks.positive;
            if (question.type == QuestionType.PARA) {
                if (details.qIds != null) {
                    Map<String, List<String>> paragraphMap = sortQuestionIdsAccordingToParagraphId(details.qIds);
                    Map<String, Integer> paragraphsQuesCount = countOfParagraphQuestionForEachParagraph(paragraphMap);
                    question.totalParagraphs = paragraphMap.keySet().size();
                    details.qIds = getOrderedQuestionIds(paragraphMap);
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if (qSearch == null) {
                            continue;
                        }
                        logger.debug("MARKS : " + details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        qSearch.paraQuestionsCount = paragraphsQuesCount.get(qid);
                        String paraContent = getParaContent(qid);
                        logger.debug(":::::   para content " + paraContent);
                        logger.debug(":::::   para question content " + qSearch.content);
                        qSearch.content = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                                + htmlBreaks + qSearch.content;
                        if (req.testState != null) {
                            if (req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                logger.debug("answer given is for qId " + qid + " " + qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if (req.needSolution) {
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            } else {
                if (details.qIds != null) {
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if (qSearch == null) {
                            continue;
                        }
                        logger.debug("MARKS : " + details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        if (req.testState != null) {
                            if (req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                logger.debug("answer given is for qId " + qid + " " + qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if (req.needSolution) {
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            }
            boarWiseQuestions.addITestQuestion(question);
        }
    }

    public Map<String, List<String>> sortQuestionIdsAccordingToParagraphId(List<String> qIds) {
        // TODO Auto-generated method stub
        Map<String, List<String>> paragraphMap = new HashMap<String, List<String>>();
        List<Question> questions = questionComponent.getQuestionsByIds(qIds);
        for (Question ques : questions) {
            if (paragraphMap.containsKey(ques.paragraphId)) {
                List<String> quesIds = paragraphMap.get(ques.paragraphId);
                quesIds.add(ques._getStringId());
                paragraphMap.put(ques.paragraphId, quesIds);
            } else {
                List<String> quesIds = new ArrayList<String>();
                quesIds.add(ques._getStringId());
                paragraphMap.put(ques.paragraphId, quesIds);
            }
        }
        return paragraphMap;
    }

    private List<String> getAnswerGiven(GetTestDetailsReq req, String qId) throws VedantuException {
        // TODO Auto-generated method stub
        logger.debug("Getting Answer for Attempt Id " + req.attemptId + " and Question Id " + qId);
        UserQuestionAttempt qAttempt = getFinilazedQuestionAttempt(req.attemptId, qId, true);
        return qAttempt.answerGiven;
    }

    public UserQuestionAttempt getFinilazedQuestionAttempt(String attemptId, String qId, boolean isJudgeable) throws VedantuException {
        logger.debug("getlastAttemptTime attemptId: " + attemptId);
        if (StringUtils.isEmpty(attemptId)) {
            throw new VedantuException(VedantuErrorCode.ATTEMPT_NOT_FOUND);
        }
        UserQuestionAttempt res = new UserQuestionAttempt();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("attemptId").is(attemptId);
        criteria.and("qId").is(qId);
        criteria.and("isJudgeable").is(isJudgeable);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ConstantsGlobal.TIME_CREATED));
        List<UserQuestionAttempt> results = mongoTemplate.find(query, UserQuestionAttempt.class);
        for (UserQuestionAttempt lastQuestionAttempt : results) {
            logger.debug("Id of the latest question attempt of qId " + qId + " and attemptId " + attemptId + " is " + lastQuestionAttempt.id);
            res = lastQuestionAttempt;
            break;
        }
        return res;
    }

    public void getSolutionsFromCMDSQuestions(QuestionSearchIndexDetails qSearch) throws VedantuException {
        Answer ans = questionComponent.getQuestionAnswer(qSearch.id);
        if (ans != null) {
            qSearch.key = ans.answer;
        }
        Solution solution = getByQuestionId(qSearch.id);
        if (solution != null) {
            qSearch.solution = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, solution.content);
        }
    }

    public Solution getByQuestionId(String questionId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("qId").is(questionId);
        criteria.and("verified").is(true);
        query.addCriteria(criteria);
        query.with(Sort.by(ConstantsGlobal.TIME_CREATED));
        Solution solution = mongoTemplate.findOne(query, Solution.class);
        if (solution == null) {
            logger.error("Cannot find solution with the quesiton id :" + questionId);
        }
        return solution;
    }

    private List<TestQTypeQuestions> addSimplifiedTestQTypeQuestionsDistritution(TestMetadata mdata,
                                                                                 Map<String, QuestionSearchIndexDetails> questionMap,
                                                                                 List<TestQTypeQuestions> questions, GetTestDetailsReq req) throws VedantuException {

        if (org.springframework.util.CollectionUtils.isEmpty(mdata.details)) {
            return new ArrayList<TestQTypeQuestions>();
        }
        List<TestQTypeQuestions> newQuestions = new ArrayList<TestQTypeQuestions>();
        Map<String, Integer> qTypeMap = new HashMap<String, Integer>();
        if (!questions.isEmpty() || questions != null) {
            int counter = 0;
            for (TestQTypeQuestions ques : questions) {
                qTypeMap.put(ques.type.toString(), counter++);
            }
        }
        final String htmlBreaks = "<br/><br/>";
        for (TestDetails details : mdata.details) {
            TestQTypeQuestions question;
            if (questions.isEmpty() || questions == null) {
                question = new TestQTypeQuestions();
            } else {
                if (qTypeMap.containsKey(details.type.toString())) {
                    question = questions.get(qTypeMap.get(details.type.toString()));
                } else {
                    question = new TestQTypeQuestions();
                }
            }
            question.type = details.type;
            question.totalMarks += details.qusCount * details.marks.positive;
            if (question.type == QuestionType.PARA) {
                if (details.qIds != null) {
                    Map<String, List<String>> paragraphMap = sortQuestionIdsAccordingToParagraphId(details.qIds);
                    Map<String, Integer> paragraphsQuesCount = countOfParagraphQuestionForEachParagraph(paragraphMap);
                    question.totalParagraphs += paragraphMap.keySet().size();
                    details.qIds = getOrderedQuestionIds(paragraphMap);
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if (qSearch == null) {
                            continue;
                        }
                        logger.debug("MARKS : " + details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        qSearch.paraQuestionsCount = paragraphsQuesCount.get(qid);
                        String paraContent = getParaContent(qid);
                        logger.debug(":::::   para content " + paraContent);
                        logger.debug(":::::   para question content " + qSearch.content);
                        qSearch.content = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                                + htmlBreaks + qSearch.content;
                        if (req.testState != null) {
                            if (req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                logger.debug("answer given is for qId " + qid + " " + qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if (req.needSolution) {
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            } else {
                if (details.qIds != null) {
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if (qSearch == null) {
                            continue;
                        }
                        logger.debug("MARKS : " + details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        if (req.testState != null) {
                            if (req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                logger.debug("answer given is for qId " + qid + " " + qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if (req.needSolution) {
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            }
            newQuestions.add(question);
        }
        return newQuestions;
    }

    public List<TestBoardWiseQuestions> getSimplifiedTestBoardWiseQuestions(Test test, GetTestDetailsReq req) throws VedantuException {
        Map<String, QuestionSearchIndexDetails> questionMap = questionComponent.getQuestionsMap(
                test.__getAllQIds(), false);
        List<TestBoardWiseQuestions> boardWiseQuestionsList = new ArrayList<TestBoardWiseQuestions>();
        Map<String, Integer> boardIndex = new LinkedHashMap<String, Integer>();
        int counter = 0;
        for (TestMetadata mdata : test.metadata) {
            boardIndex.put(mdata.id, counter++);
        }
        for (SimplifiedBoardNames simplifiedBoardName : test.simplifiedBoardNames) {
            TestBoardWiseQuestions newBoard = new TestBoardWiseQuestions();
            newBoard.name = simplifiedBoardName.simplifiedName;
            newBoard.id = "";
            List<TestQTypeQuestions> question = new ArrayList<TestQTypeQuestions>();
            for (String brdId : simplifiedBoardName.brdIds) {
                question = addSimplifiedTestQTypeQuestionsDistritution(test.metadata.get(boardIndex.get(brdId)), questionMap, question, req);
                newBoard.id += brdId + "_";
                boardIndex.remove(brdId);
            }
            for (TestQTypeQuestions ques : question) {
                newBoard.addITestQuestion(ques);
            }
            boardWiseQuestionsList.add(newBoard);
        }
        if (!boardIndex.isEmpty()) {
            for (String brdId : boardIndex.keySet()) {
                TestMetadata mdata = test.metadata.get(boardIndex.get(brdId));
                TestBoardWiseQuestions boardWiseQuestions = new TestBoardWiseQuestions(mdata.name,
                        mdata.id);
                addTestQTypeQuestionsDistritution(mdata, questionMap, boardWiseQuestions, req);
                boardWiseQuestionsList.add(boardWiseQuestions);
            }
        }
        return boardWiseQuestionsList;
    }

}

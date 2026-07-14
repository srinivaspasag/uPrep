package com.vedantu.content.managers;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.daos.analytics.UserQuestionAttemptDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.content.models.Solution;
import com.vedantu.content.models.analytics.UserQuestionAttempt;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.content.models.tests.Test;
import com.vedantu.content.pojos.requests.tests.GetSubjectiveQuestionUserAttemptsReq;
import com.vedantu.content.pojos.requests.tests.GetTestDetailsReq;
import com.vedantu.content.pojos.responses.tests.TestBoardWiseQuestions;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.pojos.tests.TestQTypeQuestions;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.search.utils.ElasticSearchUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Logger.ALogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTestManager extends AbstractContentManager {

	private static final ALogger LOGGER   = Logger.of(AbstractTestManager.class);
    protected static List<TestBoardWiseQuestions> getTestBoardWiseQuestions(
            AbstractTestCommonModel test, GetTestDetailsReq req) throws VedantuException {

        Map<String, QuestionSearchIndexDetails> questionMap = QuestionManager.getQuestionsMap(
                test.__getAllQIds(), false);
        LOGGER.info("questionMap : "+questionMap);
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
                addTestQTypeQuestionsDistritution(mdata, questionMap, boardWiseQuestions,req);
            }
            boardWiseQuestionsList.add(boardWiseQuestions);
        }
        return boardWiseQuestionsList;
    }

	protected static List<TestBoardWiseQuestions> getTestSubjectiveBoardQuestions(
			AbstractTestCommonModel test, SrcEntity parent, boolean showAttempts)
			throws VedantuException {
		List<TestBoardWiseQuestions> boardWiseQuestionsList = new ArrayList<TestBoardWiseQuestions>();
		for (TestMetadata mdata : test.metadata) {
			TestBoardWiseQuestions boardWiseQuestions = new TestBoardWiseQuestions(
					mdata.name, mdata.id);
			for (TestDetails details : mdata.details) {
				TestQTypeQuestions question = new TestQTypeQuestions();
				question.type = details.type;
				if (question.type == QuestionType.SUBJECTIVE
						&& details.qIds != null) {
					Map<String, QuestionSearchIndexDetails> questionMap = QuestionManager
							.getQuestionsMap(details.qIds, false);
					if (details.qIds != null) {
						for (String qid : details.qIds) {
							QuestionSearchIndexDetails qSearch = questionMap
									.get(qid);
							if (qSearch == null) {
								continue;
							}
							if(showAttempts){
								////Getting attempts count only if needed..
								qSearch.attempts = UserQuestionAttemptDAO.INSTANCE.getStudentAttempts(parent, qid, StringUtils.EMPTY, 0, 0, null).size();
								qSearch.notGradedCount = UserQuestionAttemptDAO.INSTANCE.getStudentAttempts(parent, qid, "UNKNOWN", 0, 0, null).size();
							}
							getSolutionsFromCMDSQuestions(qSearch);
							qSearch.marks = new Marks(details.marks.positive,
									details.marks.negative);
							boardWiseQuestions.addITestQuestion(qSearch);
						}
					}
				}
			}
			boardWiseQuestionsList.add(boardWiseQuestions);
		}
		return boardWiseQuestionsList;
	}

	protected static List<UserQuestionAttempt> getTestSubjectiveQuestionUserAttempts(
			GetSubjectiveQuestionUserAttemptsReq req, MutableLong totalHits, SrcEntity parent) throws VedantuException {
		List<UserQuestionAttempt> userQuestionAttempts = UserQuestionAttemptDAO.INSTANCE.getStudentAttempts(parent, req.id, StringUtils.EMPTY, req.start, req.size, totalHits);
		//Need to construct answer given to include images.
		List<UserQuestionAttempt> userQuestionAttemptAnswers = new ArrayList<UserQuestionAttempt>();
		for(UserQuestionAttempt qAttempt:userQuestionAttempts){
			qAttempt.answerGiven = QuestionManager.constructAnswerText(qAttempt.answerGiven);
			userQuestionAttemptAnswers.add(qAttempt);
		}
		/*
		List<UserQuestionAttempt> userQuestionAttempts = new ArrayList<UserQuestionAttempt>();
		List<UserEntityAttempt> userEntityAttempts = UserEntityAttemptDAO.INSTANCE
				.getAllSubjectiveTestAttemptsList(EntityType.TEST,
						req.testId, req.orgId, req.start, req.size,
						totalHits);
		for (UserEntityAttempt userEntityAttempt : userEntityAttempts) {
			UserQuestionAttempt qAttempt = UserQuestionAttemptDAO.INSTANCE
					.getFinilazedQuestionAttempt(
							userEntityAttempt._getStringId(), req.id, true);
			if (qAttempt.type == QuestionType.SUBJECTIVE) {
				qAttempt.answerGiven = QuestionManager
						.constructAnswerText(qAttempt.answerGiven);
				userQuestionAttempts.add(qAttempt);
			}
		}
		*/
		return userQuestionAttemptAnswers;
	}

    private static void addTestQTypeQuestionsDistritution(TestMetadata mdata,
            Map<String, QuestionSearchIndexDetails> questionMap,
            TestBoardWiseQuestions boarWiseQuestions, GetTestDetailsReq req) throws VedantuException {

        if (!CollectionUtils.isNotEmpty(mdata.details)) {
            return;
        }
        final String htmlBreaks = "<br/><br/>";
        for (TestDetails details : mdata.details) {
            TestQTypeQuestions question = new TestQTypeQuestions();
            question.type = details.type;
            if(details.maxQuestionsTobeAttempted>0){
                question.totalMarks = details.maxQuestionsTobeAttempted * details.marks.positive;
                question.maxQuestionsToBeAttempted=details.maxQuestionsTobeAttempted;
            }
            else{
                question.totalMarks = details.qusCount * details.marks.positive;
            }
            if(question.type == QuestionType.PARA){
                if (details.qIds != null) {
                    Map<String,List<String>> paragraphMap = sortQuestionIdsAccordingToParagraphId(details.qIds);
                    Map<String, Integer> paragraphsQuesCount = countOfParagraphQuestionForEachParagraph(paragraphMap);
                    question.totalParagraphs = paragraphMap.keySet().size();
                    details.qIds = getOrderedQuestionIds(paragraphMap);
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if(qSearch == null) {
                            continue;
                        }
                        LOGGER.debug("MARKS : "+details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        qSearch.paraQuestionsCount = paragraphsQuesCount.get(qid);
                        String paraContent = getParaContent(qid);
                        LOGGER.debug(":::::   para content "+ paraContent);
                        LOGGER.debug(":::::   para question content " + qSearch.content);
                        qSearch.content = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                                + htmlBreaks+qSearch.content;
                        if(req.testState != null){
                            if(req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                LOGGER.debug("answer given is for qId "+qid+" "+qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if(req.needSolution){
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            }else{
                if (details.qIds != null) {
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if(qSearch == null) {
                            continue;
                        }
                        LOGGER.debug("MARKS : "+details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        qSearch.maxQuestionsToBeAttemptedForSection=details.maxQuestionsTobeAttempted;
                        if(req.testState != null){
                            if(req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                LOGGER.debug("answer given is for qId "+qid+" "+qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if(req.needSolution){
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            }
            boarWiseQuestions.addITestQuestion(question);
        }
    }

    private static List<String> getAnswerGiven(GetTestDetailsReq req, String qId) throws VedantuException {
        // TODO Auto-generated method stub
        LOGGER.debug("Getting Answer for Attempt Id "+req.attemptId+" and Question Id "+qId);
        UserQuestionAttempt qAttempt =  UserQuestionAttemptDAO.INSTANCE.getFinilazedQuestionAttempt(req.attemptId, qId, true);
		if (qAttempt.type == QuestionType.SUBJECTIVE) {
			qAttempt.answerGiven = QuestionManager.constructAnswerText(qAttempt.answerGiven);
		}
        return qAttempt.answerGiven;
    }

    private static List<TestQTypeQuestions> addSimplifiedTestQTypeQuestionsDistritution(TestMetadata mdata,
            Map<String, QuestionSearchIndexDetails> questionMap,
            List<TestQTypeQuestions> questions, GetTestDetailsReq req) throws VedantuException {

        if (!CollectionUtils.isNotEmpty(mdata.details)) {
            return new ArrayList<TestQTypeQuestions>();
        }
        List<TestQTypeQuestions> newQuestions = new ArrayList<TestQTypeQuestions>();
        Map<String, Integer> qTypeMap = new HashMap<String, Integer>();
        if(!questions.isEmpty() || questions != null){
            int counter = 0;
            for(TestQTypeQuestions ques : questions){
                qTypeMap.put(ques.type.toString(), counter++);
            }
        }
        final String htmlBreaks = "<br/><br/>";
        for (TestDetails details : mdata.details) {
            TestQTypeQuestions question;
            if(questions.isEmpty() || questions == null){
                question = new TestQTypeQuestions();
            }else{
                if(qTypeMap.containsKey(details.type.toString())){
                    question = questions.get(qTypeMap.get(details.type.toString()));
                }else{
                    question = new TestQTypeQuestions();
                }
            }
            question.type = details.type;
            question.totalMarks += details.qusCount * details.marks.positive;
            question.maxQuestionsToBeAttempted+=details.maxQuestionsTobeAttempted;
            if(question.type == QuestionType.PARA){
                if (details.qIds != null) {
                    Map<String,List<String>> paragraphMap = sortQuestionIdsAccordingToParagraphId(details.qIds);
                    Map<String, Integer> paragraphsQuesCount = countOfParagraphQuestionForEachParagraph(paragraphMap);
                    question.totalParagraphs += paragraphMap.keySet().size();
                    details.qIds = getOrderedQuestionIds(paragraphMap);
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if(qSearch == null) {
                            continue;
                        }
                        LOGGER.debug("MARKS : "+details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        qSearch.paraQuestionsCount = paragraphsQuesCount.get(qid);
                        String paraContent = getParaContent(qid);
                        LOGGER.debug(":::::   para content "+ paraContent);
                        LOGGER.debug(":::::   para question content " + qSearch.content);
                        qSearch.content = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                                + htmlBreaks+qSearch.content;
                        if(req.testState != null){
                            if(req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                LOGGER.debug("answer given is for qId "+qid+" "+qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if(req.needSolution){
                            getSolutionsFromCMDSQuestions(qSearch);
                        }
                        question.addQuestion(qSearch);
                    }
                }
            }else{
                if (details.qIds != null) {
                    for (String qid : details.qIds) {
                        QuestionSearchIndexDetails qSearch = questionMap.get(qid);
                        if(qSearch == null) {
                            continue;
                        }
                        LOGGER.debug("MARKS : "+details.marks.toString());
                        qSearch.marks = new Marks(details.marks.positive, details.marks.negative);
                        if(req.testState != null){
                            if(req.testState.equals("RESUMED")) {
                                qSearch.answerGiven = getAnswerGiven(req, qid);
                                LOGGER.debug("answer given is for qId "+qid+" "+qSearch.answerGiven);
                            }
                        }
                        // Get keys and solution for this question
                        if(req.needSolution){
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

    protected static List<TestBoardWiseQuestions> getSimplifiedTestBoardWiseQuestions(Test test, GetTestDetailsReq req) throws VedantuException {
        Map<String, QuestionSearchIndexDetails> questionMap = QuestionManager.getQuestionsMap(
                test.__getAllQIds(), false);
        List<TestBoardWiseQuestions> boardWiseQuestionsList = new ArrayList<TestBoardWiseQuestions>();
        Map<String,Integer> boardIndex = new LinkedHashMap<String,Integer>();
        int counter = 0;
        for (TestMetadata mdata : test.metadata) {
            boardIndex.put(mdata.id, counter++);
        }
        for(SimplifiedBoardNames simplifiedBoardName : test.simplifiedBoardNames){
            TestBoardWiseQuestions newBoard = new TestBoardWiseQuestions();
            newBoard.name = simplifiedBoardName.simplifiedName;
            newBoard.id  = StringUtils.EMPTY;
            List<TestQTypeQuestions> question = new ArrayList<TestQTypeQuestions>();
            for(String brdId : simplifiedBoardName.brdIds){
                question = addSimplifiedTestQTypeQuestionsDistritution(test.metadata.get(boardIndex.get(brdId)), questionMap, question,req);
                newBoard.id += brdId+"_";
                boardIndex.remove(brdId);
            }
            for(TestQTypeQuestions ques : question){
                newBoard.addITestQuestion(ques);
            }
            boardWiseQuestionsList.add(newBoard);
        }
        if(!boardIndex.isEmpty()){
            for(String brdId: boardIndex.keySet()) {
                TestMetadata mdata = test.metadata.get(boardIndex.get(brdId));
                TestBoardWiseQuestions boardWiseQuestions = new TestBoardWiseQuestions(mdata.name,
                        mdata.id);
                addTestQTypeQuestionsDistritution(mdata, questionMap, boardWiseQuestions, req);
                boardWiseQuestionsList.add(boardWiseQuestions);
            }
        }
        return boardWiseQuestionsList;
    }

    private static Map<String, Integer> countOfParagraphQuestionForEachParagraph(
            Map<String, List<String>> paragraphMap) {
        // TODO Auto-generated method stub
        Map<String, Integer> count = new HashMap<String, Integer>();
        for(String paragraphId : paragraphMap.keySet()){
            if(paragraphMap.get(paragraphId) != null && !paragraphMap.get(paragraphId).isEmpty()){
                List<String> qIds = paragraphMap.get(paragraphId);
                for(String qId : qIds){
                    count.put(qId, qIds.size());
                }
            }
        }
        return count;
    }

    private static List<String> getOrderedQuestionIds(Map<String, List<String>> paragraphMap) {
        // TODO Auto-generated method stub
        List<String> qIds = new ArrayList<String>();
        for(String paragraphId : paragraphMap.keySet()){
            if(paragraphMap.get(paragraphId) != null && !paragraphMap.get(paragraphId).isEmpty()){
                qIds.addAll(paragraphMap.get(paragraphId));
            }
        }
        return qIds;
    }

    private static Map<String, List<String>> sortQuestionIdsAccordingToParagraphId(List<String> qIds) {
        // TODO Auto-generated method stub
        Map<String,List<String>> paragraphMap = new HashMap<String, List<String>>();
        List<Question> questions = QuestionDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(
                new ArrayList<String>(qIds), true));
        for(Question ques: questions){
            if(paragraphMap.containsKey(ques.paragraphId)){
                List<String> quesIds = paragraphMap.get(ques.paragraphId);
                quesIds.add(ques._getStringId());
                paragraphMap.put(ques.paragraphId, quesIds);
            }else{
                List<String> quesIds =  new ArrayList<String>();
                quesIds.add(ques._getStringId());
                paragraphMap.put(ques.paragraphId, quesIds);
            }
        }
        return paragraphMap;
    }

    @SuppressWarnings("unchecked")
    public static String getParaContent(String qid) {
        String paraId = "";
        String paraContext = "";
        try {
            Question question = QuestionDAO.INSTANCE.getQuestion(qid);
            paraId = question.paragraphId;
        } catch (VedantuException e) {
           LOGGER.error("Error while fetching para for question " + qid, e);
        }
        TermQueryBuilder query = QueryBuilders.termQuery("id", paraId);

        SearchResponse response = ElasticSearchUtils.getSearchResponse(query,
                "timeCreated", "desc", 0, 1,
                EntityType.CMDSQUESTION.getIndexName(), EntityType.CMDSQUESTION.getIndexType()
                        .toLowerCase(), null, false, (AbstractFacetBuilder[]) null);

        SearchHits allHits = response.getHits();
        for (SearchHit hits : allHits.getHits()) {
            paraContext = ((Map<String,Object>)hits.sourceAsMap().get("questionBody")).get("newText")+"";
        }
        return paraContext;
    }

    public static void getSolutionsFromCMDSQuestions(QuestionSearchIndexDetails qSearch) throws VedantuException {
        Answer ans = AnswerDAO.INSTANCE.getByQuestionId(qSearch.id);
        if(ans != null){
            qSearch.key = ans.answer;
        }
        Solution solution = SolutionDAO.INSTANCE.getByQuestionId(qSearch.id);
        if(solution != null){
            qSearch.solution = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, solution.content);
        }
    }

}

package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddSolutionReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.questions.AddQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.requests.questions.GetQuestionsSolutionsReq;

public interface QuestionService {

    VedantuResponse addQuestion(AddQuestionReq addQuestionReq);

    VedantuResponse addSolution(AddSolutionReq addSolutionReq);

    VedantuResponse getSolutions(GetSolutionsReq getSolutionsReq);

    VedantuResponse getQuestionInfo(GetQuestionReq getQuestionReq);

    VedantuResponse getQuestionsSolutions(GetQuestionsSolutionsReq getQuestionsSolutionsReq);

    VedantuResponse getQuestions(GetQuestionsReq getQuestionsReq);

    VedantuResponse getSimilarQuestions(GetSimilarEntities getSimilarEntities);

}

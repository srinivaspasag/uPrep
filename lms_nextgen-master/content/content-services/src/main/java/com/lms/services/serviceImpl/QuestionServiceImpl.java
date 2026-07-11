package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.QuestionComponent;
import com.lms.pojos.requests.AddSolutionReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.questions.AddQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.requests.questions.GetQuestionsSolutionsReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.questions.*;
import com.lms.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    QuestionComponent questionComponent;

    @Override
    public VedantuResponse addQuestion(AddQuestionReq addQuestionReq) {
        AddQuestionRes addQusRes = null;
        try {
            addQusRes = questionComponent.addQuestion(addQuestionReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(addQusRes);
    }

    @Override
    public VedantuResponse addSolution(AddSolutionReq addSolutionReq) {
        AddSolutionRes addSolutionRes = null;
        try {
            addSolutionRes = questionComponent.addSolution(addSolutionReq, true);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(addSolutionRes);
    }

    @Override
    public VedantuResponse getSolutions(GetSolutionsReq getSolutionsReq) {
        GetSolutionsRes getSolutionsRes = null;
        try {
            getSolutionsRes = questionComponent.getSolutions(getSolutionsReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getSolutionsRes);
    }

    @Override
    public VedantuResponse getQuestionInfo(GetQuestionReq getQusReq) {
        GetQuestionRes getQusRes = null;
        try {
            getQusRes = questionComponent.getQuestionInfo(getQusReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getQusRes);
    }

    @Override
    public VedantuResponse getQuestionsSolutions(GetQuestionsSolutionsReq getQuestionsSolutionsReq) {
        GetQuestionsSolutionRes getQusSolutionsRes = null;
        try {
            getQusSolutionsRes = questionComponent.getSolutionsMap(getQuestionsSolutionsReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getQusSolutionsRes);
    }

    @Override
    public VedantuResponse getQuestions(GetQuestionsReq getQuestionsReq) {
        ListResponse<GetQuestionRes> getQuestionsListRes = questionComponent
                .getQuestions(getQuestionsReq);
        return new VedantuResponse(getQuestionsListRes);
    }

    @Override
    public VedantuResponse getSimilarQuestions(GetSimilarEntities getSimilarQuestionsListReq) {
        SearchListResponse<GetQuestionRes> getQuestionsListRes = questionComponent
                .getSimilarQuestion(getSimilarQuestionsListReq);
        return new VedantuResponse(getQuestionsListRes);
    }

}

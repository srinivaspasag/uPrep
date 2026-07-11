package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddSolutionReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.questions.AddQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.requests.questions.GetQuestionsSolutionsReq;
import com.lms.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class Questions {
    @Autowired
    QuestionService questionService;

    @PostMapping("/addQuestion")
    public ResponseEntity<VedantuResponse> addQuestion(AddQuestionReq addQuestionReq) {
        return ResponseEntity.ok(questionService.addQuestion(addQuestionReq));
    }

    @PostMapping("/addSolution")
    public ResponseEntity<VedantuResponse> addSolution(AddSolutionReq addSolutionReq) {
        return ResponseEntity.ok(questionService.addSolution(addSolutionReq));
    }

    @PostMapping("/getSolutions")
    public ResponseEntity<VedantuResponse> getSolutions(GetSolutionsReq getSolutionsReq) {
        return ResponseEntity.ok(questionService.getSolutions(getSolutionsReq));
    }

    @PostMapping("/getQuestionInfo")
    public ResponseEntity<VedantuResponse> getQuestionInfo(GetQuestionReq getQuestionReq) {
        return ResponseEntity.ok(questionService.getQuestionInfo(getQuestionReq));
    }

    @PostMapping("/getQuestionsSolutions")
    public ResponseEntity<VedantuResponse> getQuestionsSolutions(GetQuestionsSolutionsReq getQuestionsSolutionsReq) {
        return ResponseEntity.ok(questionService.getQuestionsSolutions(getQuestionsSolutionsReq));
    }

    @PostMapping("/getQuestions")
    public ResponseEntity<VedantuResponse> getQuestions(GetQuestionsReq getQuestionsReq) {
        return ResponseEntity.ok(questionService.getQuestions(getQuestionsReq));
    }

    @PostMapping("/getSimilarQuestions")
    public ResponseEntity<VedantuResponse> getSimilarQuestions(GetSimilarEntities getSimilarEntities) {
        return ResponseEntity.ok(questionService.getSimilarQuestions(getSimilarEntities));
    }

}

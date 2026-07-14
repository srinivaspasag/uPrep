package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddQuestionReq;
import com.lms.pojos.requests.GetCMDSQuestionReq;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.PublishQuestionAsChallengeReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.responce.EditQuestionReq;
import com.lms.pojos.responce.GetUsageReq;
import com.lms.services.CmdsQuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/cmdsQuestions")
public class CMDSQuestions {
    @Autowired
    private CmdsQuestionsService cmdsQuestionsServiceImpl;

    @PostMapping("/getCMDSModuleInfo")
    public ResponseEntity<VedantuResponse> getCMDSModuleInfo(@Valid AddQuestionReq addQuestionReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.getCMDSmoduleInfo(addQuestionReq));
    }

    @PostMapping("/update")
    public ResponseEntity<VedantuResponse> update(@Valid EditQuestionReq editQuestionReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.update(editQuestionReq));
    }

    @PostMapping("/publishQuestionAsChallenge")
    public ResponseEntity<VedantuResponse> publishQuestionAsChallenge(@Valid PublishQuestionAsChallengeReq publishQuestionAsChallengeReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.publishQuestionAsChallenge(publishQuestionAsChallengeReq));
    }

    @PostMapping("/getQuestion")
    public ResponseEntity<VedantuResponse> getQuestion(@Valid GetCMDSQuestionReq getCMDSQuestionReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.getQuestion(getCMDSQuestionReq));
    }

    @PostMapping("/getQuestions")
    public ResponseEntity<VedantuResponse> getQuestions(@Valid GetQuestionsReq getQuestionsReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.getQuestions(getQuestionsReq));
    }

    @PostMapping("/getUsages")
    public ResponseEntity<VedantuResponse> getUsages(@Valid GetUsageReq getUsageReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.getUsages(getUsageReq));
    }

    @PostMapping("/getSolutions")
    public ResponseEntity<VedantuResponse> getSolutions(@Valid GetSolutionsReq getSolutionsReq) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.getSolutions(getSolutionsReq));
    }

    @GetMapping("/fixMissingSolutions")
    public ResponseEntity<VedantuResponse> fixMissingSolutions(@RequestParam String orgId, String scope) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.fixMissingSolutions(orgId, scope));
    }

    @GetMapping("/fixMissingSolution")
    public ResponseEntity<VedantuResponse> fixMissingSolution(@RequestParam String orgId, String scope, String cmdsQId) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.fixMissingSolution(orgId, scope, cmdsQId));
    }

    @GetMapping("/fixBoards")
    public ResponseEntity<VedantuResponse> fixBoards(@RequestParam String orgId, String boardId) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.fixBoards(orgId, boardId));
    }

    @GetMapping("/fixBoardMapping")
    public ResponseEntity<VedantuResponse> fixBoardMapping(@RequestParam String orgId) {
        return ResponseEntity.ok(cmdsQuestionsServiceImpl.fixBoardMapping(orgId));
    }

}

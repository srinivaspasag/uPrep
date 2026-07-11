package com.lms.controller;


import com.amazonaws.Response;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.CmdsAssignmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cmdsAssignments")
public class CMDSAssignments {
    @Autowired
    private CmdsAssignmentsService cmdsAssignmentsServiceImpl;

    @PostMapping("/createAssignment")
    public ResponseEntity<VedantuResponse> createAssignment(CreateCMDSTestReq createCMDSTestReq) {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.createAssignment(createCMDSTestReq));
    }

    @PostMapping("/addQuestion")
    public ResponseEntity<VedantuResponse> addQuestion(ModifyCMDSAssignmentQuestionsReq modifyCMDSAssignmentQuestionsReq)
    {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.addQuestion(modifyCMDSAssignmentQuestionsReq));
    }

    @PostMapping("/removeQuestion")
    public ResponseEntity<VedantuResponse> removeQuestion(ModifyCMDSAssignmentQuestionsReq modifyCMDSAssignmentQuestionsReq)
    {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.removeQuestion(modifyCMDSAssignmentQuestionsReq));
    }

    @PostMapping("/getAssignmentInfo")
    public ResponseEntity<VedantuResponse> getAssignmentInfo(GetCMDSTestReq getCMDSTestReq)
    {
        return  ResponseEntity.ok(cmdsAssignmentsServiceImpl.getAssignmentInfos(getCMDSTestReq));
    }

    @PostMapping("/getAssignmentQuestions")
    public ResponseEntity<VedantuResponse> getAssignmentQuestions(GetCMDSAssignmentQuestionsReq getCMDSAssignmentQuestionsReq)
    {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.getAssignmentQuestions(getCMDSAssignmentQuestionsReq));
    }

    @PostMapping("/finishAssignmentEditing")
    public ResponseEntity<VedantuResponse> finishAssignmentEditing(FinishCMDSAssignmentEditReq finishCMDSAssignmentEditReq)
    {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.finishAssignmentEditing(finishCMDSAssignmentEditReq));
    }

    @PostMapping("/getAssignments")
    public ResponseEntity<VedantuResponse> getAssignments(GetTestsReq getTestsReq)
    {
        return ResponseEntity.ok(cmdsAssignmentsServiceImpl.getAssignments(getTestsReq));
    }


}

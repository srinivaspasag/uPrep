package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.tests.GetAssignmentInfoReq;
import com.lms.pojos.requests.tests.GetAssignmentsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/assignments")
public class Assignments {
    @Autowired
    AssignmentService assignmentService;

    @PostMapping("/getAssignmentInfo")
    public ResponseEntity<VedantuResponse> getAssignmentInfo(GetAssignmentInfoReq getAssignmentInfoReq) {
        return ResponseEntity.ok(assignmentService.getAssignmentInfo(getAssignmentInfoReq));
    }

    @PostMapping("/getAssignmentQuestions")
    public ResponseEntity<VedantuResponse> getAssignmentQuestions(GetTestDetailsReq getTestDetailsReq) {
        return ResponseEntity.ok(assignmentService.getAssignmentQuestions(getTestDetailsReq));
    }

    @PostMapping("/getAssignments")
    public ResponseEntity<VedantuResponse> getAssignments(GetAssignmentsReq getAssignmentsReq) {
        return ResponseEntity.ok(assignmentService.getAssignments(getAssignmentsReq));
    }
}

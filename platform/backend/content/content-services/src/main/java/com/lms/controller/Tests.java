package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetTestsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.requests.tests.GetTestInfoReq;
import com.lms.services.TestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tests")
public class Tests {
    @Autowired
    private TestsService testsServiceImpl;


    @PostMapping("/getTestInfo")
    public ResponseEntity<VedantuResponse> getTestInfo(GetTestInfoReq getTestInfoReq) {
        return ResponseEntity.ok(testsServiceImpl.getTestInfo(getTestInfoReq));
    }

    @PostMapping("/getTests")
    public ResponseEntity<VedantuResponse> getTests(GetTestsReq getTestsReq) {
        return ResponseEntity.ok(testsServiceImpl.getTests(getTestsReq));
    }

    @PostMapping("/getTestQuestions")
    public ResponseEntity<VedantuResponse> getTestQuestions(GetTestDetailsReq getTestDetailsReq) {
        return ResponseEntity.ok(testsServiceImpl.getTestQuestions(getTestDetailsReq));
    }
}

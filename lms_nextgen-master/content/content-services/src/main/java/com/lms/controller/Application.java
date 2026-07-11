package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetContentForDemoReq;
import com.lms.pojos.requests.GetContentReq;
import com.lms.services.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/application")
public class Application {
    @Autowired
    private ContentService contentServiceImpl;
    public static final String server = "1";
    @GetMapping("/ping")
    public ResponseEntity<VedantuResponse> ping() {
        return ResponseEntity.ok(new VedantuResponse(server,"",""));
    }
    @PostMapping ("/ping")
    public ResponseEntity<VedantuResponse> ping1() {
        return ResponseEntity.ok(new VedantuResponse(server,"",""));
    }
    @PostMapping ("/getContentResponse")
    public ResponseEntity<VedantuResponse> getContent(@Valid GetContentReq getContentReq) {
        return ResponseEntity.ok( contentServiceImpl.getContentresponse(getContentReq));
    }
    @PostMapping ("/getContentForDemo")
    public ResponseEntity<VedantuResponse> getContent(@Valid GetContentForDemoReq getContentForDemoReq) {
        return ResponseEntity.ok( contentServiceImpl.getContentForDemo(getContentForDemoReq));
    }


}

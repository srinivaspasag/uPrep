package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.EmailStatusAndEnableReq;
import com.lms.pojos.requests.TestDummyEmailReq;
import com.lms.services.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/Mailer")
public class Mailer {
    @Autowired
    private MailerService mailerServiceImpl;

    @PostMapping("/testMail")
    public ResponseEntity<VedantuResponse> testMail(@Valid TestDummyEmailReq testDummyEmailReq) throws IOException {
        return ResponseEntity.ok(mailerServiceImpl.testMail(testDummyEmailReq));
    }

    @GetMapping("/getStatus")
    public ResponseEntity<VedantuResponse> getStatus(@Valid EmailStatusAndEnableReq emailStatusAndEnableReq) throws IOException {
        return ResponseEntity.ok(mailerServiceImpl.getStatus(emailStatusAndEnableReq));
    }

    @GetMapping("/setEmailStatus")
    public ResponseEntity<VedantuResponse> setEmailStatus(@Valid EmailStatusAndEnableReq emailStatusAndEnableReq) throws IOException {
        return ResponseEntity.ok(mailerServiceImpl.setEmailStatus(emailStatusAndEnableReq));
    }

    @GetMapping("/testConfig")
    public ResponseEntity<VedantuResponse> testConfig(@Valid EmailStatusAndEnableReq emailStatusAndEnableReq) throws IOException {
        return ResponseEntity.ok(mailerServiceImpl.testConfig(emailStatusAndEnableReq));
    }

    @PostMapping("/informOrgs")
    public ResponseEntity<VedantuResponse> informOrgs(@Valid TestDummyEmailReq testDummyEmailReq) throws IOException {
        return ResponseEntity.ok(mailerServiceImpl.informOrgs(testDummyEmailReq));
    }

}

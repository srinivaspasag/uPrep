package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/application")
public class Application {

    public static final String server = "1";

    @GetMapping("/ping")
    public ResponseEntity<VedantuResponse> ping() {
        return ResponseEntity.ok(new VedantuResponse(server, "", ""));
    }

    @PostMapping("/ping")
    public ResponseEntity<VedantuResponse> ping1() {
        return ResponseEntity.ok(new VedantuResponse(server, "", ""));
    }


}

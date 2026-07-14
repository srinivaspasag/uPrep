package com.lms.user.vedantu.controllers;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
        @RequestMapping("/application")
public class Application {
    public static final String server = "1";
    @GetMapping("/ping")
    public ResponseEntity<VedantuResponse> ping() {
        return ResponseEntity.ok(new VedantuResponse(server,"",""));
    }
}

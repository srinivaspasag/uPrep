package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/application")
public class Applictaion {


    public static final String server = "1";

    @GetMapping("/index")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Your new application is ready.");
    }
    @GetMapping("/ping")
    public ResponseEntity<VedantuResponse> ping() {
        return ResponseEntity.ok(new VedantuResponse(server,"",""));
    }
}

package com.lms.user.vedantu.controllers;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.user.vedantu.service.UserUpgradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/upgrades")

public class UserUpgrades {
    @Autowired
    private UserUpgradesService userUpgradesServiceImpl;

    @PostMapping("/generateCreds")
    public ResponseEntity<VedantuResponse> getCredentials() {

        return ResponseEntity.ok(userUpgradesServiceImpl.generateCreds());
    }
}

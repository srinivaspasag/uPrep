package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.GetOrgReq;
import com.lms.pojo.responce.ActionTakenRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/BillMocker")
public class BillMocker {
    private static final Logger logger = LoggerFactory.getLogger(BillMocker.class);

    public VedantuResponse generate(GetOrgReq getOrgReq) {

        logger.debug(" Called createDirectory");

        GetOrgReq request = null;
        ActionTakenRes response = null;

        return null;

    }

    @GetMapping("/testConfig")
    public ResponseEntity<VedantuResponse> testConfig(GetOrgReq getOrgReq) {
        return ResponseEntity.ok(generate(getOrgReq));
    }

}

package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetSDCardReq;
import com.lms.services.CMDSSDCardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sdCards")
public class CMDSSDCards {
    @Autowired
    private CMDSSDCardsService cmdssdCardsServiceImpl;

    @PostMapping
    public ResponseEntity<VedantuResponse> get(GetSDCardReq getSDCardReq) {
        return ResponseEntity.ok(cmdssdCardsServiceImpl.get(getSDCardReq));
    }
}

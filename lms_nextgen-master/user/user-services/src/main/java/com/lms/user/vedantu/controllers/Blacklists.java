package com.lms.user.vedantu.controllers;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.user.vedantu.service.BlacklistsService;
import com.lms.user.vedantu.user.requests.BlacklistEmailReq;
import com.lms.user.vedantu.user.requests.GetBlacklistEmailReq;
import com.lms.user.vedantu.user.requests.GetBlacklistedEmailsReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/blacklists")

public class Blacklists {
    @Autowired
    private BlacklistsService blacklistsServiceImpl;

    @PostMapping("/blacklist")
    public ResponseEntity<VedantuResponse> blacklist(BlacklistEmailReq blacklistEmailReq) throws VedantuException {
        return ResponseEntity.ok(blacklistsServiceImpl.getBlackList(blacklistEmailReq));
    }

    @PostMapping("/removeFromBlacklist")
    public ResponseEntity<VedantuResponse> removeFromBlacklist(BlacklistEmailReq blacklistEmailReq) throws VedantuException {
        return ResponseEntity.ok(blacklistsServiceImpl.removeFromBlacklist(blacklistEmailReq));
    }

    @PostMapping("/getBlacklistInfo")
    public ResponseEntity<VedantuResponse> getBlacklistInfo(GetBlacklistEmailReq getBlacklistEmailReq) throws VedantuException {
        return ResponseEntity.ok(blacklistsServiceImpl.getBlacklistInfo(getBlacklistEmailReq));
    }

    @PostMapping("/getBlacklistedEmails")
    public ResponseEntity<VedantuResponse> getBlacklistedEmails(GetBlacklistedEmailsReq getBlacklistedEmailsReq) throws VedantuException {
        return ResponseEntity.ok(blacklistsServiceImpl.getBlacklistedEmails(getBlacklistedEmailsReq));
    }
}

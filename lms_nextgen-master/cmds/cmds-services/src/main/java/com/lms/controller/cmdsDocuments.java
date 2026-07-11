package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetCMDSDocumentReq;
import com.lms.services.CmdsDocumentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cmdsDocuments")
public class cmdsDocuments
{

    @Autowired
    private CmdsDocumentsService cmdsDocumentsServiceImpl;


    @PostMapping("/reconvert")
    public ResponseEntity<VedantuResponse> reconvert(GetCMDSDocumentReq getCMDSDocumentReq)
    {
        return ResponseEntity.ok(cmdsDocumentsServiceImpl.reconvert(getCMDSDocumentReq));
    }

    @PostMapping("/get")
    public ResponseEntity<VedantuResponse> get(GetCMDSDocumentReq getCMDSDocumentReq)
    {
        return ResponseEntity.ok(cmdsDocumentsServiceImpl.get(getCMDSDocumentReq));
    }
}

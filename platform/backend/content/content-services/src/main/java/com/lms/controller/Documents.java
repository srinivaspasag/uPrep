package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetDocumentReq;
import com.lms.pojos.requests.GetDocumentsReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.services.DocumentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping("/documents")
@RestController
public class Documents {
    @Autowired
    private DocumentsService documentsServiceImpl;

    @PostMapping("/getDocument")
    public ResponseEntity<VedantuResponse> getDocument(@Valid GetDocumentReq getDocumentReq) throws VedantuException {
        return ResponseEntity.ok(documentsServiceImpl.getdocument(getDocumentReq));
    }

    @PostMapping("/getDocuments")
    public ResponseEntity<VedantuResponse> getDocuments(@Valid GetDocumentsReq getDocumentsReq) throws VedantuException {
        return ResponseEntity.ok(documentsServiceImpl.getdocuments(getDocumentsReq));
    }

    @PostMapping("/getSimilarDocuments")
    public ResponseEntity<VedantuResponse> getSimilarDocuments(GetSimilarEntities getSimilarEntities) throws VedantuException {
        return ResponseEntity.ok(documentsServiceImpl.getsimilarDocuments(getSimilarEntities));
    }

}

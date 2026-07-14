package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/contents")
public class ContentController {
    @Autowired
    ContentService contentService;

    @PostMapping("/getContentLinks")
    public ResponseEntity<VedantuResponse> getContentLinks(@Valid GetContentsLinkReq getContentsLinkReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getContentLinks(getContentsLinkReq));
    }

    @PostMapping("/getRemovedContentLinks")
    public ResponseEntity<VedantuResponse> getRemovedContentLinks(@Valid GetContentsLinkReq getContentsLinkReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getRemovedContentLinks(getContentsLinkReq));
    }
    @PostMapping("/getContentDownloadLink")
    public ResponseEntity<VedantuResponse> getContentDownloadLink(@Valid GetContentDownloadLinkReq getContentDownloadLinkReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getContentDownloadLink(getContentDownloadLinkReq));
    }
    @PostMapping("/getPdfDownloadLink")
    public ResponseEntity<VedantuResponse> getPdfDownloadLink(@Valid GetDownloadUrlOfPdfReq getDownloadUrlOfPdfReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getPdfDownloadLink(getDownloadUrlOfPdfReq));
    }
    @PostMapping("/getSecureLink")
    public ResponseEntity<VedantuResponse> getSecureLink(@Valid GetContentSecuredDownloadLinkReq getContentSecuredDownloadLinkReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getSecureLink(getContentSecuredDownloadLinkReq));
    }
    @PostMapping("/getEntityInfoForApp")
    public ResponseEntity<VedantuResponse> getEntityInfoForApp(@Valid GetEntityInfoForAppReq getEntityInfoForAppReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getEntityInfoForApp(getEntityInfoForAppReq));
    }
    @PostMapping("/validateResource")
    public ResponseEntity<VedantuResponse> validateResource(@Valid GetEntityReq getEntityReq) throws VedantuException {
        return ResponseEntity.ok(contentService.validateResource(getEntityReq));
    }
    @PostMapping("/getFileInfos")
    public ResponseEntity<VedantuResponse> getFileInfos(@Valid GetFileInfoReq getFileInfoReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getFileInfos(getFileInfoReq));
    }
    @PostMapping("/getContents")
    public ResponseEntity<VedantuResponse> getContents(@Valid GetContentsReq getContentsReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getcontents(getContentsReq));
    }
    @PostMapping("/getEntityReviews")
    public ResponseEntity<VedantuResponse> getEntityReviews(@Valid GetEntityReviewsReq getEntityReviewsReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getEntityReviews(getEntityReviewsReq));
    }
    @PostMapping("/addRatingAndFeedback")
    public ResponseEntity<VedantuResponse> addRatingAndFeedback(@Valid AddEntityInfoReq addEntityInfoReq) throws VedantuException {
        return ResponseEntity.ok(contentService.addRatingAndFeedback(addEntityInfoReq));
    }
    @PostMapping("/getCMDSEntityInfo")
    public ResponseEntity<VedantuResponse> getCMDSEntityInfo(@Valid GetEntityInfoForAppReq getEntityInfoForAppReq) throws VedantuException {
        return ResponseEntity.ok(contentService.getCMDSEntityInfo(getEntityInfoForAppReq));
    }
}

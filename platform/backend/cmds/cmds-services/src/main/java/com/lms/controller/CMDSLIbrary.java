package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.CMDSLIbraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/cmdsLibrary")
public class CMDSLIbrary {
    @Autowired
    private CMDSLIbraryService cmdsIbraryServiceImpl;

    @PostMapping("/addToLibrary")
    public ResponseEntity<VedantuResponse> addToLibrary(@Valid AddToLibraryReq addToLibraryReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.addToLibrary(addToLibraryReq));
    }

    @PostMapping("/removeFromLibrary")
    public ResponseEntity<VedantuResponse> removeFromLibrary(@Valid AddToLibraryReq addToLibraryReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.removeFromLibrary(addToLibraryReq));
    }

    @PostMapping("/getLibraryResources")
    public ResponseEntity<VedantuResponse> getLibraryResources(@Valid GetLibraryResourcesReq getLibraryResourcesReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.getLibraryResources(getLibraryResourcesReq));
    }

    @PostMapping("/makeVisible")
    public ResponseEntity<VedantuResponse> makeVisible(@Valid MakeVisibleReq makeVisibleReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.makeVisible(makeVisibleReq));
    }

    @PostMapping("/getVisibilityStatus")
    public ResponseEntity<VedantuResponse> getVisibilityStatus(@Valid GetVisibilityChartReq getVisibilityChartReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.getVisibilityStatus(getVisibilityChartReq));
    }

    @PostMapping("/move")
    public ResponseEntity<VedantuResponse> move(@Valid UpdateRankReq updateRankReq) {
        return ResponseEntity.ok(cmdsIbraryServiceImpl.move(updateRankReq));
    }
}

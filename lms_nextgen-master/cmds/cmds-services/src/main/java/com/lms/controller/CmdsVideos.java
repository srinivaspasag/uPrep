package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.GetCMDSVideoReq;
import com.lms.services.CmdsVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cmdsVideos")
public class CmdsVideos {
    @Autowired
    private CmdsVideoService cmdsVideoServiceimpl;

    @PostMapping("/getVideo")
    public ResponseEntity<VedantuResponse> getVideo(GetCMDSVideoReq getCMDSVideoReq) {
        return ResponseEntity.ok(cmdsVideoServiceimpl.getVideo(getCMDSVideoReq));
    }

    @PostMapping("/reprocess/{id}")
    public ResponseEntity<VedantuResponse> reprocess(@PathVariable("id") String id) {
        return ResponseEntity.ok(cmdsVideoServiceimpl.reporocess(id));
    }

    @PostMapping("/convertAgain")
    public ResponseEntity<VedantuResponse> convertAgain(@PathVariable("id") String id) {
        return ResponseEntity.ok(cmdsVideoServiceimpl.convertAgain(id));
    }

}

package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddMicrositeConfigReq;
import com.lms.pojo.request.GetOrgMicrositeConfigReq;
import com.lms.pojo.request.ValidateExternalURLReq;
import com.lms.service.MicrositesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
@RestController
@RequestMapping("/microsites")
public class Microsites {
    @Autowired
    private MicrositesService micrositesServiceImpl;

    @PostMapping("/getConfig")
    public ResponseEntity<VedantuResponse> getConfig(@Valid GetOrgMicrositeConfigReq getOrgMicrositeConfigReq) throws VedantuException {
        return ResponseEntity.ok(micrositesServiceImpl.getConfiguRation(getOrgMicrositeConfigReq));
    }
    @PostMapping("/addToConfig")
    public ResponseEntity<VedantuResponse> addToConfig(@Valid AddMicrositeConfigReq addMicrositeConfigReq) throws VedantuException {
        return ResponseEntity.ok(micrositesServiceImpl.addToConfig(addMicrositeConfigReq));
    }
    @PostMapping("/checkURL")
    public ResponseEntity<VedantuResponse> checkURL(@Valid ValidateExternalURLReq validateExternalURLReq) throws VedantuException {
        return ResponseEntity.ok(micrositesServiceImpl.checkURL(validateExternalURLReq));
    }

}

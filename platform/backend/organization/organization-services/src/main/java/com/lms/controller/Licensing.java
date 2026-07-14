package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddLicensingPlanReq;
import com.lms.pojo.request.GetLicensingPlansReq;
import com.lms.pojo.request.MarkStateReq;
import com.lms.pojo.request.campaigns.DeleteLicensingPlanReq;
import com.lms.service.LicensingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("licensings")
public class Licensing {
    @Autowired
    private LicensingService licensingServiceImpl;
    @PostMapping("/getSupportedFeatures")
    public ResponseEntity<VedantuResponse> getSupportedFeatures() throws VedantuException {
        return ResponseEntity.ok(licensingServiceImpl.getSupportedFeatures());
    }
    @PostMapping("/getPlans")
    public ResponseEntity<VedantuResponse> getPlans(GetLicensingPlansReq getLicensingPlansReq) throws VedantuException {
        return ResponseEntity.ok(licensingServiceImpl.getPlans(getLicensingPlansReq));
    }
    @PostMapping("/create")
    public ResponseEntity<VedantuResponse> create(AddLicensingPlanReq addLicensingPlanReq) throws VedantuException {
        return ResponseEntity.ok(licensingServiceImpl.create(addLicensingPlanReq));
    }
    @PostMapping("/delete")
    public ResponseEntity<VedantuResponse> delete(DeleteLicensingPlanReq deleteLicensingPlanReq) throws VedantuException {
        return ResponseEntity.ok(licensingServiceImpl.delete(deleteLicensingPlanReq));
    }
    @PostMapping("/update")
    public ResponseEntity<VedantuResponse> update() throws VedantuException {
        return null;
    }
    @PostMapping("/mark")
    public ResponseEntity<VedantuResponse> mark(MarkStateReq markStateReq) throws VedantuException {
        return ResponseEntity.ok(licensingServiceImpl.mark(markStateReq));
    }




}

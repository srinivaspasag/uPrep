package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddCampaignCodeReq;
import com.lms.pojo.request.ApplyCampaignCodeReq;
import com.lms.pojo.request.CreateBulkCampaignCodeReq;
import com.lms.pojo.request.GetCampaignCodeReq;
import com.lms.service.CampaignCodesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaignCodes")
public class CampaignCodesController {
    @Autowired
    private CampaignCodesServices campaignCodesServicesImpl;
    @PostMapping("/addCampaignCode")
    public ResponseEntity<VedantuResponse> addCampaignCode(AddCampaignCodeReq addCampaignCodeReq) throws VedantuException {
        return ResponseEntity.ok(campaignCodesServicesImpl.addCampainCode(addCampaignCodeReq));
    }
    @PostMapping("/createBulkCampaignCodes")
    public ResponseEntity<VedantuResponse> createBulkCampaignCodes(CreateBulkCampaignCodeReq createBulkCampaignCodeReq) throws VedantuException {
        return ResponseEntity.ok(campaignCodesServicesImpl.createBulkCampainCodes(createBulkCampaignCodeReq));
    }
    @PostMapping("/getCampaignCode")
    public ResponseEntity<VedantuResponse> getCampaignCode(GetCampaignCodeReq getCampaignCodeReq) throws VedantuException {
        return ResponseEntity.ok(campaignCodesServicesImpl.getCampainCode(getCampaignCodeReq));
    }
    @PostMapping("/isValidCampaignCode")
    public ResponseEntity<VedantuResponse> isValidCampaignCode(GetCampaignCodeReq getCampaignCodeReq) throws VedantuException {
        return ResponseEntity.ok(campaignCodesServicesImpl.isValidCampainCode(getCampaignCodeReq));
    }
    @PostMapping("/applyCampaignCode")
    public ResponseEntity<VedantuResponse> applyCampaignCode(ApplyCampaignCodeReq applyCampaignCodeReq) throws VedantuException {
        return ResponseEntity.ok(campaignCodesServicesImpl.applyCampainCode(applyCampaignCodeReq));
    }


}

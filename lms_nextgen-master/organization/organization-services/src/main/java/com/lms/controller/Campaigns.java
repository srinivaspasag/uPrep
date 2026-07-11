package com.lms.controller;


import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.campaigns.*;
import com.lms.service.CampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/campaigns")
public class Campaigns {
    @Autowired
    private CampaignService campaignServiceimpl;

    @PostMapping("/addCampaign")
    public ResponseEntity<VedantuResponse> addCampaign(AddCampaignReq addCampaignReq) throws VedantuException {
        return ResponseEntity.ok(campaignServiceimpl.addCampaign(addCampaignReq));
    }

    @PostMapping("/deleteCampaign")
    public ResponseEntity<VedantuResponse> deleteCampaign(DeleteCampaignReq deleteCampaignReq) throws VedantuException {
        return ResponseEntity.ok(campaignServiceimpl.deleteCampaign(deleteCampaignReq));
    }

    @PostMapping("/updateCampaign")
    public ResponseEntity<VedantuResponse> updateCampaign(UpdateCampaignReq updateCampaignReq) throws VedantuException {
        return ResponseEntity.ok(campaignServiceimpl.updateCampaign(updateCampaignReq));
    }

    @PostMapping("/getCampaign")
    public ResponseEntity<VedantuResponse> getCampaign(GetCampaignReq getCampaignReq) throws VedantuException {
        return ResponseEntity.ok(campaignServiceimpl.getCampaign(getCampaignReq));
    }

    @PostMapping("/getCampaigns")
    public ResponseEntity<VedantuResponse> getCampaigns(GetCampaignsReq getCampaignsReq) {
        return ResponseEntity.ok(campaignServiceimpl.getCampaigns(getCampaignsReq));
    }

}

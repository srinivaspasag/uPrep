package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddSalesCampaignReq;
import com.lms.pojo.request.DeleteSalesCampaignReq;
import com.lms.pojo.request.GetSalesCampaignReq;
import com.lms.pojo.request.UpdateSalesCampaignReq;
import com.lms.service.SalesCampaignsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/salesCampaigns")
public class SalesCampaigns {
    @Autowired
    private SalesCampaignsService salesCampaignsServiceImpl;
    @PostMapping("/addSalesCampaign")
    public ResponseEntity<VedantuResponse> addSalesCampaign(AddSalesCampaignReq addSalesCampaignReq) throws VedantuException {
        return ResponseEntity.ok(salesCampaignsServiceImpl.addSaleCampaign(addSalesCampaignReq));
    }
    @PostMapping("/getSalesCampaign")
    public ResponseEntity<VedantuResponse> getSalesCampaign(GetSalesCampaignReq getSalesCampaignReq) throws VedantuException {
        return ResponseEntity.ok(salesCampaignsServiceImpl.getSaleCampaign(getSalesCampaignReq));
    }
    @PostMapping("/getSalesCampaigns")
    public ResponseEntity<VedantuResponse> getSalesCampaigns(GetSalesCampaignReq getSalesCampaignReq) throws VedantuException {
        return ResponseEntity.ok(salesCampaignsServiceImpl.getSaleCampaigns(getSalesCampaignReq));
    }
    @PostMapping("/updateSalesCampaign")
    public ResponseEntity<VedantuResponse> updateSalesCampaign(UpdateSalesCampaignReq updateSalesCampaignReq) throws VedantuException {
        return ResponseEntity.ok(salesCampaignsServiceImpl.updateSaleCampaign(updateSalesCampaignReq));
    }
    @PostMapping("/deleteSalesCampaign")
    public ResponseEntity<VedantuResponse> deleteSalesCampaign(DeleteSalesCampaignReq deleteSalesCampaignReq) throws VedantuException {
        return ResponseEntity.ok(salesCampaignsServiceImpl.deleteSalesCampaign(deleteSalesCampaignReq));
    }
}

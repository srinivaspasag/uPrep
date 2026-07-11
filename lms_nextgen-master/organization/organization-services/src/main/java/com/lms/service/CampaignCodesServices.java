package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.pojo.request.AddCampaignCodeReq;
import com.lms.pojo.request.ApplyCampaignCodeReq;
import com.lms.pojo.request.CreateBulkCampaignCodeReq;
import com.lms.pojo.request.GetCampaignCodeReq;

public interface CampaignCodesServices {
    VedantuResponse addCampainCode(AddCampaignCodeReq addCampaignCodeReq);

    VedantuResponse createBulkCampainCodes(CreateBulkCampaignCodeReq createBulkCampaignCodeReq);

    VedantuResponse getCampainCode(GetCampaignCodeReq getCampaignCodeReq);

    VedantuResponse isValidCampainCode(GetCampaignCodeReq getCampaignCodeReq);

    VedantuResponse applyCampainCode(ApplyCampaignCodeReq applyCampaignCodeReq);
}

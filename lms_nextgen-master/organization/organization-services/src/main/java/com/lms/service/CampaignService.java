package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.campaigns.*;

public interface CampaignService {
    VedantuResponse addCampaign(AddCampaignReq addCampaignReq);

    VedantuResponse deleteCampaign(DeleteCampaignReq deleteCampaignReq);

    VedantuResponse updateCampaign(UpdateCampaignReq updateCampaignReq);

    VedantuResponse getCampaign(GetCampaignReq getCampaignReq);

    VedantuResponse getCampaigns(GetCampaignsReq getCampaignsReq);
}

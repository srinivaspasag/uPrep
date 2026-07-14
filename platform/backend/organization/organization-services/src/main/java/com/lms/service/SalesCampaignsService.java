package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.AddSalesCampaignReq;
import com.lms.pojo.request.DeleteSalesCampaignReq;
import com.lms.pojo.request.GetSalesCampaignReq;
import com.lms.pojo.request.UpdateSalesCampaignReq;

public interface SalesCampaignsService {
    VedantuResponse addSaleCampaign(AddSalesCampaignReq addSalesCampaignReq);

    VedantuResponse getSaleCampaign(GetSalesCampaignReq getSalesCampaignReq);

    VedantuResponse getSaleCampaigns(GetSalesCampaignReq getSalesCampaignReq);

    VedantuResponse updateSaleCampaign(UpdateSalesCampaignReq updateSalesCampaignReq);

    VedantuResponse deleteSalesCampaign(DeleteSalesCampaignReq deleteSalesCampaignReq);
}

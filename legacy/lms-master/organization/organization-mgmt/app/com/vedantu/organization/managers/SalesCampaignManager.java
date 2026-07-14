package com.vedantu.organization.managers;

import java.util.List;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.SalesCampaignDAO;
import com.vedantu.organization.models.SalesCampaign;
import com.vedantu.organization.pojos.requests.AddSalesCampaignReq;
import com.vedantu.organization.pojos.requests.DeleteSalesCampaignReq;
import com.vedantu.organization.pojos.requests.GetSalesCampaignReq;
import com.vedantu.organization.pojos.requests.GetSalesCampaignsReq;
import com.vedantu.organization.pojos.requests.UpdateSalesCampaignReq;
import com.vedantu.organization.pojos.responses.AddSalesCampaignRes;
import com.vedantu.organization.pojos.responses.DeleteSalesCampaignRes;
import com.vedantu.organization.pojos.responses.UpdateSalesCampaignRes;

public class SalesCampaignManager {

    public static SalesCampaignManager INSTANCE = new SalesCampaignManager();

    private final static ALogger       LOGGER   = Logger.of(SalesCampaignManager.class);

    public static AddSalesCampaignRes addSalesCampaign(AddSalesCampaignReq request) {

        AddSalesCampaignRes response = new AddSalesCampaignRes();
        SalesCampaign salesCampaign = new SalesCampaign(request.name, request.rewardType,
                request.rewardValue, request.startTime, request.expiryTime, request.isActive);
        SalesCampaignDAO.INSTANCE.save(salesCampaign);
        response.done = true;
        response.salesCampaign = salesCampaign;
        return response;
    }

    public static UpdateSalesCampaignRes updateSalesCampaign(UpdateSalesCampaignReq request)
            throws VedantuException {

        UpdateSalesCampaignRes response = new UpdateSalesCampaignRes();
        SalesCampaign salesCampaign = SalesCampaignDAO.INSTANCE.getById(request.salesCampaignId);
        if (salesCampaign == null) {
            LOGGER.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        salesCampaign.name = request.name;
        salesCampaign.rewardType = request.rewardType;
        salesCampaign.rewardValue = request.rewardValue;
        salesCampaign.startTime = request.startTime;
        salesCampaign.expiryTime = request.expiryTime;
        salesCampaign.isActive = request.isActive;
        SalesCampaignDAO.INSTANCE.save(salesCampaign);
        response.salesCampaign = salesCampaign;
        return response;
    }

    public static SalesCampaign getSalesCampaign(GetSalesCampaignReq request)
            throws VedantuException {

        SalesCampaign salesCampaign = SalesCampaignDAO.INSTANCE.getById(request.salesCampaignId);
        if (salesCampaign == null) {
            LOGGER.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        return salesCampaign;
    }

    public static List<SalesCampaign> getSalesCampaigns(GetSalesCampaignsReq request) {

        List<SalesCampaign> salesCampaigns = SalesCampaignDAO.INSTANCE.getAllSalesCampaigns();
        if (salesCampaigns == null) {
            return null;
        }
        return salesCampaigns;
    }

    public static DeleteSalesCampaignRes deleteSalesCampaign(DeleteSalesCampaignReq request)
            throws VedantuException {

        DeleteSalesCampaignRes response = new DeleteSalesCampaignRes();
        SalesCampaign salesCampaign = SalesCampaignDAO.INSTANCE.getById(request.salesCampaignId);
        if (salesCampaign == null) {
            LOGGER.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        salesCampaign.recordState = VedantuRecordState.DELETED;
        SalesCampaignDAO.INSTANCE.save(salesCampaign);
        response.salesCampaign = salesCampaign;
        return response;
    }
}

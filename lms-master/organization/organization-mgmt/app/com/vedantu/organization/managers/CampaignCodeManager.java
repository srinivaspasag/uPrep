package com.vedantu.organization.managers;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.daos.CampaignCodeDAO;
import com.vedantu.organization.daos.SalesCampaignDAO;
import com.vedantu.organization.models.CampaignCode;
import com.vedantu.organization.models.SalesCampaign;
import com.vedantu.organization.pojos.requests.AddCampaignCodeReq;
import com.vedantu.organization.pojos.requests.ApplyCampaignCodeReq;
import com.vedantu.organization.pojos.requests.CreateBulkCampaignCodeReq;
import com.vedantu.organization.pojos.requests.GetCampaignCodeReq;
import com.vedantu.organization.pojos.responses.AddCampaignCodeRes;
import com.vedantu.organization.pojos.responses.ApplyCampaignCodeRes;
import com.vedantu.organization.pojos.responses.CreateBulkCampaignCodeRes;
import com.vedantu.organization.pojos.responses.GetCampaignCodeRes;
import com.vedantu.organization.pojos.responses.ValidateCampaignCodeRes;

public class CampaignCodeManager {

    public static CampaignCodeManager INSTANCE = new CampaignCodeManager();

    private final static ALogger      LOGGER   = Logger.of(CampaignCodeManager.class);

    public static AddCampaignCodeRes addCampaignCode(AddCampaignCodeReq request) {

        AddCampaignCodeRes response = new AddCampaignCodeRes();
        CampaignCode campaignCode = new CampaignCode(request.salesCampaignId, request.maxUsageCount);
        campaignCode.code = CampaignCodeDAO.INSTANCE.generateCampaignCode();
        campaignCode.expired = false;
        CampaignCodeDAO.INSTANCE.save(campaignCode);
        response.done = true;
        response.campaignCode = campaignCode;
        return response;
    }

    public static CreateBulkCampaignCodeRes createBulkCampaignCodes(
            CreateBulkCampaignCodeReq request) {

        CreateBulkCampaignCodeRes response = new CreateBulkCampaignCodeRes();
        for (int i = 0; i < request.numberOfCampaignCodesRequired; i++) {
            CampaignCode campaignCode = new CampaignCode();
            campaignCode.salesCampaignId = request.salesCampaignId;
            campaignCode.code = CampaignCodeDAO.INSTANCE.generateCampaignCode();
            campaignCode.expired = false;
            campaignCode.maxUsageCount = 1;
            CampaignCodeDAO.INSTANCE.save(campaignCode);
            response.campaignCodes.add(campaignCode);
        }
        response.success = true;
        return response;
    }

    public static GetCampaignCodeRes getCampaignCode(GetCampaignCodeReq request)
            throws VedantuException {

        GetCampaignCodeRes response = new GetCampaignCodeRes();
        CampaignCode campaignCode = CampaignCodeDAO.INSTANCE
                .getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            LOGGER.error("campaignCode does not exist, check the code : " + request.campaignCode);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        response.campaignCode = campaignCode;
        return response;
    }

    public static ValidateCampaignCodeRes isValidCampaignCode(GetCampaignCodeReq request) {

        ValidateCampaignCodeRes response = new ValidateCampaignCodeRes();
        CampaignCode campaignCode = CampaignCodeDAO.INSTANCE
                .getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            LOGGER.error("campaignCode does not exist, check the code : " + request.campaignCode);
            response.campaignCodeExists = false;
            return response;
        }
        SalesCampaign salesCampaign = SalesCampaignDAO.INSTANCE
                .getById(campaignCode.salesCampaignId);
        if (salesCampaign.startTime > System.currentTimeMillis()
                || salesCampaign.expiryTime < System.currentTimeMillis()) {
            LOGGER.error("salesCampaign is not started or expired " + campaignCode.salesCampaignId);
            response.campaignCodeExists = false;
            return response;
        }
        response.campaignCodeExists = true;
        return response;
    }

    public static ApplyCampaignCodeRes applyCampaignCode(ApplyCampaignCodeReq request)
            throws VedantuException {

        ApplyCampaignCodeRes response = new ApplyCampaignCodeRes();
        CampaignCode campaignCode = CampaignCodeDAO.INSTANCE
                .getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            LOGGER.error("campaignCode does not exist, check the code : " + request.campaignCode);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        if (campaignCode.maxUsageCount <= campaignCode.currentUsageCount) {
            LOGGER.error("campaignCode used maximum times : " + request.campaignCode);
            response.applied = false;
            response.message = "Code is used maximum times";
            return response;
        }
        SalesCampaign salesCampaign = SalesCampaignDAO.INSTANCE
                .getById(campaignCode.salesCampaignId);
        if (salesCampaign.startTime > System.currentTimeMillis()
                || salesCampaign.expiryTime < System.currentTimeMillis()) {
            LOGGER.error("salesCampaign is not started or expired " + campaignCode.salesCampaignId);
            response.applied = false;
            response.message = "Campaign not started or expired";
            return response;
        }
        campaignCode.currentUsageCount += 1;
        campaignCode.consumerUserIds.add(request.userId);
        if (campaignCode.maxUsageCount == campaignCode.currentUsageCount) {
            campaignCode.expired = true;
        }
        CampaignCodeDAO.INSTANCE.save(campaignCode);
        response.applied = true;
        response.message = "Applied Successfully";
        return response;
    }
}

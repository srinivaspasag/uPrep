package com.vedantu.organization.managers;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.daos.CampaignDAO;
import com.vedantu.organization.models.Campaign;
import com.vedantu.organization.pojos.requests.campaigns.AddCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.DeleteCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.GetCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.GetCampaignsReq;
import com.vedantu.organization.pojos.requests.campaigns.UpdateCampaignReq;
import com.vedantu.organization.pojos.responses.campaigns.AddCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.DeleteCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.GetCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.GetCampaignsRes;
import com.vedantu.organization.pojos.responses.campaigns.UpdateCampaignRes;

public class CampaignManager {

    public static CampaignManager INSTANCE = new CampaignManager();

    private final static ALogger  LOGGER   = Logger.of(CampaignManager.class);

    public static AddCampaignRes addCampaign(AddCampaignReq addCampaignReq) throws VedantuException {

        AddCampaignRes response = null;
        Campaign campaignExisting = CampaignDAO.INSTANCE
                .getCampaignWithCampaignType(addCampaignReq.campaignType);
        Campaign campaign = new Campaign();
        if (campaignExisting == null) {
            campaign.message = addCampaignReq.message;
            campaign.friendRewards = addCampaignReq.friendRewards;
            campaign.referrerRewards = addCampaignReq.referrerRewards;
            campaign.campaignType = addCampaignReq.campaignType;
            CampaignDAO.INSTANCE.save(campaign);
        } else {
            LOGGER.error("Please check for the active campaigns in Campaign Type :"
                    + addCampaignReq.campaignType
                    + " only one campaign can be active in one campaign type.");
            throw new VedantuException(VedantuErrorCode.ANOTHER_ACTIVE_CAMPAIGN_EXIST);
        }
        response = new AddCampaignRes(campaign);
        return response;
    }

    public static DeleteCampaignRes deleteCampaign(DeleteCampaignReq deleteCampaignReq)
            throws VedantuException {

        DeleteCampaignRes response = new DeleteCampaignRes();
        CampaignDAO.INSTANCE.updateRecordState(deleteCampaignReq.id);
        response.done = true;
        return response;
    }

    public static UpdateCampaignRes updateCampaign(UpdateCampaignReq updateCampaignReq) {

        UpdateCampaignRes response = new UpdateCampaignRes();
        Campaign campaign = new Campaign();
        campaign.message = updateCampaignReq.message;
        campaign.friendRewards = updateCampaignReq.friendRewards;
        campaign.referrerRewards = updateCampaignReq.referrerRewards;
        Campaign updatedCampaign = CampaignDAO.INSTANCE.updatecampaign(campaign,
                updateCampaignReq.id);
        response.campaign = updatedCampaign;
        return response;
    }

    public static GetCampaignsRes getCampaigns(GetCampaignsReq getCampaignsReq) {
        GetCampaignsRes response = new GetCampaignsRes();
        Campaign campaign = CampaignDAO.INSTANCE
                .getCampaignWithCampaignType(getCampaignsReq.campaignType);
        response.campaign = campaign;
        return response;
    }

    public static GetCampaignRes getCampaign(GetCampaignReq getCampaignReq) throws VedantuException {
        GetCampaignRes response = new GetCampaignRes();
        Campaign campaign = CampaignDAO.INSTANCE.getCampaign(getCampaignReq.id);
        response.campaign = campaign;
        return response;
    }
}

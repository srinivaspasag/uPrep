package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.CampaignType;
import com.lms.models.Campaign;
import com.lms.pojo.request.GetCampaignsRes;
import com.lms.pojo.request.campaigns.*;
import com.lms.pojo.responce.campaigns.AddCampaignRes;
import com.lms.pojo.responce.campaigns.DeleteCampaignRes;
import com.lms.pojo.responce.campaigns.GetCampaignRes;
import com.lms.pojo.responce.campaigns.UpdateCampaignRes;
import com.lms.repository.CampaignRepo;
import com.lms.service.CampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CampaignServiceImpl implements CampaignService {
    private static final Logger logger = LoggerFactory.getLogger(CampaignServiceImpl.class);

    @Autowired
    CampaignRepo campaignRepo;

    @Override
    public VedantuResponse addCampaign(AddCampaignReq addCampaignReq) {
        AddCampaignRes response = null;
        Campaign campaignExisting = campaignRepo.findByCampaignType(addCampaignReq.getCampaignType());
        Campaign campaign = new Campaign();
        if (campaignExisting == null) {
            campaign.message = addCampaignReq.message;
            campaign.friendRewards = addCampaignReq.friendRewards;
            campaign.referrerRewards = addCampaignReq.referrerRewards;
            campaign.campaignType = addCampaignReq.campaignType;
            campaignRepo.save(campaign);
        } else {
            logger.error("Please check for the active campaigns in Campaign Type :"
                    + addCampaignReq.campaignType
                    + " only one campaign can be active in one campaign type.");
            throw new VedantuException(VedantuErrorCode.ANOTHER_ACTIVE_CAMPAIGN_EXIST);
        }
        response = new AddCampaignRes(campaign);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse deleteCampaign(DeleteCampaignReq deleteCampaignReq) {
        DeleteCampaignRes response = new DeleteCampaignRes();
        updateRecordState(deleteCampaignReq.id);
        response.done = true;
        return new VedantuResponse(response);
    }

    public boolean updateRecordState(String id) throws VedantuException {

        Campaign campaign = campaignRepo.findById(id).get();
        if (campaign != null) {
            campaign.recordState = VedantuRecordState.DELETED;
            campaignRepo.save(campaign);
            return true;
        } else {
            logger.error("Campaign does not exist, Please check campaign id :" + id);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
    }

    @Override
    public VedantuResponse updateCampaign(UpdateCampaignReq updateCampaignReq) {
        if (updateCampaignReq == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        UpdateCampaignRes response = new UpdateCampaignRes();
        Campaign campaign = new Campaign();
        campaign.message = updateCampaignReq.message;
        campaign.friendRewards = updateCampaignReq.friendRewards;
        campaign.referrerRewards = updateCampaignReq.referrerRewards;
        Campaign updatedCampaign = updatecampaign(campaign, updateCampaignReq.id);
        response.campaign = updatedCampaign;
        return new VedantuResponse(response);
    }

    public Campaign updatecampaign(Campaign campaignUpdated, String campaignId) {

        Campaign campaign = campaignRepo.findById(campaignId).get();
        campaign.message = campaignUpdated.message;
        campaign.friendRewards = campaignUpdated.friendRewards;
        campaign.referrerRewards = campaignUpdated.referrerRewards;
        campaignRepo.save(campaign);
        return campaign;
    }

    @Override
    public VedantuResponse getCampaign(GetCampaignReq getCampaignReq) {

        GetCampaignRes response = new GetCampaignRes();
        Campaign campaign = getCampaign(getCampaignReq.id);
        response.campaign = campaign;
        return new VedantuResponse(response);
    }


    @Override
    public VedantuResponse getCampaigns(GetCampaignsReq getCampaignsReq) {
        GetCampaignsRes response = new GetCampaignsRes();
        Campaign campaign = getCampaignWithCampaignType(getCampaignsReq.getCampaignType());
        if (campaign == null) {
            logger.error("campaign does not exist");
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        response.campaign = campaign;
        return new VedantuResponse(response);
    }

    public Campaign getCampaign(String id) throws VedantuException {

        Campaign campaign = campaignRepo.findById(id).get();
        if (campaign == null) {
            logger.error("campaign does not exist, check the id : " + id);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        return campaign;
    }

    public Campaign getCampaignWithCampaignType(CampaignType campaignType) {
        Campaign campaign = campaignRepo.findByCampaignTypeAndRecordState(campaignType, VedantuRecordState.ACTIVE);
        return campaign;
    }
}

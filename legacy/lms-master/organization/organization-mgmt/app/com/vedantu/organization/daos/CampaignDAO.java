package com.vedantu.organization.daos;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.CampaignType;
import com.vedantu.organization.models.Campaign;

public class CampaignDAO extends VedantuBasicDAO<Campaign, ObjectId> {

    private static final ALogger    LOGGER   = Logger.of(CampaignDAO.class);
    public static final CampaignDAO INSTANCE = new CampaignDAO();

    public CampaignDAO() {
        super(Campaign.class);
    }

    public boolean updateRecordState(String id) throws VedantuException {

        Campaign campaign = getById(id);
        if (campaign != null) {
            campaign.recordState = VedantuRecordState.DELETED;
            return true;
        } else {
            LOGGER.error("Campaign does not exist, Please check campaign id :" + id);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
    }

    public Campaign updatecampaign(Campaign campaignUpdated, String campaignId) {

        Campaign campaign = getById(campaignId);
        campaign.message = campaignUpdated.message;
        campaign.friendRewards = campaignUpdated.friendRewards;
        campaign.referrerRewards = campaignUpdated.referrerRewards;
        save(campaign);
        return campaign;
    }

    public Campaign getCampaignWithCampaignType(CampaignType campaignType) {

        Query<Campaign> query = getDS().find(entityClazz).filter("recordState",
                VedantuRecordState.ACTIVE);
        Campaign campaign = query.filter("campaignType", campaignType).get();
        return campaign;
    }

    public Campaign getCampaign(String id) throws VedantuException {

        Campaign campaign = CampaignDAO.INSTANCE.getById(id);
        if (campaign != null) {
            LOGGER.error("campaign does not exist, check the id : " + id);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        return campaign;
    }

}

package com.vedantu.organization.daos;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.models.CampaignCode;

public class CampaignCodeDAO extends VedantuBasicDAO<CampaignCode, ObjectId> {

    private static final ALogger        LOGGER   = Logger.of(CampaignCodeDAO.class);
    public static final CampaignCodeDAO INSTANCE = new CampaignCodeDAO();

    private CampaignCodeDAO() {
        super(CampaignCode.class);
    }

    public CampaignCode getCampaignCodeByCode(String code) {
        Query<CampaignCode> query = getDS().find(entityClazz).filter("recordState",
                VedantuRecordState.ACTIVE);
        CampaignCode campaignCode = query.filter("code", code).filter("expired", false).get();
        return campaignCode;
    }

    public String generateCampaignCode() {
        String campaignCode = RandomStringUtils.randomAlphabetic(8).toLowerCase();
        boolean isUniqueCampaignCode = checkCampaignCodeUniqueness(campaignCode);
        if (!isUniqueCampaignCode) {
            campaignCode = generateCampaignCode();
        }
        return campaignCode;
    }

    public boolean checkCampaignCodeUniqueness(String campaignCode) {
        Query<CampaignCode> query = getQuery().filter("code", campaignCode);
        List<CampaignCode> campaignCodes = query.asList();
        if (campaignCodes.isEmpty()) {
            return true;
        }
        return false;
    }

}

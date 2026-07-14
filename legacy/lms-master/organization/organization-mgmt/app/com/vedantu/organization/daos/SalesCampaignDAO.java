package com.vedantu.organization.daos;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.models.SalesCampaign;

public class SalesCampaignDAO extends VedantuBasicDAO<SalesCampaign, ObjectId> {

    private static final ALogger         LOGGER   = Logger.of(SalesCampaignDAO.class);
    public static final SalesCampaignDAO INSTANCE = new SalesCampaignDAO();

    private SalesCampaignDAO() {
        super(SalesCampaign.class);
    }

    public List<SalesCampaign> getAllSalesCampaigns() {
        Query<SalesCampaign> query = getDS().find(entityClazz).filter("recordState",
                VedantuRecordState.ACTIVE);
        List<SalesCampaign> salesCampaigns = query.asList();
        return salesCampaigns;
    }

}

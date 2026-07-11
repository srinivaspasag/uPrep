package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.CenterNewsEntityDetails;
import com.vedantu.comm.utils.news.NewsUtils;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.models.OrgCenter;

public class CenterNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static CenterNewsPopulator INSTANCE = new CenterNewsPopulator();
    private final static ALogger            LOGGER   = Logger.of(CenterNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        if (newsEntities == null || newsEntities.isEmpty()) {
            LOGGER.error("newsEntities set is null or empty");
            return;
        }
        Map<String, SrcEntity> entityIds = NewsUtils.getSrcEntityIds(newsEntities);
        LOGGER.info("Received cursor from mongo for " + entityIds.keySet());

        for (String key : entityIds.keySet()) {
            String centerId = getCenterId(key);
            SrcEntity keyEntity = entityIds.get(key);
            CenterNewsEntityDetails centerDetails = (CenterNewsEntityDetails) populate(orgId,
                    userId, new SrcEntity(EntityType.CENTER, centerId), null);
            Logger.info("Decorating from cursor found key entity " + keyEntity);
            srcEntityDetails.put(keyEntity, centerDetails);
        }

    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> detailsMap) {

        CenterNewsEntityDetails details = null;

        OrgCenter center = (OrgCenter) (detailsMap == null ? OrgCenterDAO.INSTANCE
                .getById(newsEntity.id) : detailsMap.get(newsEntity.id));

        if (center == null) {
            LOGGER.error("received cursor is null");
            return details;
        }
        details = new CenterNewsEntityDetails(newsEntity.type, newsEntity.id);

        details.name = center.getName();
        details.code = center.code;
        LOGGER.info("Decorating from cursor from program for " + details.name);
        return details;
    }

    private String getCenterId(String key) {

        String centerId = null;
        String[] ids = key.split("#");
        if (ids.length > 1) {
            centerId = ids[1];
        } else {

            centerId = ids[0];
        }
        return centerId;

    }
}

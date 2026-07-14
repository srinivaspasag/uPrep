package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.models.mongo.Remark;
import com.vedantu.comm.news.details.RemarkNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.IVedantuModel;

public class RemarksNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static RemarksNewsPopulator INSTANCE = new RemarksNewsPopulator();
    private final static ALogger             LOGGER   = Logger.of(RemarksNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        Remark remark = (Remark) modelDetailMap.get(newEntity.id);

        if (remark == null) {
            LOGGER.error("no remark found for: " + newEntity);
            return null;
        }

        RemarkNewsEntityDetails details = new RemarkNewsEntityDetails(newEntity.type, newEntity.id);

        details.content = remark.content;
        details.contentSrc = new SrcEntity(EntityType.ORGANIZATION, remark.orgId);
        LOGGER.info("Decorating from cursor from remarks for " + details);
        return details;
    }

}

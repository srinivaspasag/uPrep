package com.vedantu.comm.managers.news.populators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.CenterNewsEntityDetails;
import com.vedantu.comm.news.details.ProgramNewsEntityDetails;
import com.vedantu.comm.news.details.SectionNewsEntityDetails;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.models.OrgSection;

public class SectionNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static SectionNewsPopulator INSTANCE = new SectionNewsPopulator();
    private final static ALogger             LOGGER   = Logger.of(SectionNewsPopulator.class);

    private SectionNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        OrgSection section = (OrgSection) modelDetailMap.get(newEntity.id);
        if (section == null) {
            LOGGER.error("no section found for entity: " + newEntity);
            return null;
        }

        SectionNewsEntityDetails details = new SectionNewsEntityDetails(EntityType.SECTION,
                section._getStringId());

        details.name = section.getName();
        details.code = section.code;
        LOGGER.info("Decorated section : " + details);

        if (StringUtils.isNotEmpty(section.programId)) {
            IPopulator programPopulator = EntityDetailsPopulatorFactory.INSTANCE
                    .get(EntityType.PROGRAM);
            if (programPopulator != null) {

                VedantuBaseMongoModel model = EntityTypeDAOFactory.INSTANCE.get(EntityType.PROGRAM)
                        .getById(section.programId);
                Map<String, IVedantuModel> detailMap = new HashMap<String, IVedantuModel>();
                detailMap.put(model._getStringId(), model);
                details.program = (ProgramNewsEntityDetails) programPopulator.populate(orgId,
                        userId, new SrcEntity(EntityType.PROGRAM, model._getStringId()), detailMap);
            }
        }
        
        if (StringUtils.isNotEmpty(section.centerId)) {
            IPopulator centerPopulator = EntityDetailsPopulatorFactory.INSTANCE
                    .get(EntityType.CENTER);
            if (centerPopulator != null) {

                VedantuBaseMongoModel model = EntityTypeDAOFactory.INSTANCE.get(EntityType.CENTER)
                        .getById(section.centerId);
                Map<String, IVedantuModel> detailMap = new HashMap<String, IVedantuModel>();
                detailMap.put(model._getStringId(), model);
                details.center = (CenterNewsEntityDetails) centerPopulator.populate(orgId,
                        userId, new SrcEntity(EntityType.CENTER, model._getStringId()), detailMap);
            }
        }
        return details;
    }
}

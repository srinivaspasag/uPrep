package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.ModuleNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Module;
import com.vedantu.mongo.IVedantuModel;

public class ModuleNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static ModuleNewsPopulator INSTANCE = new ModuleNewsPopulator();
    private final static ALogger           LOGGER   = Logger.of(ModuleNewsPopulator.class);

    private ModuleNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        LOGGER.debug(" Populating module " + newEntity.id);
        Module module = (Module) modelDetailMap.get(newEntity.id);
        if (module == null) {
            LOGGER.error("no module found for : " + newEntity);
            return null;
        }

        ModuleNewsEntityDetails details = new ModuleNewsEntityDetails(newEntity.id);
        details.totalContentCount = module.totalContentCount;

        details.name = module.name;
        details.id = module._getStringId();
        details.type = EntityType.MODULE;
        details.timeCreated = module.timeCreated;
        details.contentSrc = module.contentSrc;
        
        
        return details;

    }

}

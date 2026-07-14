package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.TestNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.tests.AbstractTestCommonModel;
import com.vedantu.mongo.IVedantuModel;

public abstract class AbstractTestNewsPopulator extends AbstractEntityDetailsPopulator {

    private final static ALogger LOGGER = Logger.of(AbstractTestNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        TestNewsEntityDetails details = null;
        AbstractTestCommonModel test = (AbstractTestCommonModel) modelDetailMap.get(newsEntity.id);
        if (test == null) {
            return details;
        }
        details = new TestNewsEntityDetails(newsEntity.id, newsEntity.type);
        LOGGER.debug(" Populating test info " + details.id);

        details.contentSrc = test.contentSrc;
        details.timeCreated = test.timeCreated;
        details.name = test.name;
        
        return details;
    }

}

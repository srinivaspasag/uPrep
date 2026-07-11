package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.SolutionNewsDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Solution;
import com.vedantu.mongo.IVedantuModel;

public class SolutionNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static SolutionNewsPopulator INSTANCE = new SolutionNewsPopulator();
    private final static ALogger              LOGGER   = Logger.of(SolutionNewsPopulator.class);

    private SolutionNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        Solution solution = (Solution) modelDetailMap.get(newsEntity.id);
        if (solution == null) {
            return null;
        }
        SolutionNewsDetails details = null;

        details = new SolutionNewsDetails(newsEntity.id);
        LOGGER.debug(" Populating solution  " + details.id + " info");
        details.qId= solution.qId;
        details.content = solution.content;
        details.timeCreated = solution.timeCreated;
   
        return details;
    }
}

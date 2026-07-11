package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.ProgramNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.IVedantuModel;
import com.vedantu.organization.models.OrgProgram;

public class ProgramNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static ProgramNewsPopulator INSTANCE = new ProgramNewsPopulator();
    private final static ALogger             LOGGER   = Logger.of(ProgramNewsPopulator.class);

    private ProgramNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);

    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        OrgProgram program = (OrgProgram) modelDetailMap.get(newEntity.id);
        if (program == null) {
            LOGGER.error("no program found for  : " + newEntity);
            return null;
        }
        ProgramNewsEntityDetails programDetails = new ProgramNewsEntityDetails(EntityType.PROGRAM,
                program._getStringId());

        programDetails.name = program.getName();
        programDetails.code = program.code;
        LOGGER.debug("Decorating from cursor from program for " + programDetails.name);
        return programDetails;
    }

}

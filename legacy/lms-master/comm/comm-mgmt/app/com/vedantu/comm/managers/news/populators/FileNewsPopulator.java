package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.FileNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.File;
import com.vedantu.mongo.IVedantuModel;

public class FileNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static FileNewsPopulator INSTANCE = new FileNewsPopulator();
    private final static ALogger          LOGGER   = Logger.of(FileNewsPopulator.class);

    private FileNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        LOGGER.debug(" Populating file " + newEntity.id);
        File file = (File) modelDetailMap.get(newEntity.id);
        if (file == null) {
            LOGGER.error("no file found for : " + newEntity);
            return null;
        }
        FileNewsEntityDetails details = new FileNewsEntityDetails(newEntity.id);

        details.name = file.name;
        details.id = file._getStringId();
        details.type = EntityType.FILE;
        details.timeCreated = file.timeCreated;
        details.contentSrc = file.contentSrc;
        return details;

    }

}

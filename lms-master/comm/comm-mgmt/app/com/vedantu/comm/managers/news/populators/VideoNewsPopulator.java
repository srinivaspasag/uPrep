package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.VideoNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Video;
import com.vedantu.mongo.IVedantuModel;

public class VideoNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static VideoNewsPopulator INSTANCE = new VideoNewsPopulator();
    private final static ALogger           LOGGER   = Logger.of(VideoNewsPopulator.class);

    private VideoNewsPopulator() {

    }

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        LOGGER.debug(" Populating video " + newEntity.id);
        Video video = (Video) modelDetailMap.get(newEntity.id);
        if (video == null) {
            LOGGER.error("no video found for : " + newEntity);
            return null;
        }

        VideoNewsEntityDetails details = new VideoNewsEntityDetails(newEntity.id);
        details.duration = video.duration;

        details.name = video.name;
        details.id = video._getStringId();
        details.type = EntityType.VIDEO;
        details.timeCreated = video.timeCreated;
        details.contentSrc = video.contentSrc;
        
        
        return details;

    }

}

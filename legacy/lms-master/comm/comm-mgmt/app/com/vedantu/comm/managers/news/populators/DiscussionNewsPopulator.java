package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.DiscussionNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.Discussion;
import com.vedantu.mongo.IVedantuModel;

public class DiscussionNewsPopulator extends AbstractEntityDetailsPopulator {

    public final static DiscussionNewsPopulator INSTANCE = new DiscussionNewsPopulator();
    private final static ALogger                LOGGER   = Logger.of(DiscussionNewsPopulator.class);

    @Override
    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newsEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        LOGGER.debug(" Populating discussion " + newsEntity.id);
        Discussion discussion = (Discussion) modelDetailMap.get(newsEntity.id);
        if (discussion == null) {
            return null;
        }
        DiscussionNewsEntityDetails details = null;
        details = new DiscussionNewsEntityDetails(newsEntity.id);
        details.name = discussion.name;
        details.timeCreated = discussion.timeCreated;
        details.contentSrc = discussion.contentSrc;
        return details;
    }
}

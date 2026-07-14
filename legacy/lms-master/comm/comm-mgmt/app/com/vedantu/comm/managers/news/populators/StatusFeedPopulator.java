package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.news.details.StatusFeedNewsEntityDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.models.StatusFeed;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.mongo.IVedantuModel;

public class StatusFeedPopulator extends AbstractEntityDetailsPopulator {

    public final static StatusFeedPopulator INSTANCE = new StatusFeedPopulator();
    private final static ALogger            LOGGER   = Logger.of(StatusFeedPopulator.class);

    private StatusFeedPopulator() {

    }

    public void populate(String orgId, String userId, Set<SrcEntity> newsEntities,
            Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

        super.populate(orgId, userId, newsEntities, srcEntityDetails, entityType);
    }

    @Override
    public SrcEntity populate(String orgId, String userId, SrcEntity newEntity,
            Map<String, IVedantuModel> modelDetailMap) {

        StatusFeed feed = (StatusFeed) modelDetailMap.get(newEntity.id);
        if (feed == null) {
            LOGGER.error("no statusfeed found for entity: " + newEntity);
            return null;
        }

        StatusFeedNewsEntityDetails details = new StatusFeedNewsEntityDetails(newEntity.id);

        LOGGER.debug(" Populating status feed" + details.id);
        details.statusMessage = feed.statusMessage;
        details.sourceContent = feed.sourceContent;
        if (details.sourceContent != null) {

            if (StringUtils.isNotEmpty(details.sourceContent.image)) {
                LOGGER.info("Source image" + details.sourceContent.image);
                details.sourceContent.image = ImageDisplayURLUtil
                        .getStatuFeedOrginalImageURL(details.sourceContent.image);
                if (details.sourceContent.linkType == LinkType.UPLOADED) {
                    details.sourceContent.url = details.sourceContent.image;
                }
            }
            if (StringUtils.isEmpty(details.sourceContent.url)) {
                details.sourceContent.url = StringUtils.EMPTY;
            }
            if (details.sourceContent.linkInfo != null) {
                details.sourceContent.linkInfo.populate();
            }
        }
        details.userId = feed.userId;
        details.timeCreated = feed.timeCreated;
        details.upVotes = feed.upVotes;
        details.comments = feed.comments;
        details.followers = feed.followers;
        details.contentSrc = feed.contentSrc;
        return details;
    }

}
package com.vedantu.comm.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.populators.EntityDetailsPopulatorFactory;
import com.vedantu.comm.managers.news.populators.IPopulator;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class NewsAggregatorHelper {

    private static final ALogger LOGGER = Logger.of(NewsAggregatorHelper.class);

    public static List<NewsFeedInfo> populateDetails(List<NewsFeedInfo> newsFeeds, String userId) {

        if (null == newsFeeds) {
            return null;
        }
        Map<EntityType, Set<SrcEntity>> collectedEntities = collectNewsEntities(newsFeeds);
        if (null != collectedEntities && !collectedEntities.isEmpty()) {
            int total = 0;
            for (Map.Entry<EntityType, Set<SrcEntity>> entry : collectedEntities.entrySet()) {
                LOGGER.debug("collected entities : type=" + entry.getKey() + ", count="
                        + (null != entry.getValue() ? entry.getValue().size() : 0));
                if (null != entry.getValue()) {
                    total = total + entry.getValue().size();
                }
            }
            LOGGER.debug("collected entities : type=ALL, count=" + total);
        } else {
            LOGGER.error("no entities collected");
        }
        Map<SrcEntity, SrcEntity> srcEntityDetails = fetchSrcEntityDetails(collectedEntities,
                userId);
        if (null != srcEntityDetails && !srcEntityDetails.isEmpty()) {
            int detailsFoundCount = 0;
            for (Map.Entry<SrcEntity, SrcEntity> entry : srcEntityDetails.entrySet()) {
                if (null != entry.getValue()) {
                    detailsFoundCount++;
                } else {
                    LOGGER.error("no details found for SrcEntity : "
                            + new Gson().toJson(entry.getKey()));
                }
            }
            LOGGER.debug("details fetched stats : count=" + srcEntityDetails.size()
                    + ", detailsFoundCount=" + detailsFoundCount);
        } else {
            LOGGER.error("no entities details fetched");
        }

        List<NewsFeedInfo> newsFeedDetails = new ArrayList<NewsFeedInfo>();
        for (NewsFeedInfo newsFeedInfo : newsFeeds) {
            NewsFeedInfo n = populateDetails(srcEntityDetails, newsFeedInfo);
            if (null != n) {
                LOGGER.debug("Adding decorated newsfeed to response set " + n);
                newsFeedDetails.add(n);
            }
        }
        LOGGER.debug("Decorated newsFeedDetails: cout :" + newsFeedDetails.size() + " all "
                + newsFeedDetails);
        return newsFeedDetails;
    }

    private static NewsFeedInfo populateDetails(
            Map<SrcEntity, ? extends SrcEntity> srcEntityDetails, NewsFeedInfo newsFeedInfo) {

        if (null == newsFeedInfo) {
            return null;
        }
        if (null == srcEntityDetails || srcEntityDetails.isEmpty()) {
            return newsFeedInfo;
        }
        if (!srcEntityDetails.containsKey(newsFeedInfo.src)
                || null == srcEntityDetails.get(newsFeedInfo.src)) {
            LOGGER.debug("suppressing news due to missing src : " + new Gson().toJson(newsFeedInfo));
            return null;
        }
        if (!srcEntityDetails.containsKey(newsFeedInfo.actor)
                || null == srcEntityDetails.get(newsFeedInfo.actor)) {
            LOGGER.debug("suppressing news due to missing actor : "
                    + new Gson().toJson(newsFeedInfo));
            return null;
        }
        // create copy
        NewsFeedInfo n = new NewsFeedInfo(newsFeedInfo);

        // details
        if (null != newsFeedInfo.src) {
            n.src = srcEntityDetails.get(newsFeedInfo.src);
            LOGGER.debug("newFeedDetails object is : " + n.src.type);
        }

        if (null != newsFeedInfo.srcOwner) {
            n.srcOwner = srcEntityDetails.get(newsFeedInfo.srcOwner);
        }

        if (null != newsFeedInfo.actor) {
            n.actor = srcEntityDetails.get(newsFeedInfo.actor);
        }

        if (null != newsFeedInfo.sharedWith && !newsFeedInfo.sharedWith.isEmpty()) {

            n.sharedWith = new ArrayList<ShareWithEntity>();
            LOGGER.debug(" checking for sharedwtih entities" + srcEntityDetails);
         
            if (CollectionUtils.isNotEmpty(newsFeedInfo.sharedWith)) {
                for (SrcEntity ne : newsFeedInfo.sharedWith) {
                    if (srcEntityDetails.get(ne) != null) {
                        LOGGER.debug("Updating shared with SrcEntity : " + ne + " with "
                                + srcEntityDetails.get(ne) + srcEntityDetails.get(ne).getClass()
                                + " instanceof" + (ne instanceof ShareWithEntity));

                        LOGGER.debug("Updating SrcEntity : " + ne + " with "
                                + srcEntityDetails.get(ne));
                        if (ne instanceof ShareWithEntity) {
                            n.sharedWith.add((ShareWithEntity) srcEntityDetails.get(ne));
                        } else {
                            n.sharedWith.add(new ShareWithEntity(ne.type, ne.id));
                        }
                    } else {
                        n.sharedWith.add((ShareWithEntity) ne);
                    }
                }

            }
        }

        if (null != newsFeedInfo.involved && !newsFeedInfo.involved.isEmpty()) {
            n.involved = new ArrayList<SrcEntity>();
            for (SrcEntity ne : newsFeedInfo.involved) {
                n.involved.add(srcEntityDetails.get(ne));
            }
        }

        return n;
    }

    private static Map<EntityType, Set<SrcEntity>>
            collectNewsEntities(List<NewsFeedInfo> newsFeeds) {

        Map<EntityType, Set<SrcEntity>> collectedEntities = new HashMap<EntityType, Set<SrcEntity>>();
        for (NewsFeedInfo n : newsFeeds) {
            LOGGER.debug("src for SrcEntity is : " + new Gson().toJson(n.src));
            collectSrcEntity(collectedEntities, n.src);
            collectSrcEntity(collectedEntities, n.srcOwner);
            collectSrcEntity(collectedEntities, n.actor);
            LOGGER.debug("Shared with Entitites:" + n.sharedWith);
            collectSrcEntity(collectedEntities, n.sharedWith);
            collectSrcEntity(collectedEntities, n.involved);

        }
        return collectedEntities;
    }

    private static void collectSrcEntity(Map<EntityType, Set<SrcEntity>> collectedEntities,
            List<? extends SrcEntity> newsEntities) {

        if (null == newsEntities || newsEntities.isEmpty()) {
            return;
        }
        LOGGER.info("NewsEntities Count: " + newsEntities.size());
        for (SrcEntity SrcEntity : newsEntities) {
            collectSrcEntity(collectedEntities, SrcEntity);
        }
    }

    private static void collectSrcEntity(Map<EntityType, Set<SrcEntity>> collectedEntities,
            SrcEntity SrcEntity) {

        if (null == SrcEntity) {
            return;
        }
        LOGGER.info("SrcEntity is instanceof of  " + SrcEntity.getClass());
        EntityType type = SrcEntity.type;
        // if (SrcEntity instanceof CommentSrcEntity) {
        // CommentSrcEntity commentSrcEntity = (CommentSrcEntity) SrcEntity;
        // collectSrcEntity(collectedEntities, commentSrcEntity.pDoc);
        // if (null != commentSrcEntity.pCommId) {
        // collectSrcEntity(collectedEntities, commentSrcEntity.pCommBy);
        // collectSrcEntity(collectedEntities, new CommentSrcEntity(
        // commentSrcEntity.id, commentSrcEntity.type, null,
        // null, commentSrcEntity.pCommId, null));
        // }
        // if (null != commentSrcEntity.commId) {
        // collectSrcEntity(collectedEntities, new SrcEntity(
        // commentSrcEntity.id, commentSrcEntity.type));
        // SrcEntity = new CommentSrcEntity(commentSrcEntity.id,
        // commentSrcEntity.type, null, null,
        // commentSrcEntity.commId, null);
        // type = EntityType.COMMENT;
        // }
        // }
        if (!collectedEntities.containsKey(type)) {
            collectedEntities.put(type, new HashSet<SrcEntity>());
        }
        if (!collectedEntities.get(type).contains(SrcEntity)) {
            collectedEntities.get(type).add(SrcEntity);
            LOGGER.debug("collected entity : type=" + type + ", SrcEntity="
                    + new Gson().toJson(SrcEntity));
        } else {
            LOGGER.info("ignoring earlier collected entity : type=" + type + ", SrcEntity="
                    + new Gson().toJson(SrcEntity));
        }
    }

    private static Map<SrcEntity, SrcEntity> fetchSrcEntityDetails(
            Map<EntityType, Set<SrcEntity>> collectedEntities, String userId) {

        Map<SrcEntity, SrcEntity> srcEntityDetails = new HashMap<SrcEntity, SrcEntity>();
        if (collectedEntities == null) {
            return srcEntityDetails;
        }
        for (Map.Entry<EntityType, Set<SrcEntity>> e : collectedEntities.entrySet()) {
            // TODO: verify this
            // if (EntityType.COMMENT == e.getKey()) {
            // // delaying processing for comments to ensure all other entities
            // // have been obtained
            // continue;
            // }
            Map<SrcEntity, SrcEntity> mappedDetails = fetchSrcEntityDetails(e.getKey(),
                    e.getValue(), userId);
            if (null != mappedDetails && !mappedDetails.isEmpty()) {
                LOGGER.debug(" Adding Decorated entities " + mappedDetails);
                srcEntityDetails.putAll(mappedDetails);
            }
        }
        // // processing for comments at the last
        // if (collectedEntities.containsKey(EntityType.COMMENT)) {
        // LOGGER.info("collectedEntities contains : " + EntityType.COMMENT);
        // Map<SrcEntity, SrcEntity> mappedDetails = fetchSrcEntityDetails(EntityType.COMMENT,
        // collectedEntities.get(EntityType.COMMENT), userId);
        // if (null != mappedDetails && !mappedDetails.isEmpty()) {
        // LOGGER.info(" Adding Decorated entities " + mappedDetails);
        // srcEntityDetails.putAll(mappedDetails);
        //
        // }
        // }
        return srcEntityDetails;
    }

    private static Map<SrcEntity, SrcEntity> fetchSrcEntityDetails(EntityType entityType,
            Set<SrcEntity> newsEntities, String userId) {

        LOGGER.debug("fetching news entity details for entities of type=" + entityType
                + ", newsEntities.size=" + (null != newsEntities ? newsEntities.size() : 0));

        String orgId = null;
        if (NewsFeedSecurityVaildator.get() != null) {
            orgId = NewsFeedSecurityVaildator.get().getOrgId();
        }

        Map<SrcEntity, SrcEntity> srcEntityNewsDetails = new HashMap<SrcEntity, SrcEntity>();
        IPopulator populator = EntityDetailsPopulatorFactory.INSTANCE.get(entityType);

        if (populator != null) {
            populator.populate(orgId, userId, newsEntities, srcEntityNewsDetails, entityType);
        }

        LOGGER.debug("fetched news entity details for entities of type=" + entityType
                + ", newsEntities.size=" + (null != newsEntities ? newsEntities.size() : 0)
                + ", SrcEntityDetails.size="
                + (null != srcEntityNewsDetails ? srcEntityNewsDetails.size() : 0));
        return srcEntityNewsDetails;
    }
}

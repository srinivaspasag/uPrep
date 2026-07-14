package com.lms.managers;

import com.google.gson.Gson;
import com.lms.common.ShareWithEntity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.managers.news.NewsFeedSecurityVaildator;
import com.lms.pojos.news.NewsFeedInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NewsAggregatorHelper {

    private static final Logger logger = LoggerFactory.getLogger(NewsAggregatorHelper.class);

    public static List<NewsFeedInfo> populateDetails(List<NewsFeedInfo> newsFeeds, String userId) {

        if (null == newsFeeds) {
            return null;
        }
        Map<EntityType, Set<SrcEntity>> collectedEntities = collectNewsEntities(newsFeeds);
        if (null != collectedEntities && !collectedEntities.isEmpty()) {
            int total = 0;
            for (Map.Entry<EntityType, Set<SrcEntity>> entry : collectedEntities.entrySet()) {
                logger.debug("collected entities : type=" + entry.getKey() + ", count="
                        + (null != entry.getValue() ? entry.getValue().size() : 0));
                if (null != entry.getValue()) {
                    total = total + entry.getValue().size();
                }
            }
            logger.debug("collected entities : type=ALL, count=" + total);
        } else {
            logger.error("no entities collected");
        }
        Map<SrcEntity, SrcEntity> srcEntityDetails = fetchSrcEntityDetails(collectedEntities,
                userId);
        if (null != srcEntityDetails && !srcEntityDetails.isEmpty()) {
            int detailsFoundCount = 0;
            for (Map.Entry<SrcEntity, SrcEntity> entry : srcEntityDetails.entrySet()) {
                if (null != entry.getValue()) {
                    detailsFoundCount++;
                } else {
                    logger.error("no details found for SrcEntity : "
                            + new Gson().toJson(entry.getKey()));
                }
            }
            logger.debug("details fetched stats : count=" + srcEntityDetails.size()
                    + ", detailsFoundCount=" + detailsFoundCount);
        } else {
            logger.error("no entities details fetched");
        }

        List<NewsFeedInfo> newsFeedDetails = new ArrayList<NewsFeedInfo>();
        for (NewsFeedInfo newsFeedInfo : newsFeeds) {
            NewsFeedInfo n = populateDetails(srcEntityDetails, newsFeedInfo);
            if (null != n) {
                logger.debug("Adding decorated newsfeed to response set " + n);
                newsFeedDetails.add(n);
            }
        }
        logger.debug("Decorated newsFeedDetails: cout :" + newsFeedDetails.size() + " all "
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
            logger.debug("suppressing news due to missing src : " + new Gson().toJson(newsFeedInfo));
            return null;
        }
        if (!srcEntityDetails.containsKey(newsFeedInfo.actor)
                || null == srcEntityDetails.get(newsFeedInfo.actor)) {
            logger.debug("suppressing news due to missing actor : "
                    + new Gson().toJson(newsFeedInfo));
            return null;
        }
        // create copy
        NewsFeedInfo n = new NewsFeedInfo(newsFeedInfo);

        // details
        if (null != newsFeedInfo.src) {
            n.src = srcEntityDetails.get(newsFeedInfo.src);
            logger.debug("newFeedDetails object is : " + n.src.type);
        }

        if (null != newsFeedInfo.srcOwner) {
            n.srcOwner = srcEntityDetails.get(newsFeedInfo.srcOwner);
        }

        if (null != newsFeedInfo.actor) {
            n.actor = srcEntityDetails.get(newsFeedInfo.actor);
        }

        if (null != newsFeedInfo.sharedWith && !newsFeedInfo.sharedWith.isEmpty()) {

            n.sharedWith = new ArrayList<ShareWithEntity>();
            logger.debug(" checking for sharedwtih entities" + srcEntityDetails);

            if (CollectionUtils.isNotEmpty(newsFeedInfo.sharedWith)) {
                for (SrcEntity ne : newsFeedInfo.sharedWith) {
                    if (srcEntityDetails.get(ne) != null) {
                        logger.debug("Updating shared with SrcEntity : " + ne + " with "
                                + srcEntityDetails.get(ne) + srcEntityDetails.get(ne).getClass()
                                + " instanceof" + (ne instanceof ShareWithEntity));

                        logger.debug("Updating SrcEntity : " + ne + " with "
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
            logger.debug("src for SrcEntity is : " + new Gson().toJson(n.src));
            collectSrcEntity(collectedEntities, n.src);
            collectSrcEntity(collectedEntities, n.srcOwner);
            collectSrcEntity(collectedEntities, n.actor);
            logger.debug("Shared with Entitites:" + n.sharedWith);
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
        logger.info("NewsEntities Count: " + newsEntities.size());
        for (SrcEntity SrcEntity : newsEntities) {
            collectSrcEntity(collectedEntities, SrcEntity);
        }
    }

    private static void collectSrcEntity(Map<EntityType, Set<SrcEntity>> collectedEntities,
                                         SrcEntity SrcEntity) {

        if (null == SrcEntity) {
            return;
        }
        logger.info("SrcEntity is instanceof of  " + SrcEntity.getClass());
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
            logger.debug("collected entity : type=" + type + ", SrcEntity="
                    + new Gson().toJson(SrcEntity));
        } else {
            logger.info("ignoring earlier collected entity : type=" + type + ", SrcEntity="
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
                logger.debug(" Adding Decorated entities " + mappedDetails);
                srcEntityDetails.putAll(mappedDetails);
            }
        }
        // // processing for comments at the last
        // if (collectedEntities.containsKey(EntityType.COMMENT)) {
        // logger.info("collectedEntities contains : " + EntityType.COMMENT);
        // Map<SrcEntity, SrcEntity> mappedDetails = fetchSrcEntityDetails(EntityType.COMMENT,
        // collectedEntities.get(EntityType.COMMENT), userId);
        // if (null != mappedDetails && !mappedDetails.isEmpty()) {
        // logger.info(" Adding Decorated entities " + mappedDetails);
        // srcEntityDetails.putAll(mappedDetails);
        //
        // }
        // }
        return srcEntityDetails;
    }

    private static Map<SrcEntity, SrcEntity> fetchSrcEntityDetails(EntityType entityType,
                                                                   Set<SrcEntity> newsEntities, String userId) {

        logger.debug("fetching news entity details for entities of type=" + entityType
                + ", newsEntities.size=" + (null != newsEntities ? newsEntities.size() : 0));

        String orgId = null;
        if (NewsFeedSecurityVaildator.get() != null) {
            orgId = NewsFeedSecurityVaildator.get().getOrgId();
        }

        Map<SrcEntity, SrcEntity> srcEntityNewsDetails = new HashMap<SrcEntity, SrcEntity>();
       /* IPopulator populator = EntityDetailsPopulatorFactory.INSTANCE.get(entityType);

        if (populator != null) {
            populator.populate(orgId, userId, newsEntities, srcEntityNewsDetails, entityType);
        }*/

        logger.debug("fetched news entity details for entities of type=" + entityType
                + ", newsEntities.size=" + (null != newsEntities ? newsEntities.size() : 0)
                + ", SrcEntityDetails.size="
                + (null != srcEntityNewsDetails ? srcEntityNewsDetails.size() : 0));
        return srcEntityNewsDetails;
    }
}


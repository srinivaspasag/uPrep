package com.lms.clusters;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.managers.news.NewsFeedSecurityVaildator;
import com.lms.pojos.news.IClusterable;
import com.lms.pojos.news.NewsFeedInfo;
import com.lms.pojos.news.clustering.SrcNewsEntityClusterKey;
import com.lms.pojos.news.details.CommentNewsEntityDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class NewsClusterManager {

    private static final Logger logger = LoggerFactory.getLogger(NewsClusterManager.class);

    public static boolean getClusteredNewsFeed(Map<IClusterable, NewsFeedCluster> feedMap,
                                               List<NewsFeedInfo> newsFeeds, final int maxClusterCount) {

        logger.debug(" Clustering now ");

        SrcEntity keyNewsEntity = null;

        for (NewsFeedInfo newsFeedInfo : newsFeeds) {
            IClusterable key = null;
            keyNewsEntity = newsFeedInfo.src;
            switch (NewsFeedSecurityVaildator.get().contextType) {
                case NEWSFEED:
                case NOTIFICATION:
                case ACIVITY_FEEDS:

                    if (newsFeedInfo.src.type == EntityType.COMMENT) {
                        keyNewsEntity = getSrcEntityForComment(newsFeedInfo);
                    }
                    key = new SrcNewsEntityClusterKey(keyNewsEntity, newsFeedInfo.eType,
                            newsFeedInfo.time);
                    break;
                // case ACIVITY_FEEDS:
                // keyNewsEntity = newsFeedInfo.actor;
                // // clustering based on user actiontype, entity type
                // key = new ActorNewsEntityClusterKey(newsFeedInfo.src.type, newsFeedInfo.eType,
                // newsFeedInfo.time);
                // break;
                default:
                    break;

            }

            NewsFeedCluster newsCluster = feedMap.get(key);
            if (newsCluster != null && feedMap.keySet().size() < maxClusterCount) {
                logger.info("Key found: " + key.toString() + " For " + newsFeedInfo);
                feedMap.get(key).addNewsFeed(newsFeedInfo);
            } else {
                logger.info("Key Added: " + key.toString() + " For " + newsFeedInfo);
                newsCluster = new NewsFeedCluster(keyNewsEntity, newsFeedInfo.eType);
                newsCluster.addNewsFeed(newsFeedInfo);
                feedMap.put(key, newsCluster);

            }
            if (feedMap.keySet().size() >= maxClusterCount) {
                return false;
            }

        }
        return true;

    }

    public static SrcEntity getSrcEntityForComment(NewsFeedInfo newsFeedInfo) {

        CommentNewsEntityDetails commentDetails = (CommentNewsEntityDetails) newsFeedInfo.src;
        if (newsFeedInfo.why != null) {
            switch (newsFeedInfo.why) {
                case ROOT_OWNER:
                    return new SrcEntity(commentDetails.rootDetails.type, commentDetails.rootDetails.id);
                default:
                    break;

            }
        }
        return new SrcEntity(commentDetails.type, commentDetails.id);
    }
}

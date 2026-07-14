package com.vedantu.comm.news.cluster;

import java.util.List;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.news.details.CommentNewsEntityDetails;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.comm.pojos.news.clustering.IClusterable;
import com.vedantu.comm.pojos.news.clustering.SrcNewsEntityClusterKey;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;

public class NewsClusterManager {

    private final static ALogger LOGGER = Logger.of(NewsClusterManager.class);

    public static boolean getClusteredNewsFeed(Map<IClusterable, NewsFeedCluster> feedMap,
            List<NewsFeedInfo> newsFeeds, final int maxClusterCount) {

        LOGGER.debug(" Clustering now ");

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
                LOGGER.info("Key found: " + key.toString() + " For " + newsFeedInfo);
                feedMap.get(key).addNewsFeed(newsFeedInfo);
            } else {
                LOGGER.info("Key Added: " + key.toString() + " For " + newsFeedInfo);
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

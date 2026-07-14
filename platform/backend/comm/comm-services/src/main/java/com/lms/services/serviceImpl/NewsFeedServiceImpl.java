package com.lms.services.serviceImpl;


import com.lms.clusters.ClusterComparator;
import com.lms.clusters.NewsClusterManager;
import com.lms.clusters.NewsFeedCluster;
import com.lms.common.utils.ListUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.components.NewsFeedManager;
import com.lms.enums.NewsContext;
import com.lms.managers.news.*;
import com.lms.pojos.news.IClusterable;
import com.lms.pojos.news.NewsFeedInfo;
import com.lms.pojos.requests.GetNewsFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetActivityFeedsReq;
import com.lms.pojos.requests.newsfeeds.GetNewsFeedsRes;
import com.lms.pojos.requests.newsfeeds.GetOlderActivityFeedsReq;
import com.lms.services.NewsfeedService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class NewsFeedServiceImpl implements NewsfeedService {
    private static final Logger logger = LoggerFactory.getLogger(NewsFeedServiceImpl.class);
    private static final int DEFAULT_COUNT = 5;
    @Value("${activityfeeds.default.count}")
    private String count;
    @Autowired
    private NewsFeedManager newsFeedManager;

    @Override
    public VedantuResponse getOlderActivityFeeds(GetOlderActivityFeedsReq request) {
        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(count);
            } catch (Exception e) {
                logger.error(
                        "config property activityfeeds.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }

        }
        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NEWSFEED);
        if (CollectionUtils.isNotEmpty(request.userActions)) {
            logger.info("Filters are provided ");
            FilterManager filterManager = new FilterManager();
            List<UserActionType> userActionTypes = new ArrayList<UserActionType>();
            for (String userAction : request.userActions) {
                userActionTypes.add(UserActionType.valueOfKey(userAction));
            }

            UserActionTypeFilter eventTypeFilter = new UserActionTypeFilter(userActionTypes);
            filterManager.addFilter(eventTypeFilter);
            FilterManagerStore.set(filterManager);

        }


        List<NewsFeedInfo> newsFeeds = getOlderActivityFeeds(
                request.eType, request.eId, request.beforeNewsActivityId, request.size,
                request.userId);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            response.firstId = ListUtils.getFirst(newsFeeds).newsFeedId;

            if (request.needClustered) {
                logger.debug("clustering-operation started" + System.currentTimeMillis());
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusterdActivityFeeds(
                        request.userId, request.eType, request.eId, newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
//                response.list.addAll((Collection<? extends IListResponseObj>) clusterList);
                response.lastId = ListUtils.getLast(clusterList).lastNewsFeedId;
                response.totalHits = clusterList.size();
                logger.debug("clustering-operation finished" + System.currentTimeMillis());
            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = ListUtils.getLast(newsFeeds).newsFeedId;
            }
        }
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getNewsFeeds(GetNewsFeedsReq request) {
        GetNewsFeedsRes response = new GetNewsFeedsRes();


        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NEWSFEED);
        List<NewsFeedInfo> newsFeeds = newsFeedManager.getNewsFeeds(request.userId, request.size);

        if (request.needClustered) {
            logger.debug("clustering-operation started" + System.currentTimeMillis());
            Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusteredNewsFeeds(newsFeeds,
                    newsFeeds.size());
            List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
            Collections.sort(clusterList, ClusterComparator.INSTANCE);
            response.list.addAll(clusterList);
            response.totalHits = clusterList.size();
            logger.debug("clustering-operation finished" + System.currentTimeMillis());
        } else {

            response.list.addAll(newsFeeds);
            response.totalHits = newsFeeds.size();
        }

        return new VedantuResponse(response);


    }


    // Note : Haddop implementations are pending in the below method, are commented
    public List<NewsFeedInfo> getOlderActivityFeeds(EntityType entityType, String entityId,
                                                    String beforeNewsActivityId, int count, String requesterId) {


        logger.debug("Fetching old activities before" + beforeNewsActivityId);
//        long fromTime = HBaseUtil.getTimeValue(beforeNewsActivityId);
        // System.out.println("fromTime : " + fromTime);
        if (EntityType.USER == entityType) {
            if (!StringUtils.isEmpty(requesterId) && requesterId.equals(entityId)) {
                NewsFeedSecurityVaildator.get().setNeedAuthorization(false);
                NewsFeedSecurityVaildator.get().setContextType(NewsContext.ACIVITY_FEEDS);
            }

//                return getActivityFeedsFromTime(entityType, entityId, fromTime, count,
//                        beforeNewsActivityId)
        } else {
//                return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
//                        entityType, entityId, fromTime, count, beforeNewsActivityId, false, false);
        }
        return null;
    }

    private Map<IClusterable, NewsFeedCluster> getClusterdActivityFeeds(String requesterId,
                                                                        EntityType entityType, String entityId, List<NewsFeedInfo> newsFeeds, int count) {

        logger.debug("Clustering for count" + count);

        Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable, NewsFeedCluster>();

        while (feedMap.size() < count && CollectionUtils.isNotEmpty(newsFeeds)) {
            boolean exhaustedNewsfeeds = NewsClusterManager.getClusteredNewsFeed(feedMap,
                    newsFeeds, count);
            if (exhaustedNewsfeeds) {

                newsFeeds = getOlderActivityFeeds(entityType,
                        entityId, newsFeeds.get(newsFeeds.size() - 1).newsFeedId,
                        count - feedMap.size(), requesterId);
            }
            logger.debug("Found more news feeds" + newsFeeds.size());

        }

        return feedMap;
    }

    private Map<IClusterable, NewsFeedCluster> getClusteredNewsFeeds(
            List<NewsFeedInfo> newsFeeds, int count) {

        Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable, NewsFeedCluster>();

        while (feedMap.size() < count && CollectionUtils.isNotEmpty(newsFeeds)) {
            boolean exhaustedNewsFeeds = newsFeedManager.getClusteredNewsFeed(feedMap,
                    newsFeeds, count);

            if (exhaustedNewsFeeds) {

                newsFeeds = newsFeedManager.getOlderNewsFeeds(
                        NewsFeedSecurityVaildator.get().getUserId(),
                        newsFeeds.get(newsFeeds.size() - 1).newsFeedId, count - feedMap.size());
            }
        }
        System.gc();
        return feedMap;
    }

    @Override
    public VedantuResponse getActivityFeeds(GetActivityFeedsReq request) {
        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(count);
            } catch (Exception e) {
                logger.error(
                        "config property activityfeeds.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }
        }
        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);

        if (CollectionUtils.isNotEmpty(request.userActions)) {
            logger.info("Filters are provided ");
            FilterManager filterManager = new FilterManager();
            List<UserActionType> userActionTypes = new ArrayList<UserActionType>();
            for (String userAction : request.userActions) {
                userActionTypes.add(UserActionType.valueOfKey(userAction));
            }

            UserActionTypeFilter eventTypeFilter = new UserActionTypeFilter(userActionTypes);
            filterManager.addFilter(eventTypeFilter);
            FilterManagerStore.set(filterManager);

        }

        List<NewsFeedInfo> newsFeeds = newsFeedManager.getActivityFeeds(request.eType,
                request.eId, request.size, request.userId);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            response.firstId = ListUtils.getFirst(newsFeeds).newsFeedId;

            if (request.needClustered) {
                logger.debug("clustering-operation started" + System.currentTimeMillis());
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusterdActivityFeeds(
                        request.userId, request.eType, request.eId, newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
                response.list.addAll(clusterList);
                response.totalHits = clusterList.size();
                response.lastId = ListUtils.getLast(clusterList).lastNewsFeedId;
                logger.debug("clustering-operation finished" + System.currentTimeMillis());
            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = ListUtils.getLast(newsFeeds).newsFeedId;
            }
        }

        return new VedantuResponse(response);

    }

}






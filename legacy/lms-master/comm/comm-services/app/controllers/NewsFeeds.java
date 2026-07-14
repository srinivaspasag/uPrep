package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.comm.enums.NewsContext;
import com.vedantu.comm.managers.news.FilterManager;
import com.vedantu.comm.managers.news.FilterManagerStore;
import com.vedantu.comm.managers.news.NewsAggregator;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.UserActionTypeFilter;
import com.vedantu.comm.managers.news.UserSecuritySet;
import com.vedantu.comm.news.cluster.ClusterComparator;
import com.vedantu.comm.news.cluster.NewsClusterManager;
import com.vedantu.comm.news.cluster.NewsFeedCluster;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.comm.pojos.news.clustering.IClusterable;
import com.vedantu.comm.requests.newsfeeed.GetActivityFeedsReq;
import com.vedantu.comm.requests.newsfeeed.GetNewsFeedReq;
import com.vedantu.comm.requests.newsfeeed.GetNewsFeedsReq;
import com.vedantu.comm.requests.newsfeeed.GetNotificationSummaryReq;
import com.vedantu.comm.requests.newsfeeed.GetNotificationsReq;
import com.vedantu.comm.requests.newsfeeed.GetOlderActivityFeedsReq;
import com.vedantu.comm.requests.newsfeeed.GetOlderNotificationsReq;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.pojos.response.newsfeed.GetNewsFeedRes;
import com.vedantu.pojos.response.newsfeed.GetNewsFeedsRes;
import com.vedantu.pojos.response.newsfeed.GetNotificationSummaryRes;

public class NewsFeeds extends AbstractVedantuController {

    private final static ALogger LOGGER        = Logger.of(NewsFeeds.class);
    private static final int     DEFAULT_COUNT = 5;

    // private static final NewsEntitySerializer newsEntitySerializer = new
    // NewsEntitySerializer();

    public static Result getNewsFeeds() {

        GetNewsFeedsReq request = null;
        GetNewsFeedsRes response = new GetNewsFeedsRes();

        Form<GetNewsFeedsReq> requestForm = Form.form(GetNewsFeedsReq.class).bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        request = requestForm.get();

        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("newsfeeds.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property activityfeeds.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }
        }

        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NEWSFEED);
        List<NewsFeedInfo> newsFeeds = NewsAggregator.getInstance().getNewsFeeds(request.userId,
                request.size);

        if (request.needClustered) {
            LOGGER.debug("clustering-operation started"+ System.currentTimeMillis());
            Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusteredNewsFeeds(newsFeeds,
                    newsFeeds.size());
            List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
            Collections.sort(clusterList, ClusterComparator.INSTANCE);
            response.list.addAll(clusterList);
            response.totalHits = clusterList.size();
            LOGGER.debug("clustering-operation finished"+ System.currentTimeMillis());
        } else {

            response.list.addAll(newsFeeds);
            response.totalHits = newsFeeds.size();
        }

        return ok(getResultResponse(response).toObjectNode());

    }

    private static Map<IClusterable, NewsFeedCluster> getClusteredNewsFeeds(
            List<NewsFeedInfo> newsFeeds, int count) {

        Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable, NewsFeedCluster>();

        while (feedMap.size() < count && CollectionUtils.isNotEmpty(newsFeeds)) {
            boolean exhaustedNewsFeeds = NewsClusterManager.getClusteredNewsFeed(feedMap,
                    newsFeeds, count);

            if (exhaustedNewsFeeds) {

                newsFeeds = NewsAggregator.getInstance().getOlderNewsFeeds(
                        NewsFeedSecurityVaildator.get().getUserId(),
                        newsFeeds.get(newsFeeds.size() - 1).newsFeedId, count - feedMap.size());
            }
        }
        System.gc();
        return feedMap;
    }

    //
    // public static void getOlderNewsFeeds(@Required String userId,
    // @Required String beforeNewsFeedId, int count, boolean needClustered) {
    // if (validation.hasError("userId")) {
    // renderJSON(generateErrorResponse(ErrorCode.INVALID_USERID));
    // }
    // if (validation.hasError("beforeNewsFeedId")
    // || !HBaseUtil.isValidRowId(beforeNewsFeedId)) {
    // renderJSON(generateErrorResponse(ErrorCode.INVALID_BEFORENEWSFEEDID));
    // }
    // if (count < 0) {
    // renderJSON(generateErrorResponse(ErrorCode.INVALID_COUNT));
    // }
    // if (count == 0) {
    // try {
    // count = Integer.parseInt(Play.configuration
    // .getProperty("newsfeeds.default.count"));
    // } catch (Exception e) {
    // Logger.log4j
    // .error("config property newsfeeds.default.count not found in application.conf file",
    // e);
    // count = DEFAULT_COUNT;
    // }
    // }
    // NewsFeedInfos newsFeeds = NewsAggregator.getInstance()
    // .getOlderNewsFeeds(userId, beforeNewsFeedId, count);
    // JSONResponse r = null;
    //
    // if (needClustered) {
    // Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable,
    // NewsFeedCluster>();
    // while (feedMap.size() < count && newsFeeds.newsFeedCount > 0) {
    // NewsAggregator.getInstance().getClusteredNewsFeed(feedMap,
    // newsFeeds.newsFeeds, false);
    //
    // newsFeeds = NewsAggregator.getInstance()
    // .getOlderNewsFeeds(
    // userId,
    // newsFeeds.newsFeeds.get(newsFeeds.newsFeeds
    // .size() - 1).newsFeedId, count);
    // }
    // r = new JSONResponse(feedMap.values());
    // } else {
    // r = new JSONResponse(newsFeeds);
    // }
    // renderJSON(r, newsEntitySerializer);
    // }
    //
    // public static void getNewNewsFeeds(@Required String userId,
    // boolean needClustered) {
    // if (validation.hasError("userId")) {
    // renderJSON(generateErrorResponse(ErrorCode.INVALID_USERID));
    // }
    // NewsFeedInfos newsFeeds = NewsAggregator.getInstance().getNewNewsFeeds(
    // userId);
    // JSONResponse r = null;
    // if (needClustered) {
    // Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable,
    // NewsFeedCluster>();
    // NewsAggregator.getInstance().getClusteredNewsFeed(feedMap,
    // newsFeeds.newsFeeds, false);
    // r = new JSONResponse(feedMap.values());
    // } else {
    // r = new JSONResponse(newsFeeds);
    // }
    // renderJSON(r, newsEntitySerializer);
    // }
    //
    public static Result getActivityFeeds() {

        Form<GetActivityFeedsReq> requestForm = Form.form(GetActivityFeedsReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetActivityFeedsReq request = requestForm.get();
        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("activityfeeds.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property activityfeeds.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }
        }
        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);

        if (CollectionUtils.isNotEmpty(request.userActions)) {
            Logger.info("Filters are provided ");
            FilterManager filterManager = new FilterManager();
            List<UserActionType> userActionTypes = new ArrayList<UserActionType>();
            for (String userAction : request.userActions) {
                userActionTypes.add(UserActionType.valueOfKey(userAction));
            }

            UserActionTypeFilter eventTypeFilter = new UserActionTypeFilter(userActionTypes);
            filterManager.addFilter(eventTypeFilter);
            FilterManagerStore.set(filterManager);

        }

        List<NewsFeedInfo> newsFeeds = NewsAggregator.getInstance().getActivityFeeds(request.eType,
                request.eId, request.size, request.userId);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            response.firstId = com.vedantu.commons.utils.ListUtils.getFirst(newsFeeds).newsFeedId;

            if (request.needClustered) {
                LOGGER.debug("clustering-operation started"+ System.currentTimeMillis());
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusterdActivityFeeds(
                        request.userId, request.eType, request.eId, newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
                response.list.addAll(clusterList);
                response.totalHits = clusterList.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(clusterList).lastNewsFeedId;
                LOGGER.debug("clustering-operation finished"+ System.currentTimeMillis());
            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(newsFeeds).newsFeedId;
            }
        }

        return ok(getResultResponse(response).toObjectNode());

    }

    //
    public static Result getOlderActivityFeeds() {

        Form<GetOlderActivityFeedsReq> requestForm = Form.form(GetOlderActivityFeedsReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());
        GetOlderActivityFeedsReq request = requestForm.get();

        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (requestForm.hasErrors() || request.validate() != null) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("activityfeeds.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property activityfeeds.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }
        }

        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(true);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NEWSFEED);
        if (CollectionUtils.isNotEmpty(request.userActions)) {
            Logger.info("Filters are provided ");
            FilterManager filterManager = new FilterManager();
            List<UserActionType> userActionTypes = new ArrayList<UserActionType>();
            for (String userAction : request.userActions) {
                userActionTypes.add(UserActionType.valueOfKey(userAction));
            }

            UserActionTypeFilter eventTypeFilter = new UserActionTypeFilter(userActionTypes);
            filterManager.addFilter(eventTypeFilter);
            FilterManagerStore.set(filterManager);

        }

        List<NewsFeedInfo> newsFeeds = NewsAggregator.getInstance().getOlderActivityFeeds(
                request.eType, request.eId, request.beforeNewsActivityId, request.size,
                request.userId);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            response.firstId = com.vedantu.commons.utils.ListUtils.getFirst(newsFeeds).newsFeedId;

            if (request.needClustered) {
                LOGGER.debug("clustering-operation started"+ System.currentTimeMillis());
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusterdActivityFeeds(
                        request.userId, request.eType, request.eId, newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
                response.list.addAll(clusterList);
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(clusterList).lastNewsFeedId;
                response.totalHits = clusterList.size();
                LOGGER.debug("clustering-operation finished"+ System.currentTimeMillis());
            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(newsFeeds).newsFeedId;
            }
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    private static Map<IClusterable, NewsFeedCluster> getClusterdActivityFeeds(String requesterId,
            EntityType entityType, String entityId, List<NewsFeedInfo> newsFeeds, int count) {

        LOGGER.debug("Clustering for count" + count);

        Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable, NewsFeedCluster>();

        while (feedMap.size() < count && CollectionUtils.isNotEmpty(newsFeeds)) {
            boolean exhaustedNewsfeeds = NewsClusterManager.getClusteredNewsFeed(feedMap,
                    newsFeeds, count);
            if (exhaustedNewsfeeds) {

                newsFeeds = NewsAggregator.getInstance().getOlderActivityFeeds(entityType,
                        entityId, newsFeeds.get(newsFeeds.size() - 1).newsFeedId,
                        count - feedMap.size(), requesterId);
            }
            LOGGER.debug("Found more news feeds" + newsFeeds.size());

        }

        return feedMap;
    }

    //
    public static Result getNotifications() {

        Form<GetNotificationsReq> requestForm = Form.form(GetNotificationsReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());
        GetNotificationsReq request = requestForm.get();

        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(false);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NOTIFICATION);
        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (requestForm.hasErrors() || request.validate() != null) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("notifications.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property notifications.default.count not found in application.conf file",

                        e);
                request.size = DEFAULT_COUNT;
            }
        }

        List<NewsFeedInfo> newsFeeds = NewsAggregator.getInstance().getNotifications(
                request.userId, request.size);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            response.firstId = com.vedantu.commons.utils.ListUtils.getFirst(newsFeeds).newsFeedId;
            if (request.needClustered) {
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusteredNotifications(
                        newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
                response.list.addAll(clusterList);
                response.totalHits = clusterList.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(clusterList).lastNewsFeedId;
            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(newsFeeds).newsFeedId;
            }
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getOlderNotifications() {

        Form<GetOlderNotificationsReq> requestForm = Form.form(GetOlderNotificationsReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());
        GetOlderNotificationsReq request = requestForm.get();
        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));
        NewsFeedSecurityVaildator.get().setNeedAuthorization(false);
        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NOTIFICATION);

        GetNewsFeedsRes response = new GetNewsFeedsRes();
        if (requestForm.hasErrors() || request.validate() != null) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("notifications.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property notifications.default.count not found in application.conf file",
                        e);
                request.size = DEFAULT_COUNT;
            }
        }

        List<NewsFeedInfo> newsFeeds = NewsAggregator.getInstance().getOlderNotifications(
                request.userId, request.beforeNotificationId, request.size);
        if (CollectionUtils.isNotEmpty(newsFeeds)) {
            if (request.needClustered) {
                LOGGER.debug("clustering-operation started"+ System.currentTimeMillis());
                Map<IClusterable, NewsFeedCluster> newsFeedClusters = getClusteredNotifications(
                        newsFeeds, request.size);
                List<NewsFeedCluster> clusterList = new ArrayList(newsFeedClusters.values());
                Collections.sort(clusterList, ClusterComparator.INSTANCE);
                response.list.addAll(clusterList);
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(clusterList).lastNewsFeedId;
                response.totalHits = clusterList.size();
                LOGGER.debug("clustering-operation finished"+ System.currentTimeMillis());

            } else {

                response.list.addAll(newsFeeds);
                response.totalHits = newsFeeds.size();
                response.lastId = com.vedantu.commons.utils.ListUtils.getLast(newsFeeds).newsFeedId;
            }

        }
        return ok(getResultResponse(response).toObjectNode());

    }

    private static Map<IClusterable, NewsFeedCluster> getClusteredNotifications(
            List<NewsFeedInfo> newsFeeds, int count) {

        Map<IClusterable, NewsFeedCluster> feedMap = new HashMap<IClusterable, NewsFeedCluster>();
        NewsClusterManager.getClusteredNewsFeed(feedMap, newsFeeds, count);
        while (feedMap.size() < count && CollectionUtils.isNotEmpty(newsFeeds)) {
            boolean exhaustedNewsfeeds = NewsClusterManager.getClusteredNewsFeed(feedMap,
                    newsFeeds, count);
            if (exhaustedNewsfeeds) {

                newsFeeds = NewsAggregator.getInstance().getOlderNotifications(
                        NewsFeedSecurityVaildator.get().getUserId(),
                        newsFeeds.get(newsFeeds.size() - 1).newsFeedId, count - feedMap.size());
            }

        }

        return feedMap;
    }

    public static Result getNotificationsSummary() {

        Form<GetNotificationSummaryReq> requestForm = Form.form(GetNotificationSummaryReq.class)
                .bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());
        GetNotificationSummaryReq request = requestForm.get();
        GetNotificationSummaryRes response = new GetNotificationSummaryRes();
        if (request.size == 0) {
            try {
                request.size = Integer.parseInt(Play.application().configuration()
                        .getString("notifications.default.count"));
            } catch (Exception e) {
                Logger.error(
                        "config property notifications.default.count not found in application.conf file",

                        e);
                request.size = DEFAULT_COUNT;
            }
        }
        NewsFeedSecurityVaildator.set(new UserSecuritySet(request.userId, request.orgId));

        NewsFeedSecurityVaildator.get().setContextType(NewsContext.NOTIFICATION);
        response = NewsAggregator.getInstance().getNotificationsSummary(request.userId,
                request.size);

        return ok(getResultResponse(response).toObjectNode());
    }

    //

    public static Result getNewsFeed() {

        Form<GetNewsFeedReq> requestForm = Form.form(GetNewsFeedReq.class).bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());
        GetNewsFeedReq request = requestForm.get();

        GetNewsFeedRes response = new GetNewsFeedRes();
        if (requestForm.hasErrors() || request.validate() != null) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        try {

            NewsFeedInfo newsFeedInfo = NewsAggregator.getInstance().getNewsFeed(request.userId,
                    request.newsFeedId);

            response.newsFeed = newsFeedInfo;
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

}
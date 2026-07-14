package com.vedantu.comm.managers.news;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vedantu.comm.enums.NewsContext;
import com.vedantu.comm.managers.NewsAggregatorHelper;
import com.vedantu.comm.pojos.news.NewsActivityRef;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.comm.utils.news.NewsUtils;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.hbase.HBaseUtil;
import com.vedantu.commons.news.AbstractInfo;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.pojos.response.newsfeed.GetNotificationSummaryRes;

public class NewsAggregator {

    public static final String          TABLE_NAME_NEWSACTIVITY         = "newsactivity";
    public static final String          TABLE_NAME_NEWSFEED             = "newsfeed";
    public static final String          TABLE_NAME_LASTSEENNEWSFEED     = "lastseennewsfeed";

    public static final String          TABLE_NAME_NOTIFICATION         = "notification";
    public static final String          TABLE_NAME_LASTSEENNOTIFICATION = "lastseennotification";

    private static final ALogger        LOGGER                          = Logger.of(NewsAggregator.class);
    private static final NewsAggregator INSTANCE                        = new NewsAggregator();
    private final static long           maximumLookbackTimeInMS         = Integer
                                                                                .parseInt(Play
                                                                                        .application()
                                                                                        .configuration()
                                                                                        .getString(
                                                                                                "vedantu.newsfeed.lookbackdays"))
                                                                                * DateUtils.MILLIS_PER_DAY; // TODO
                                                                                                            // check
                                                                                                            // this
                                                                                                            // limit

    private static final int            BATCHSIZE                       = 20;

    private final GsonBuilder           gsonBuilder;

    private NewsAggregator() {

        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(AbstractInfo.class, new IInfoDeserializer());
        gsonBuilder.registerTypeAdapter(SrcEntity.class, SrcEntityDeserializer.INSTANCE);
    }

    public static NewsAggregator getInstance() {

        return INSTANCE;
    }

    public List<NewsFeedInfo> getNewsFeeds(String userId, int count) {

        final long now = System.currentTimeMillis();

        return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
                EntityType.USER, userId, now, count, null, true, false);
    }

    public List<NewsFeedInfo> getOlderNewsFeeds(String userId, String beforeNewsFeedId, int count) {

        long fromTime = HBaseUtil.getTimeValue(beforeNewsFeedId);
        return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
                EntityType.USER, userId, fromTime, count, beforeNewsFeedId, false, false);
    }

    private static final int ALL = -1;

    public List<NewsFeedInfo> getNewNewsFeeds(String userId) {

        final long now = System.currentTimeMillis();
        return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
                EntityType.USER, userId, now, ALL, null, true, true);
    }

    private static class Counter {

        int count;
    }

    private List<NewsFeedInfo> getNewsResultsFromTime(final String newsTableName,
            final String lastSeenNewsTableName, EntityType entityType, String entityId,
            long fromTime, int count, String excludeRow, boolean saveLatestNewsResultId,
            boolean onlyTillLastSeen) {

        return getNewsResultsFromTime(newsTableName, lastSeenNewsTableName, entityType, entityId,
                fromTime, count, excludeRow, saveLatestNewsResultId, onlyTillLastSeen,
                (Counter) null);
    }

    private List<NewsFeedInfo> getNewsResultsFromTime(final String newsTableName,
            final String lastSeenNewsTableName, EntityType entityType, String entityId,
            long fromTime, int count, String excludeRow, boolean saveLatestNewsResultId,
            boolean onlyTillLastSeen, Counter counter) {

        List<NewsFeedInfo> newsResults = new ArrayList<NewsFeedInfo>();

        SrcEntity entity = new SrcEntity();
        entity.type = entityType;
        entity.id = entityId;

        String uptillRowId = null;
        if (onlyTillLastSeen) {
            uptillRowId = getLastSeenNewsResult(lastSeenNewsTableName, entity);
            LOGGER.info(lastSeenNewsTableName + " uptillRowId : " + uptillRowId);

        }

        boolean saveFirstRowId = saveLatestNewsResultId;

        boolean fetchMore = true;
        long previous = 0;
        long now = fromTime;
        List<String> feedIds = new ArrayList<String>();
        List<NewsActivityRef> newsActivityRefs = new ArrayList<NewsActivityRef>();
        String previousRowId = null;
        while (fetchMore && (fromTime - now) < maximumLookbackTimeInMS) {
            previous = now - DateUtils.MILLIS_PER_DAY;
            String startRowId = NewsUtils.getRowId(entity, now);
            String stopRowId = NewsUtils.getRowId(entity, previous);

            Scan scan = new Scan();
            scan.addColumn(Bytes.toBytes("act"), Bytes.toBytes("data"));
            String startRowExclusive = ((StringUtils.isNotEmpty(previousRowId)) ? previousRowId
                    : startRowId) + "0";
            scan.setStartRow(startRowExclusive.getBytes());
            scan.setStopRow(Bytes.toBytes(stopRowId));

            PageFilter pageFilter = new PageFilter(count);
            scan.setFilter(pageFilter);
            LOGGER.debug(" Scan between" + startRowExclusive + " and " + stopRowId + " for "
                    + entity.type + " with id " + entity.id + " for count of " + count);
            feedIds.clear();
            newsActivityRefs.clear();
            HTable newsTable = null;
            try {
                newsTable = HBaseUtil.getTable(newsTableName);
                ResultScanner s = newsTable.getScanner(scan);
                // since batch count provide it will fetch only that many rows
                for (Result r : s) {
                    if (count <= 0) {
                        LOGGER.debug("Checked all requested fetched feeds and still left");
                        break;
                    }
                    String rowId = Bytes.toString(r.getRow());

                    if (null != uptillRowId) {
                        if (StringUtils.equals(uptillRowId, rowId)) {
                            fetchMore = false;
                            break;
                        }
                    }
                    if (null != excludeRow) {
                        if (StringUtils.equals(excludeRow, rowId)) {
                            LOGGER.debug(lastSeenNewsTableName + " excluding rowId : " + rowId);
                            // System.out.println(lastSeenNewsTableName +
                            // " excluding rowId : " + rowId);
                            continue;
                        }
                    }

                    if (null != counter) {
                        counter.count++;
                    }

                    String newsActivityRefData = Bytes.toString(r.getValue(Bytes.toBytes("act"),
                            Bytes.toBytes("data")));
                    NewsActivityRef newsActivityRef = new Gson().fromJson(newsActivityRefData,
                            NewsActivityRef.class);

                    LOGGER.debug("Found news activity " + newsActivityRefData + " for row " + rowId);
                    newsActivityRefs.add(newsActivityRef);
                    feedIds.add(rowId);
                    previous = HBaseUtil.getTimeValue(rowId);
                    previousRowId = rowId;
                    LOGGER.debug("Previous value " + previous + "from rowId " + rowId);
                    count--;

                }
            } catch (IOException e) {
                LOGGER.error("could not consume scan " + lastSeenNewsTableName + " - startRowId : "
                        + startRowId + ", stopRowId : " + stopRowId, e);

            } finally {
                HBaseUtil.returnTable(newsTable);
            }

            LOGGER.debug(lastSeenNewsTableName + " fetchmore : " + fetchMore + ", activities : "
                    + newsActivityRefs.size() + " corresponding feedIds size " + feedIds.size()
                    + " , count : " + count);

            if (feedIds.size() > 0) {
                int accepedtedNewsActivityCount = getNewsActivityDetails(newsResults, feedIds,
                        newsActivityRefs);

                LOGGER.debug(" Accepted newsActivity count " + accepedtedNewsActivityCount
                        + "feedIds size : " + feedIds.size() + " current count before addint that "
                        + count);
                count += newsActivityRefs.size() - accepedtedNewsActivityCount;

            }
            fetchMore = fetchMore && (null != counter || count > 0);
            LOGGER.debug(" After acitivity check :" + lastSeenNewsTableName + " fetchmore : "
                    + fetchMore + ", activities : " + newsActivityRefs.size() + ", count : "
                    + count);

            if (saveFirstRowId && !feedIds.isEmpty()) {
                saveLatestNewsResultId(lastSeenNewsTableName, entity, feedIds.get(0));
                saveFirstRowId = false;
            }

            now = previous;
            LOGGER.debug(" fetchMore: " + fetchMore + " fromTime " + fromTime + " now " + now
                    + "  can still look for more past : "
                    + ((fromTime - now) < maximumLookbackTimeInMS));

        }

        LOGGER.debug("Done fetching newsfeeds maximumLookbacTime" + maximumLookbackTimeInMS);

        LOGGER.debug(" Feed count before decorations " + newsResults.size());
        List<NewsFeedInfo> newsFeedDetails = NewsAggregatorHelper.populateDetails(newsResults,
                NewsFeedSecurityVaildator.get().getUserId());

        return newsFeedDetails;
    }

    private int getNewsActivityDetails(List<NewsFeedInfo> newsFeeds, List<String> feedIds,
            List<NewsActivityRef> newsActivityRefs) {

        int startActivityREFsIndex = 0;
        int passedActivityCount = 0;
        LOGGER.debug("Before fetching activities total activity counts requested"
                + newsActivityRefs.size());
        int batchCount = 0;
        List<Get> gets = new ArrayList<Get>();
        int endActivityREFsIndex = 0;
        HTable newsActivityTable = HBaseUtil.getTable(TABLE_NAME_NEWSACTIVITY);
        while (startActivityREFsIndex < newsActivityRefs.size()) {
            LOGGER.debug("StartIndex " + startActivityREFsIndex + "  NewsActivityRefs size "
                    + newsActivityRefs.size());

            endActivityREFsIndex = Math.min(startActivityREFsIndex + BATCHSIZE,
                    newsActivityRefs.size());

            for (int i = startActivityREFsIndex; i < endActivityREFsIndex; i++) {
                Get get = new Get(Bytes.toBytes(newsActivityRefs.get(i).aid));
                gets.add(get);
            }
            if (CollectionUtils.isEmpty(gets)) {
                break;
            }

            LOGGER.debug("Iterations" + batchCount++ + " StartIndexInBatch"
                    + startActivityREFsIndex + " EndIndexInBatch " + endActivityREFsIndex);

            try {

                Result[] results = newsActivityTable.get(gets);

                LOGGER.debug(" Found newsactivity  count in " + TABLE_NAME_NEWSACTIVITY + " "
                        + results.length + " requested size " + gets.size());

                for (Result retrievedActivity : results) {

                    LOGGER.debug(" Iterating startIndex " + startActivityREFsIndex);
                    if (retrievedActivity.isEmpty()) {
                        LOGGER.debug("No result found for : " + retrievedActivity.getRow());
                        startActivityREFsIndex++;
                        continue;
                    }
                    String newsActivityData = Bytes.toString(retrievedActivity.getValue(
                            Bytes.toBytes("act"), Bytes.toBytes("data")));

                    LOGGER.debug("new activity data is : " + newsActivityData);
                    Gson gson = gsonBuilder.create();
                    NewsFeedInfo newsFeedInfo = gson.fromJson(newsActivityData, NewsFeedInfo.class);
                    newsFeedInfo.newsFeedId = feedIds.get(startActivityREFsIndex);
                    newsFeedInfo.newsActivityId = newsActivityRefs.get(startActivityREFsIndex).aid;
                    newsFeedInfo.why = newsActivityRefs.get(startActivityREFsIndex).why;
                    LOGGER.debug("adding newsfeed info to newsFeeds list : " + newsFeedInfo);

                    boolean newsFeedInfoPassed = true;
                    UserSecuritySet securitySet = NewsFeedSecurityVaildator.get();
                    LOGGER.debug("Found security validator ");
                    VedantuBaseMongoModel model = securitySet.checkIfExist(newsFeedInfo.src);
                    if (model == null || (newsFeedInfo.scope != Scope.PUBLIC
                            && (securitySet != null && !securitySet.validate(newsFeedInfo,model)))) {
                        LOGGER.debug("Rejecting feed for security " + newsFeedInfo);
                        newsFeedInfoPassed &= false;
                    }
                    com.vedantu.comm.managers.news.FilterManager filterManager = FilterManagerStore
                            .get();
                    NewsActivity newsActivity = gson.fromJson(newsActivityData, NewsActivity.class);
                    LOGGER.info("Applying filters to newsactivyt  : " + newsActivity);
                    if (filterManager != null && !filterManager.applyFilters(newsActivity)) {
                        LOGGER.debug("Rejecting feed for filter " + newsFeedInfo);
                        newsFeedInfoPassed &= false;

                    }
                    if (newsFeedInfoPassed) {
                        passedActivityCount++;
                        LOGGER.debug(" PassedActivityCount" + passedActivityCount);
                        newsFeeds.add(newsFeedInfo);
                    }
                    startActivityREFsIndex++;
                }
            } catch (IOException e) {
                LOGGER.error("could not get activities", e);
                break;
            }

        }
        HBaseUtil.returnTable(newsActivityTable);
        LOGGER.debug("Accepted newsFeedCount " + passedActivityCount);

        return passedActivityCount;
    }

    private void saveLatestNewsResultId(final String lastSeenNewsTableName, SrcEntity userEntity,
            String latestNewsFeedId) {

        String rowId = NewsUtils.getRowId(userEntity);
        LOGGER.info(lastSeenNewsTableName + " rowId : " + rowId);
        // System.out.println(lastSeenNewsTableName + " rowId : " + rowId);

        HTable lastSeenNewsTable = HBaseUtil.getTable(lastSeenNewsTableName);

        Put put = new Put(Bytes.toBytes(rowId));

        put.add(Bytes.toBytes("act"), Bytes.toBytes("nfid"), Bytes.toBytes(latestNewsFeedId));

        try {
            lastSeenNewsTable.put(put);
        } catch (IOException e) {
            LOGGER.error(lastSeenNewsTableName + " could not add rowId : " + rowId
                    + ", latestNewsFeedId : " + latestNewsFeedId, e);
            // System.out.println(lastSeenNewsTableName +
            // " could not add rowId : " + rowId+ ", latestNewsFeedId : " +
            // latestNewsFeedId);
            // e.printStackTrace();
            return;
        } finally {
            HBaseUtil.returnTable(lastSeenNewsTable);
        }
    }

    private String getLastSeenNewsResult(final String lastSeenNewsTableName, SrcEntity userEntity) {

        String lastSeenNewsResultId = null;

        String rowId = NewsUtils.getRowId(userEntity);
        LOGGER.info(lastSeenNewsTableName + " rowId : " + rowId);
        // System.out.println(lastSeenNewsTableName + " rowId : " + rowId);

        HTable lastSeenNewsTable = HBaseUtil.getTable(lastSeenNewsTableName);

        Get get = new Get(Bytes.toBytes(rowId));
        try {
            Result r = lastSeenNewsTable.get(get);
            if (null != r) {
                lastSeenNewsResultId = Bytes.toString(r.getValue(Bytes.toBytes("act"),
                        Bytes.toBytes("nfid")));
            }
        } catch (IOException e) {
            LOGGER.error("could not get " + lastSeenNewsTableName + " for rowId : " + rowId, e);
            // System.out.println("could not get " + lastSeenNewsTableName +
            // " for rowId : " + rowId);
        } finally {
            HBaseUtil.returnTable(lastSeenNewsTable);
        }
        return lastSeenNewsResultId;
    }

    public List<NewsFeedInfo> getActivityFeeds(EntityType entityType, String entityId, int count,
            String requesterId) {

        final long now = System.currentTimeMillis();
        // final long now = Long.MAX_VALUE;

        if (EntityType.USER == entityType) {
            if (StringUtils.isNotEmpty(requesterId) && requesterId.equals(entityId)) {
                NewsFeedSecurityVaildator.get().setNeedAuthorization(false);
                NewsFeedSecurityVaildator.get().setContextType(NewsContext.ACIVITY_FEEDS);
            }
            return getActivityFeedsFromTime(entityType, entityId, now, count, null);
        } else {
            NewsFeedSecurityVaildator.get().setContextType(NewsContext.NEWSFEED);
            return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
                    entityType, entityId, now, count, null, false, false);
        }
    }

    public List<NewsFeedInfo> getOlderActivityFeeds(EntityType entityType, String entityId,
            String beforeNewsActivityId, int count, String requesterId) {
        LOGGER.debug("Fetching old activities before"+ beforeNewsActivityId);
        long fromTime = HBaseUtil.getTimeValue(beforeNewsActivityId);
        // System.out.println("fromTime : " + fromTime);
        if (EntityType.USER == entityType) {
            if (StringUtils.isNotEmpty(requesterId) && requesterId.equals(entityId)) {
                NewsFeedSecurityVaildator.get().setNeedAuthorization(false);
                NewsFeedSecurityVaildator.get().setContextType(NewsContext.ACIVITY_FEEDS);
            }

            return getActivityFeedsFromTime(entityType, entityId, fromTime, count,
                    beforeNewsActivityId);
        } else {
            return getNewsResultsFromTime(TABLE_NAME_NEWSFEED, TABLE_NAME_LASTSEENNEWSFEED,
                    entityType, entityId, fromTime, count, beforeNewsActivityId, false, false);
        }
    }

    private List<NewsFeedInfo> getActivityFeedsFromTime(EntityType eType, String entityId,
            long fromTime, int count, String excludeRow) {

        List<NewsFeedInfo> newsFeeds = new ArrayList<NewsFeedInfo>();

        SrcEntity entity = new SrcEntity();
        entity.type = eType;
        entity.id = entityId;

        long now = fromTime;

        boolean fetchMore = true;
        while (fetchMore) {

            String startRowId = NewsUtils.getRowId(entity, now);
            long previous = now - maximumLookbackTimeInMS;
            String stopRowId = NewsUtils.getRowId(entity, previous);
            // System.out.println("startRowId : " + startRowId +
            // ", stopRowId : "
            // + stopRowId);

            Scan scan = new Scan();
            scan.addColumn(Bytes.toBytes("act"), Bytes.toBytes("data"));
            scan.setStartRow(Bytes.toBytes(startRowId));
            scan.setStopRow(Bytes.toBytes(stopRowId));

            boolean foundSomeResult = false;

            HTable newsFeedTable = null;
            try {
                newsFeedTable = HBaseUtil.getTable(TABLE_NAME_NEWSACTIVITY);
                ResultScanner s = newsFeedTable.getScanner(scan);
                for (Result r : s) {
                    String rowId = Bytes.toString(r.getRow());

                    if (null != excludeRow) {
                        if (StringUtils.equals(excludeRow, rowId)) {
                            LOGGER.info("excluding rowId : " + rowId);
                            continue;
                        }
                    }

                    String newsActivityData = Bytes.toString(r.getValue(Bytes.toBytes("act"),
                            Bytes.toBytes("data")));
                    NewsFeedInfo newsFeedInfo = gsonBuilder.create().fromJson(newsActivityData,
                            NewsFeedInfo.class);
                    newsFeedInfo.newsFeedId = rowId;// TODO this just to ensure that newsfeed id is same
                    newsFeedInfo.newsActivityId = rowId;
                    UserSecuritySet securitySet = NewsFeedSecurityVaildator.get();
                    VedantuBaseMongoModel model = securitySet.checkIfExist(newsFeedInfo.src);
                    
                    if (model!=null && (newsFeedInfo.scope == Scope.PUBLIC
                            || securitySet.validate(newsFeedInfo,model))) {
                        newsFeeds.add(newsFeedInfo);

                        foundSomeResult = true;
                        count--;
                        if (count == 0) {
                            break;
                        }

                    }

                }
            } catch (IOException e) {
                LOGGER.error("could not consume scan - startRowId : " + startRowId
                        + ", stopRowId : " + stopRowId, e);
                // System.out.println("could not consume scan - startRowId : "
                // + startRowId + ", stopRowId : " + stopRowId);
                // e.printStackTrace();
            } finally {
                HBaseUtil.returnTable(newsFeedTable);
            }

            fetchMore = fetchMore && foundSomeResult && count > 0;
            LOGGER.info("fetchmore : " + fetchMore + ", count : " + count);
            // System.out.println("fetchmore : " + fetchMore + ", count : "
            // + count);

            now = previous;
        }
        // TODO: verify it as this entity can be a user
        String userId = (entity.type == EntityType.USER) ? entityId : null;
        List<NewsFeedInfo> activityFeedDetails = NewsAggregatorHelper.populateDetails(newsFeeds,
                userId);

        return activityFeedDetails;
    }

    public List<NewsFeedInfo> getNotifications(String userId, int count) {

        final long now = System.currentTimeMillis();
        return getNewsResultsFromTime(TABLE_NAME_NOTIFICATION, TABLE_NAME_LASTSEENNOTIFICATION,
                EntityType.USER, userId, now, count, null, true, false);
    }

    public List<NewsFeedInfo> getOlderNotifications(String userId, String beforeNotificationId,
            int count) {

        long fromTime = HBaseUtil.getTimeValue(beforeNotificationId);
        return getNewsResultsFromTime(TABLE_NAME_NOTIFICATION, TABLE_NAME_LASTSEENNOTIFICATION,
                EntityType.USER, userId, fromTime, count, beforeNotificationId, false, false);
    }

    public GetNotificationSummaryRes getNotificationsSummary(String userId, int count) {

        GetNotificationSummaryRes response = new GetNotificationSummaryRes();

        final long now = System.currentTimeMillis();
        final Counter counter = new Counter();
        List<NewsFeedInfo> newsFeedInfos = getNewsResultsFromTime(TABLE_NAME_NOTIFICATION,
                TABLE_NAME_LASTSEENNOTIFICATION, EntityType.USER, userId, now, count, null, false,
                true, counter);

        response.list.addAll(newsFeedInfos);
        response.totalHits = counter.count;
        return response;
    }

    /**
     * Fetch individual feed
     * 
     * @param userId
     * @param newsFeedId
     * @return
     * @throws VedantuException
     */
    public NewsFeedInfo getNewsFeed(String userId, String newsFeedId) throws VedantuException {

        HbaseTableWrapper<NewsActivityRef> newsFeedInfoWrapper = new HbaseTableWrapper<NewsActivityRef>(
                TABLE_NAME_NEWSFEED, NewsActivityRef.class);
        NewsActivityRef activityRef = newsFeedInfoWrapper.getExact(newsFeedId, "act", "data");
        if (activityRef == null) {
            LOGGER.debug("No newsfeed found for newsFeedId " + newsFeedId);
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE);
        }

        @SuppressWarnings("unused")
        boolean newsFeedInfoPassed = true;

        HbaseTableWrapper<NewsActivity> newsActivityWrapper = new HbaseTableWrapper<NewsActivity>(
                TABLE_NAME_NEWSACTIVITY, NewsActivity.class);
        NewsActivity activityInfo = newsActivityWrapper.getExact(activityRef.aid, "act", "data");

        if (activityInfo == null) {
            LOGGER.debug("No activityInfo found for activity id " + activityRef.aid);
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE);
        }

        UserSecuritySet securitySet = NewsFeedSecurityVaildator.get();
        VedantuBaseMongoModel model = securitySet.checkIfExist(activityInfo.src);
        
        LOGGER.debug("Found security validator ");
        if (model== null || (securitySet != null && !securitySet.validate(activityInfo,model))) {
            LOGGER.debug("Rejecting feed for security " + activityInfo);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        NewsFeedInfo feedInfo = new NewsFeedInfo(activityInfo);
        feedInfo.newsActivityId = activityRef.aid;
        feedInfo.why = activityRef.why;
        feedInfo.newsFeedId = newsFeedId;
        List<NewsFeedInfo> decoratedList = new ArrayList<NewsFeedInfo>();
        decoratedList.add(feedInfo);

        List<NewsFeedInfo> decoratedInfos = NewsAggregatorHelper.populateDetails(decoratedList,
                userId);
        return decoratedInfos.get(0);
    }

    // public void getClusteredNewsFeed(Map<IClusterable,NewsFeedCluster >
    // feedMap, List<NewsFeedInfo> newsFeeds, boolean forNotifications ){
    // if( feedMap == null ) {
    // feedMap = new HashMap< IClusterable,NewsFeedCluster>();
    // }
    // SrcEntity keySrcEntity = null;
    // for( NewsFeedInfo newsFeedInfo : newsFeeds)
    // {
    // IClusterable key = null;
    // if( forNotifications ){
    // keySrcEntity = newsFeedInfo.src;
    // if( newsFeedInfo.src.getBaseParent() != null ){
    // keySrcEntity = newsFeedInfo.src.getBaseParent();
    // }
    //
    // key = new SrcSrcEntityClusterKey( keySrcEntity,
    // newsFeedInfo.info.actionType,newsFeedInfo.time );
    // }
    // else{
    // keySrcEntity = newsFeedInfo.actor;
    // key = new ActorSrcEntityClusterKey( newsFeedInfo.src.type,
    // newsFeedInfo.info.actionType, newsFeedInfo.time );
    // }
    // NewsFeedCluster newsCluster = feedMap.get( key );
    // if( newsCluster != null ){
    // LOGGER.log4j.info("Key found: " + key.toString() + " For "+
    // newsFeedInfo);
    // feedMap.get(key).addNewsFeed(newsFeedInfo);
    // }else
    // {
    // LOGGER.log4j.info("Key Added: " + key.toString() + " For "+
    // newsFeedInfo);
    // newsCluster = new
    // NewsFeedCluster(keySrcEntity,newsFeedInfo.info.actionType);
    // newsCluster.addNewsFeed(newsFeedInfo);
    // feedMap.put(key, newsCluster);
    // }
    // }
    //
    // }

}

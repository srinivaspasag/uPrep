package com.lms.components;

import com.lms.clusters.NewsFeedCluster;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.NewsContext;
import com.lms.managers.NewsAggregatorHelper;
import com.lms.managers.news.NewsFeedSecurityVaildator;
import com.lms.managers.news.UserSecuritySet;
import com.lms.models.NewsActivityInfo;
import com.lms.models.NewsFeed;
import com.lms.models.NewsNotification;
import com.lms.pojos.news.IClusterable;
import com.lms.pojos.news.NewsActivityRef;
import com.lms.pojos.news.NewsFeedInfo;
import com.lms.pojos.news.clustering.SrcNewsEntityClusterKey;
import com.lms.repository.NewsFeedRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.lms.clusters.NewsClusterManager.getSrcEntityForComment;

@Component
public class NewsFeedManager {
    private static final Logger logger = LoggerFactory.getLogger(NewsFeedManager.class);
    public static final String TABLE_NAME_NEWSACTIVITY = "newsactivity";
    public static final String TABLE_NAME_NEWSFEED = "newsfeed";
    public static final String TABLE_NAME_LASTSEENNEWSFEED = "lastseennewsfeed";
    public static final String TABLE_NAME_NOTIFICATION = "notification";
    public static final String TABLE_NAME_LASTSEENNOTIFICATION = "lastseennotification";
    @Autowired
    private NewsFeedRepo newsFeedRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<NewsFeedInfo> getNewsFeeds(String userId, int size) {
        //    NewsFeed newsFeed=newsFeedRepo.findByUserIdAndCount(userId,size);
        List<NewsFeedInfo> newsFeedInfoList = new ArrayList<>();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("src.id").is(userId);
        List<NewsFeed> newsFeeds = mongoTemplate.find(query.addCriteria(criteria), NewsFeed.class);
        newsFeeds.stream().forEach(i -> newsFeedInfoList.add(new NewsFeedInfo(i.getId().toString())));
        return newsFeedInfoList;
    }


    public boolean getClusteredNewsFeed(Map<IClusterable, NewsFeedCluster> feedMap,
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

    public List<NewsFeedInfo> getOlderNewsFeeds(String userId, String newsFeedId, int i) {
        return null;
    }

    public List<NewsFeedInfo> getActivityFeeds(EntityType entityType, String entityId, int count,
                                               String requesterId) {

        final long now = System.currentTimeMillis();
        // final long now = Long.MAX_VALUE;

        if (EntityType.USER == entityType) {
            if (!StringUtils.isEmpty(requesterId) && requesterId.equals(entityId)) {
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

    private List<NewsFeedInfo> getNewsResultsFromTime(final String newsTableName,
                                                      final String lastSeenNewsTableName, EntityType entityType, String entityId,
                                                      long fromTime, int count, String excludeRow, boolean saveLatestNewsResultId,
                                                      boolean onlyTillLastSeen) {
        List<NewsFeedInfo> newsResults = new ArrayList<NewsFeedInfo>();
        SrcEntity entity = new SrcEntity();
        entity.type = entityType;
        entity.id = entityId;
        String uptillRowId = null;
        if (onlyTillLastSeen) {
            uptillRowId = getLastSeenNewsResult(lastSeenNewsTableName, entity);
            logger.info(lastSeenNewsTableName + " uptillRowId : " + uptillRowId);

        }
        boolean saveFirstRowId = saveLatestNewsResultId;
        boolean fetchMore = true;
        long previous = 0;
        long now = fromTime;
        List<String> feedIds = new ArrayList<String>();
        List<NewsActivityRef> newsActivityRefs = new ArrayList<NewsActivityRef>();
        String previousRowId = null;
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("actor.type").is(entityType);
        criteria.and("actor.id").is(entityId);
        criteria.and("timeCreated").gt(now);
        query.addCriteria(criteria);
        if (newsTableName.equalsIgnoreCase(TABLE_NAME_NEWSFEED)) {
            List<NewsFeed> newsFeeds = mongoTemplate.find(query, NewsFeed.class);
            for (NewsFeed newsFeed : newsFeeds) {
                NewsActivityRef newsActivityRef = new NewsActivityRef();
                newsActivityRef.aid = newsFeed.aid;
                newsActivityRef.why = newsFeed.why;
                newsActivityRef.setTimestamp(newsFeed.timeCreated);
                newsActivityRefs.add(newsActivityRef);
                feedIds.add(newsFeed.aid);
            }

        } else if (newsTableName.equalsIgnoreCase(TABLE_NAME_NOTIFICATION)) {
            List<NewsNotification> newsNotifications = mongoTemplate.find(query, NewsNotification.class);
            for (NewsNotification newsNotification : newsNotifications) {
                NewsActivityRef newsActivityRef = new NewsActivityRef();
                newsActivityRef.aid = newsNotification.aid;
                newsActivityRef.why = newsNotification.why;
                newsActivityRef.setTimestamp(newsNotification.timeCreated);
                newsActivityRefs.add(newsActivityRef);
                feedIds.add(newsNotification.aid);
            }
        }
        if (saveFirstRowId && !feedIds.isEmpty()) {
            saveLatestNewsResultId(lastSeenNewsTableName, entity, feedIds.get(0));
            saveFirstRowId = false;
        }

        // logger.debug("Done fetching newsfeeds maximumLookbacTime" + maximumLookbackTimeInMS);

        logger.debug(" Feed count before decorations " + newsResults.size());
        List<NewsFeedInfo> newsFeedDetails = NewsAggregatorHelper.populateDetails(newsResults,
                NewsFeedSecurityVaildator.get().getUserId());

        return newsFeedDetails;
    }

    private String getLastSeenNewsResult(final String lastSeenNewsTableName, SrcEntity userEntity) {

        String lastSeenNewsResultId = null;

       /* String rowId = NewsUtils.getRowId(userEntity);
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
        }*/
        return lastSeenNewsResultId;
    }

    private void saveLatestNewsResultId(final String lastSeenNewsTableName, SrcEntity userEntity,
                                        String latestNewsFeedId) {

       /* String rowId = NewsUtils.getRowId(userEntity);
        logger.info(lastSeenNewsTableName + " rowId : " + rowId);
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
        }*/
    }

    private List<NewsFeedInfo> getActivityFeedsFromTime(EntityType eType, String entityId,
                                                        long fromTime, int count, String excludeRow) {

        List<NewsFeedInfo> newsFeeds = new ArrayList<NewsFeedInfo>();

        SrcEntity entity = new SrcEntity();
        entity.type = eType;
        entity.id = entityId;

        long now = fromTime;

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("actor.type").is(eType);
        criteria.and("actor.id").is(entityId);
        criteria.and("time").gt(fromTime);
        List<NewsActivityInfo> newsActivityInfos = mongoTemplate.find(query, NewsActivityInfo.class);
        for (NewsActivityInfo newsActivityInfo : newsActivityInfos) {
            NewsFeedInfo newsFeedInfo = new NewsFeedInfo();
            newsFeedInfo.setNewsFeedId(newsActivityInfo.actor.id);
            newsFeedInfo.newsActivityId = newsActivityInfo.actor.id;
            UserSecuritySet securitySet = NewsFeedSecurityVaildator.get();
            VedantuBaseMongoModel model = securitySet.checkIfExist(newsFeedInfo.src);

            if (model != null && (newsFeedInfo.scope == Scope.PUBLIC || securitySet.validate(newsFeedInfo, model))) {
                newsFeeds.add(newsFeedInfo);
            }
        }
        // TODO: verify it as this entity can be a user
        String userId = (entity.type == EntityType.USER) ? entityId : null;
        List<NewsFeedInfo> activityFeedDetails = NewsAggregatorHelper.populateDetails(newsFeeds, userId);

        return activityFeedDetails;
    }

}

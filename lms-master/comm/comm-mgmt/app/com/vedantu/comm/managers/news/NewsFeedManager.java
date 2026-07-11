package com.vedantu.comm.managers.news;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;

import com.vedantu.comm.pojos.news.NewsActivityRef;
import com.vedantu.comm.utils.HbaseTableWrapper;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.managers.AbstractContentManager;

public class NewsFeedManager extends AbstractContentManager {

    private static final int           NEWS_FEED_FETECH_BATCH_SIZE      = 30;
    public static final String         TABLE_NAME_DELETED_NEWS_ACTIVITY = "deleted_newsactivity";
    public static final String         TABLE_NAME_DELETED_NEWS_FEED     = "deleted_newsfeed";
    public static final String         TABLE_NAME_DELETED_NOTIFICATION  = "deleted_notification";

    HbaseTableWrapper<NewsActivityRef> newsFeedTableWrapper             = new HbaseTableWrapper<NewsActivityRef>(
                                                                                NewsAggregator.TABLE_NAME_NEWSFEED,
                                                                                NewsActivityRef.class);
    HbaseTableWrapper<NewsActivity>    newsActivityTableWrapper         = new HbaseTableWrapper<NewsActivity>(
                                                                                NewsAggregator.TABLE_NAME_NEWSACTIVITY,
                                                                                NewsActivity.class);

    HbaseTableWrapper<NewsActivityRef> notificationTableWrapper         = new HbaseTableWrapper<NewsActivityRef>(
                                                                                NewsAggregator.TABLE_NAME_NOTIFICATION,
                                                                                NewsActivityRef.class);

    HbaseTableWrapper<NewsActivity>    deletedNewsActivityTableWrapper  = new HbaseTableWrapper<NewsActivity>(
                                                                                TABLE_NAME_DELETED_NEWS_ACTIVITY,
                                                                                NewsActivity.class);
    HbaseTableWrapper<NewsActivity>    deletedNewsFeedTableWrapper      = new HbaseTableWrapper<NewsActivity>(
                                                                                TABLE_NAME_DELETED_NEWS_FEED,
                                                                                NewsActivityRef.class);
    HbaseTableWrapper<NewsActivity>    deletedNotificationTableWrapper  = new HbaseTableWrapper<NewsActivity>(
                                                                                TABLE_NAME_DELETED_NOTIFICATION,
                                                                                NewsActivityRef.class);

    /**
     * Removes news feeds created for content
     * 
     * @param userId
     * @param content
     * @return
     * @throws VedantuException
     */
    public boolean deleteAllNews(String userId, SrcEntity content) throws VedantuException {

        return deleteNewsFeeds(userId, content, EventType.UNKNOWN);
    }

    /**
     * Removes news feed generated for content in Event of type "type"
     * 
     * @param userId
     * @param newsSubscriber
     * @param type
     * @return
     * @throws VedantuException
     */

    public boolean deleteNewsFeeds(String userId, SrcEntity newsSubscriber, EventType type)
            throws VedantuException {

        if (!EntityType.isSupportedContentType(newsSubscriber.type)) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        List<NewsActivityRef> newsAcivityRefs = null;
        String startRowKey = newsSubscriber.id;
        do {
            List<String> rowKeyList = new ArrayList<String>();
            Logger.debug("Start key");
            newsAcivityRefs = newsFeedTableWrapper.getListedData(startRowKey, "act", "data",
                    NEWS_FEED_FETECH_BATCH_SIZE, newsSubscriber.id, rowKeyList);

            removeActivities(newsAcivityRefs, type);

        } while (CollectionUtils.isNotEmpty(newsAcivityRefs)
                && newsAcivityRefs.size() == NEWS_FEED_FETECH_BATCH_SIZE);
        return true;
    }

    public boolean removeActivities(List<NewsActivityRef> newsAcivityRefs, EventType type)
            throws VedantuException {

        do {

            if (CollectionUtils.isNotEmpty(newsAcivityRefs)) {
                List<String> activitiesToBeDeleted = new ArrayList<String>();
                for (NewsActivityRef ref : newsAcivityRefs) {
                    NewsActivity activity = newsActivityTableWrapper.getExact(ref.aid, "act",
                            "data");
                    boolean shouldBeRemoved = false;
                    if (type != null && type != EventType.UNKNOWN) {
                        if (type == activity.eType) {
                            shouldBeRemoved = true;
                        }
                    } else {
                        shouldBeRemoved = true;
                    }
                    if (shouldBeRemoved) {
                        deletedNewsActivityTableWrapper.addData(ref.aid, "act", "data", activity);
                        activitiesToBeDeleted.add(ref.aid);
                    }
                }
                newsActivityTableWrapper.delete(activitiesToBeDeleted);
            }

        } while (CollectionUtils.isNotEmpty(newsAcivityRefs)
                && newsAcivityRefs.size() == NEWS_FEED_FETECH_BATCH_SIZE);
        return true;
    }

    /**
     * Will remove news feed corresponding to this id
     * 
     * @param newsFeedId
     * @return
     * @throws VedantuException
     */
    public boolean deleteNewsFeeds(List<String> newsFeedIds) throws VedantuException {

        if (CollectionUtils.isNotEmpty(newsFeedIds)) {
            for (String newsFeedId : newsFeedIds) {
                NewsActivityRef ref = newsFeedTableWrapper.getExact(newsFeedId, "act", "data");
                deletedNewsFeedTableWrapper.addData(newsFeedId, "act", "data", ref);
            }
        }
        newsFeedTableWrapper.delete(newsFeedIds);
        return true;

    }

    public boolean deleteNotifications(List<String> notificationIds) throws VedantuException {

        if (CollectionUtils.isNotEmpty(notificationIds)) {
            for (String newsFeedId : notificationIds) {
                NewsActivityRef ref = notificationTableWrapper.getExact(newsFeedId, "act", "data");
                deletedNewsFeedTableWrapper.addData(newsFeedId, "act", "data", ref);
            }
        }
        notificationTableWrapper.delete(notificationIds);
        return true;

    }
}

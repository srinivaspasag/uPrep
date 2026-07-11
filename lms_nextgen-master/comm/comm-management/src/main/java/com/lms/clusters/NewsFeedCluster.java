package com.lms.clusters;

import com.lms.common.news.AbstractInfo;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;
import com.lms.enums.NotificationReason;
import com.lms.pojos.news.NewsFeedInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedCluster implements IListResponseObj {

    private static final Logger logger = LoggerFactory.getLogger(NewsFeedCluster.class);
    @Value("${vedantu.newsfeed.maxClusteredFeeds}")
    public static int MAX_CLUSTERED;
    public SrcEntity src;
    public SrcEntity srcOwner;
    public NotificationReason why;
    public EventType eType;
    public AbstractInfo info;
    public List<NewsFeedInfo> clusteredNews;
    public boolean hasMore;
    public String firstNewsFeedId;
    public long firstNewsFeedTime;

    public String lastNewsFeedId;
    public long lastNewsFeedTime;
    public long totalFeeds;

    public NewsFeedCluster(SrcEntity baseNewsEntity, EventType eventType) {

        super();
        this.src = baseNewsEntity;
        this.eType = eventType;
        this.clusteredNews = new ArrayList<NewsFeedInfo>();
        this.hasMore = false;
    }

    public boolean addNewsFeed(NewsFeedInfo newsFeedInfo) {

        why = this.why != null ? NotificationReason.compare(why, newsFeedInfo.why) : newsFeedInfo.why;
        totalFeeds++;
        srcOwner = newsFeedInfo.srcOwner;
        if (lastNewsFeedId == null || lastNewsFeedTime < newsFeedInfo.time) {
            lastNewsFeedId = newsFeedInfo.newsFeedId;
            if (!StringUtils.isEmpty(lastNewsFeedId)) {
                lastNewsFeedId = newsFeedInfo.newsActivityId;
            }
            lastNewsFeedTime = newsFeedInfo.time;
        }

        if (firstNewsFeedId == null || firstNewsFeedTime >= newsFeedInfo.time) {
            firstNewsFeedId = newsFeedInfo.newsFeedId;
            if (!StringUtils.isEmpty(firstNewsFeedId)) {
                firstNewsFeedId = newsFeedInfo.newsActivityId;
            }
            firstNewsFeedTime = newsFeedInfo.time;
        }
        info = newsFeedInfo.info;

        if (clusteredNews.size() < MAX_CLUSTERED) {
            if (!clusteredNews.contains(newsFeedInfo)) {

                return clusteredNews.add(newsFeedInfo);
            }
            logger.debug("Already contains news feed in cluser " + newsFeedInfo.hashCode());
            return false;
        }
        hasMore = true;
        return false;

    }

    public String getLastNewsFeedId() {

        return lastNewsFeedId;
    }

    public void setLastNewsFeedId(String lastNewsFeedId) {

        this.lastNewsFeedId = lastNewsFeedId;
    }
}

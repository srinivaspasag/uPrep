package com.vedantu.comm.news.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.news.AbstractInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class NewsFeedCluster implements IListResponseObj {

    private final static ALogger LOGGER        = Logger.of(NewsFeedCluster.class);

    public SrcEntity             src;
    public SrcEntity             srcOwner;
    public NotificationReason    why;
    public EventType             eType;
    public AbstractInfo          info;
    public List<NewsFeedInfo>    clusteredNews;
    public boolean               hasMore;
    public static int            MAX_CLUSTERED = Play.application()
                                                       .configuration()
                                                       .getInt("vedantu.newsfeed.maxClusteredFeeds",
                                                               10);

    public String                firstNewsFeedId;
    public long                  firstNewsFeedTime;

    public String                lastNewsFeedId;
    public long                  lastNewsFeedTime;
    public long                  totalFeeds;

    public NewsFeedCluster(SrcEntity baseNewsEntity, EventType eventType) {

        super();
        this.src = baseNewsEntity;
        this.eType = eventType;
        this.clusteredNews = new ArrayList<NewsFeedInfo>();
        this.hasMore = false;
    }

    public boolean addNewsFeed(NewsFeedInfo newsFeedInfo) {

        why = this.why != null ? NotificationReason.compare( why,newsFeedInfo.why) : newsFeedInfo.why;
        totalFeeds++;
        srcOwner = newsFeedInfo.srcOwner;
        if (lastNewsFeedId == null || lastNewsFeedTime < newsFeedInfo.time) {
            lastNewsFeedId = newsFeedInfo.newsFeedId;
            if (StringUtils.isNotEmpty(lastNewsFeedId)) {
                lastNewsFeedId = newsFeedInfo.newsActivityId;
            }
            lastNewsFeedTime = newsFeedInfo.time;
        }

        if (firstNewsFeedId == null || firstNewsFeedTime >= newsFeedInfo.time) {
            firstNewsFeedId = newsFeedInfo.newsFeedId;
            if (StringUtils.isNotEmpty(firstNewsFeedId)) {
                firstNewsFeedId = newsFeedInfo.newsActivityId;
            }
            firstNewsFeedTime = newsFeedInfo.time;
        }
        info = newsFeedInfo.info;

        if (clusteredNews.size() < MAX_CLUSTERED) {
            if (!clusteredNews.contains(newsFeedInfo)) {

                return clusteredNews.add(newsFeedInfo);
            }
            LOGGER.debug("Already contains news feed in cluser " + newsFeedInfo.hashCode());
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

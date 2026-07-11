package com.vedantu.eventbus.news.generators;

import java.util.Map;
import java.util.Set;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;

public class PostRemarkNewsFanOutCollector extends AbstractNewsFanOutCollector {

    public static PostRemarkNewsFanOutCollector INSTANCE = new PostRemarkNewsFanOutCollector();

    private PostRemarkNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
    }

}

package com.vedantu.eventbus.news.generators;

import java.util.Map;
import java.util.Set;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;

public class FollowEntityNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {

    public static FollowEntityNewsFanOutCollector INSTANCE = new FollowEntityNewsFanOutCollector();

    private FollowEntityNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
    }
}

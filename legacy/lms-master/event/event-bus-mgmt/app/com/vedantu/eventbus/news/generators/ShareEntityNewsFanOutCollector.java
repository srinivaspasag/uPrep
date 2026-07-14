package com.vedantu.eventbus.news.generators;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;

public class ShareEntityNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {
    private static final ALogger LOGGER = Logger.of(ShareEntityNewsFanOutCollector.class);
    public static ShareEntityNewsFanOutCollector INSTANCE = new ShareEntityNewsFanOutCollector();

    private ShareEntityNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
        LOGGER.debug("Done collecting subscribers");
    }

}

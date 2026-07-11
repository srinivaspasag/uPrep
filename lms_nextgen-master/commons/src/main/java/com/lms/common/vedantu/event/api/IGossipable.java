package com.lms.common.vedantu.event.api;

import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;

public interface IGossipable {
   public NewsActivity toNewsActivity() throws VedantuException;

    public boolean enableNotifcation(boolean value);

    public boolean getNotificationEnabled();
}

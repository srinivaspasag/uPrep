package com.vedantu.commons.events.apis;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.news.NewsActivity;

public interface IGossipable {

    public NewsActivity toNewsActivity() throws VedantuException;

    public boolean enableNotifcation(boolean value);

    public boolean getNotificationEnabled();
    
    

}

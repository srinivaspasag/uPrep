package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NewsContext {
    NEWSFEED, NOTIFICATION, EMAIL, ACIVITY_FEEDS;

    private static final Logger logger = LoggerFactory.getLogger(NewsContext.class);

    public static NewsContext getValueOfKey(String key) {
        NewsContext status = NewsContext.NEWSFEED;
        try {
            status = NewsContext.valueOf(key);
        } catch (Exception exception) {
            logger.debug("Could not find enum for " + key, exception);
        }
        return status;
    }
}

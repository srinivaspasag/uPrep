package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ConversationStatus {
    READ, UNREAD;

    private static final Logger logger = LoggerFactory.getLogger(ConversationStatus.class);

    public static ConversationStatus getValueOfKey(String key) {
        ConversationStatus status = ConversationStatus.UNREAD;
        try {
            status = ConversationStatus.valueOf(key);
        } catch (Exception exception) {
            logger.debug("Could not find enum for " + key, exception);
        }
        return status;
    }
}
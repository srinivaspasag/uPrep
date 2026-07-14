package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MessageAction {

    SEND, REPLY, FWD;
    private static final Logger logger = LoggerFactory.getLogger(MessageAction.class);

    public static MessageAction getValueOfKey(String key) {
        MessageAction action = MessageAction.SEND;
        try {
            MessageAction.valueOf(key);
        } catch (Exception exception) {
            logger.debug("Could not find enum for " + key, exception);
        }
        return action;
    }
}

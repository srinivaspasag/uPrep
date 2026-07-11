package com.lms.common.vedantu.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MailCategory {
    UNKNOWN, ALL, NOTIFICATION, MESSAGE;

    private static final Logger logger = LoggerFactory.getLogger(MailCategory.class);


    public static MailCategory valueOfKey(String value) {

        MailCategory mailCategory = UNKNOWN;
        try {
            mailCategory = MailCategory.valueOf(value.trim().toUpperCase());
        } catch (Throwable e) {
            logger.debug("Can not find mail category");
        }
        return mailCategory;
    }
}
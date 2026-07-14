package com.vedantu.commons.enums;

import play.Logger;
import play.Logger.ALogger;

public enum MailCategory {

    UNKNOWN, ALL, NOTIFICATION,MESSAGE;

    private static final ALogger LOGGER = Logger.of(MailCategory.class);

    public static MailCategory valueOfKey(String value) {

        MailCategory mailCategory = UNKNOWN;
        try {
            mailCategory = MailCategory.valueOf(value.trim().toUpperCase());
        } catch (Throwable e) {
            LOGGER.debug("Can not find mail category");
        }
        return mailCategory;
    }

}

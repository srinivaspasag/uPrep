package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OrganizationStatus {
    UNKNOWN, REQUESTED, UNAPPROVED, APPROVED, BLOCKED, REMOVED;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationStatus.class);

    public static OrganizationStatus valueOfKey(String name) {
        OrganizationStatus status = UNKNOWN;
        try {
            status = valueOf(name);
        } catch (Throwable t) {
            logger.error("unknown enum string: " + name);
        }
        return status;
    }

}

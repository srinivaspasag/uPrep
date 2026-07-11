package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OrganizationType {
    UNKNOWN, COLLEGE, SCHOOL, UNIVERSITY, INSTITUTE, COMPANY;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationType.class);

    public static OrganizationType valueOfKey(String name) {
        OrganizationType orgType = UNKNOWN;
        try {
            orgType = valueOf(name);
        } catch (Throwable t) {
            logger.error("unknown enum string: " + name);
        }
        return orgType;
    }
}

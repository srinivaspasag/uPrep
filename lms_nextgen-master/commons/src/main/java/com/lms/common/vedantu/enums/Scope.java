package com.lms.common.vedantu.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Scope {
    UNKNOWN, PUBLIC, PRIVATE, ORG,LIBRARY;

    private static final Logger logger = LoggerFactory.getLogger(Scope.class);

    public static Scope valueOfKey(String name) {
        Scope scope = UNKNOWN;
        try {
            scope = valueOf(name);
        } catch (Throwable t) {
            logger.error("unknown enum string: " + name);
        }
        return scope;
    }
}

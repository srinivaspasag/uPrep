package com.lms.common.vedantu.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GradeType {

    UNKNOWN, K12, UND_GRAD, GRAD, POST_GRAD;

    private static final Logger logger = LoggerFactory.getLogger(GradeType.class);

    public static GradeType valueOfKey(String key) {
        GradeType gradeType = UNKNOWN;
        try {
            gradeType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
            logger.error("unknown enum string: " + key);
        }
        return gradeType;
    }
}

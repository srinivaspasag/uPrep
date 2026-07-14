package com.lms.common.vedantu.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BoardType {
    ORGANIZATION, EXAM, COURSE, TOPIC, SUBTOPIC, SUBSUBTOPIC, FORMULA, CAREER, GENERAL;

    private static final Logger logger = LoggerFactory.getLogger(BoardType.class);

    public static BoardType valueOfKey(String key) {
        BoardType boardType = GENERAL;
        try {
            boardType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
            logger.error("unknown enum string: " + key);
        }
        return boardType;
    }
}

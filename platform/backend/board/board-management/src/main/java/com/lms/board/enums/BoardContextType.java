package com.lms.board.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BoardContextType {
    UNKNOWN, GLOBAL, CONSUMER, ORG;

    private static final Logger logger = LoggerFactory.getLogger(BoardContextType.class);

    public static BoardContextType valueOfKey(String name) {
        BoardContextType type = UNKNOWN;
        try {
            type = valueOf(name.trim().toUpperCase());
        } catch (Exception e) {
            logger.error("unknown enum string: " + name);
        }
        return type;
    }

    public static BoardContextType getParentContextType(
            BoardContextType contextType) {
        BoardContextType parentContextType = ORG == contextType ? CONSUMER
                : (CONSUMER == contextType ? GLOBAL : null);
        return parentContextType;
    }

    public static boolean isTreeNameNeeded(BoardContextType contextType) {
        return CONSUMER == contextType || ORG == contextType;
    }

    public static boolean isGradeNeeded(BoardContextType contextType) {
        return CONSUMER == contextType || ORG == contextType;
    }
}

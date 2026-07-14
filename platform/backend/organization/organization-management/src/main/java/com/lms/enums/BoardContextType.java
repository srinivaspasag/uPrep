package com.lms.enums;


public enum BoardContextType {

    UNKNOWN, GLOBAL, CONSUMER, ORG;


    public static BoardContextType valueOfKey(String name) {
        BoardContextType type = UNKNOWN;
        try {
            type = valueOf(name.trim().toUpperCase());
        } catch (Exception e) {
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

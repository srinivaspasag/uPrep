package com.vedantu.commons.enums;

public enum UserActionType {
    UNKNOWN,
    UPDATED,
    ADDED,
    ENDED,
    CHANGED,
    FOLLOWING,
    ATTEMPTED,
    PUBLISHED,
    VOTED,
    RATED,
    COMMENTED,
    ASKED,
    SHARED,
    VIEWED,
    DELETED,
    MADE_VISIBLE,
    COMPLETED;

    public String getSearchIndexType() {

        return this.name().toLowerCase();
    }

    public static UserActionType valueOfKey(String key) {

        UserActionType type = UNKNOWN;
        try {
            type = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return type;
    }

    public static enum EventActionType {
        UNKNOWN, ADD, UPDATE, REMOVE;

        public static EventActionType valueOfKey(String key) {

            EventActionType type = UNKNOWN;
            try {
                type = valueOf(key.trim().toUpperCase());
            } catch (Exception e) {}
            return type;
        }
    }

}

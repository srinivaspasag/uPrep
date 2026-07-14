package com.lms.common.vedantu.enums;

public enum UserRatingType {
    UNKNOWN,
    GOOD,
    AVERAGE,
    BAD;

    public String getSearchIndexType() {
        return this.name().toLowerCase();
    }

    public static UserRatingType valueOfKey(String key) {

        UserRatingType type = UNKNOWN;
        try {
            type = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return type;
    }
}

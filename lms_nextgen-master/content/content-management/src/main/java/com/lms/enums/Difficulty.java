package com.lms.enums;


public enum Difficulty {
    UNKNOWN, EASY, MODERATE, TOUGH;

    public static Difficulty valueOfKey(String value) {
        Difficulty difficulty = UNKNOWN;
        for (Difficulty diff : values()) {
            if (diff.name() != null && value != null && value.equalsIgnoreCase(diff.name())) {
                difficulty = diff;
                break;
            }
        }
        return difficulty;
    }
}

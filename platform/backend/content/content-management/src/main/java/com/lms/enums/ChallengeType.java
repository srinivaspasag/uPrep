package com.lms.enums;

public enum ChallengeType {
    FIXED_TIME, NO_TIME;

    public static ChallengeType valueOfKey(String key) {
        ChallengeType challengeType = NO_TIME;
        try {
            challengeType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return challengeType;
    }
}

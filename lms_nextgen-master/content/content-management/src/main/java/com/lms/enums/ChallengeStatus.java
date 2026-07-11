package com.lms.enums;

public enum ChallengeStatus {
    ACTIVE, ENDED;

    public static ChallengeStatus valueOfKey(String key) {
        ChallengeStatus status = ENDED;
        try {
            status = ChallengeStatus.valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return status;
    }
}

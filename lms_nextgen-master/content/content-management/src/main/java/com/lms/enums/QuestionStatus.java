package com.lms.enums;

public enum QuestionStatus {

    COMPLETE, INCOMPLETE, MODIFIED, UNKNOWN;

    public static QuestionStatus valueOfKey(String key) {
        QuestionStatus status = UNKNOWN;
        try {
            status = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return status;
    }

}

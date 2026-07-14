package com.lms.common.vedantu.mongo;

public enum VedantuRecordState {
    UNKNOWN,TEMPORARY
    /*For temporary states this is required to remove */,CONFIRMING, ACTIVE, DELETED;

    public static VedantuRecordState valueOfKey(String value) {

        VedantuRecordState entityType = UNKNOWN;
        try {
            entityType = VedantuRecordState.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return entityType;
    }
}

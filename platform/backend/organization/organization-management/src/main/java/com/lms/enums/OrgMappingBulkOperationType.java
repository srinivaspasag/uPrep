package com.lms.enums;

public enum OrgMappingBulkOperationType {

    COPY, MOVE, REMOVE, UNKNOWN;

    public OrgMappingBulkOperationType valueOfKey(String key) {

        OrgMappingBulkOperationType operationType = UNKNOWN;
        try {
            operationType = valueOf(key.trim().toUpperCase());
        } catch (Throwable e) {
            // swallow: handle exception
        }
        return operationType;
    }
}

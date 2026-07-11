package com.lms.common.vedantu.enums;

public enum DeviceType {
    UNKNOWN, WEB, MOBILE;

    public static DeviceType valueOfKey(String value) {

        DeviceType type = UNKNOWN;
        try {
            type = DeviceType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return type;
    }
}

package com.vedantu.commons.enums;

/**
 * this has to be thought of to distinguish between Mobile/ Tablet/ TV
 * 
 * @author vikram
 * 
 */
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

package com.lms.common.vedantu.enums;

import java.io.Serializable;

public enum AppStore implements Serializable {

    UNKNOWN(""),
    ITUNES("itunes"),
    GOOGLE_PLAY("google_play"),
    WINDOWS_PHONE("windows_phone"),
    WINDOWS_8("windows8"),
    BLACKBERRY("blackberry");


    private AppStore(String storeName) {

        this.logoConfig ="logo.url." + storeName.trim().toLowerCase();

    }

    public static AppStore valueOfKey(String value) {

        AppStore appStore = UNKNOWN;
        try {
            appStore = AppStore.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return appStore;
    }

    private String logoConfig;

    public String getLogoURL() {

        return logoConfig;
    }
};
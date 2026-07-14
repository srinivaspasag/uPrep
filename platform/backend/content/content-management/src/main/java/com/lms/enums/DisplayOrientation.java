package com.lms.enums;

import java.io.Serializable;

public enum DisplayOrientation implements Serializable {

    LANDSCAPE,
    POTRAIT;

    public static DisplayOrientation valueOfKey(String value) {

        DisplayOrientation orientation = POTRAIT;
        try {
            orientation = DisplayOrientation.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return orientation;
    }
}

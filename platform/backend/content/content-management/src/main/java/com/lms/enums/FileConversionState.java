package com.lms.enums;

public enum FileConversionState {
    ORIGINAL,
    ENCRYPTED,
    CONVERTED,
    CONVERTETED_ENCRYPTED;

    // @formatter:on

    public static FileConversionState valueOfKey(String value) {

        FileConversionState fileState = ORIGINAL;
        try {
            fileState = FileConversionState.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return fileState;
    }
}

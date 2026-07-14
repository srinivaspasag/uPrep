package com.lms.common.validation;

public class Validation {

    public static boolean isStringNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isObjectEmpty(Object value) {
        return value == null;
    }

    public static boolean isEmptyStringArray(String[] array) {
        return null == array || array.length == 0;
    }
}

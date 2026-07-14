package com.lms.enums;

public enum NumberUpdate {
    INCREMENT(1), DECREMENT(-1);

    private final int value;

    NumberUpdate(int value) {

        this.value = value;
    }

    public static NumberUpdate valueOfKey(String key) {

        NumberUpdate sortOrder = DECREMENT;
        try {
            sortOrder = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return sortOrder;
    }

    public int getValue() {

        return value;
    }
}

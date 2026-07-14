package com.lms.enums;

public enum MultiplierPowerType {
    SINGLE(1), DOUBLE(2), TRIPLE(3);
    private final int multiplier;

    MultiplierPowerType(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }
}

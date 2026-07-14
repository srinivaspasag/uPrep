package com.lms.enums;

public enum PlanState {
    INVALID, ACTIVE, OBSOLETE, DRAFT;

    public static PlanState valueOfKey(String value) {

        PlanState planType = INVALID;
        try {
            planType = PlanState.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow

        }
        return planType;
    }
}

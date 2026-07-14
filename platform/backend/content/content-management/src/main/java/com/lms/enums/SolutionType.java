package com.lms.enums;


public enum SolutionType
{

    UNKNOWN, UGS, SGS, ORGS;

    public static SolutionType valueOfKey(String key) {
        SolutionType solutionType = UNKNOWN;
        try {
            solutionType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return solutionType;
    }
}

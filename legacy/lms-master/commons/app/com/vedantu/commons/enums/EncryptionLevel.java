package com.vedantu.commons.enums;

public enum EncryptionLevel {
    NA, P, P_O, P_O_U;

    public static EncryptionLevel valueOfKey(String level) {

        EncryptionLevel eLevel = NA;
        try {
            eLevel = EncryptionLevel.valueOf(level);
        } catch (Exception e) {}
        return eLevel;
    }
    
}

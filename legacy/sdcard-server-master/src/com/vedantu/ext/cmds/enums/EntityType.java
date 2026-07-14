package com.vedantu.ext.cmds.enums;

public enum EntityType {

    FOLDER, SDCARD, SECTION, UNKNOWN;

    public static EntityType valueOfKey(String key) {

        EntityType eType = UNKNOWN;
        try {
            eType = valueOf(key.trim().toUpperCase());
        } catch (Throwable e) {}
        return eType;
    }

}

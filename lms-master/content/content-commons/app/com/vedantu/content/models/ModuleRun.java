package com.vedantu.content.models;

public enum ModuleRun {
    SEQUENTIAL, NON_SEQUENTIAL;

    public static ModuleRun valueOfKey(String key) {

        ModuleRun moduleRun = NON_SEQUENTIAL;
        try {
            moduleRun = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return moduleRun;
    }
}

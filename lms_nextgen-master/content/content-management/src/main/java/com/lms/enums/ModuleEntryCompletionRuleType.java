package com.lms.enums;

import java.io.Serializable;


public enum ModuleEntryCompletionRuleType implements Serializable {
    NONE, VIEW;

    public static ModuleEntryCompletionRuleType valueOfKey(String key) {

        ModuleEntryCompletionRuleType moduleEntryCompletionRuleType = NONE;
        try {
            if (key != null)
                moduleEntryCompletionRuleType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return moduleEntryCompletionRuleType;
    }
}
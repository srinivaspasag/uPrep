package com.vedantu.content.enums;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public enum ModuleEntryCompletionRuleType implements Serializable {
    NONE, VIEW;

    public static ModuleEntryCompletionRuleType valueOfKey(String key) {

        ModuleEntryCompletionRuleType moduleEntryCompletionRuleType = NONE;
        try {
            moduleEntryCompletionRuleType = valueOf(StringUtils.upperCase(key.trim()));
        } catch (Exception e) {}
        return moduleEntryCompletionRuleType;
    }
}
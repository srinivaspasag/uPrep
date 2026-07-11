/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webUtils;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author ajith
 */
public enum ModuleDisplayTextForEntity {

    DOCUMENT("Read", "Must Read", "Read",
            "Read Now"), TEST("Attempted",
                    "Must Attempt", "Attempt", "Attempt Now"), VIDEO(
                    "Watched", "Must Watch", "Watch",
                    "Watch Now"), ASSIGNMENT(
                    "Attempted", "Must Attempt", "Attempt", "Attempt Now"), FILE(
                    "Viewed", "", "View", ""), UNKNOWN;

    String consumedText;
    String consumeTextCompulsory;
    String consumeTextOptional;
    String consumeNowText;

    private ModuleDisplayTextForEntity(
            String consumedText, String consumeTextCompulsory,
            String consumeTextOptional, String consumeNowText) {
        this.consumedText = consumedText;
        this.consumeTextCompulsory = consumeTextCompulsory;
        this.consumeTextOptional = consumeTextOptional;
        this.consumeNowText = consumeNowText;
    }

    private ModuleDisplayTextForEntity() {

    }

    public static ModuleDisplayTextForEntity valueOfKey(String value) {

        ModuleDisplayTextForEntity entityType = UNKNOWN;
        try {
            entityType = ModuleDisplayTextForEntity.valueOf(StringUtils
                    .upperCase(value));
        } catch (Exception e) {
            // Swallow
        }
        return entityType;
    }
}

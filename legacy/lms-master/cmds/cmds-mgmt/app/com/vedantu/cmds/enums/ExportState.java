package com.vedantu.cmds.enums;


public enum ExportState {

    UNKNOWN, RUNNING, FINALIZING, FINISHED, CANCELLED;

    public static ExportState valueOfKey(String value) {

        ExportState exportType = UNKNOWN;
        try {
            exportType = ExportState.valueOf(value.toUpperCase());
        } catch (Exception e) {
            // Swallow
        }
        return exportType;
    }
}

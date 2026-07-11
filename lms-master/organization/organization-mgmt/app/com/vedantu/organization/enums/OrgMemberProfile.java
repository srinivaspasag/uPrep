package com.vedantu.organization.enums;

import play.Logger;
import play.Logger.ALogger;

public enum OrgMemberProfile {

    UNKNOWN, OFFLINE_USER/* default profile for off-line tabapp uses */, MANAGER(true), TEACHER(
            true), EDITOR, STUDENT, PARENT, SALESPERSON;

    private static final ALogger LOGGER = Logger.of(OrgMemberProfile.class);

    private boolean              isAllowedInstContentRemoval;

    private OrgMemberProfile() {

        this(false);
    }

    private OrgMemberProfile(boolean isAllowedInstContentRemoval) {

        this.isAllowedInstContentRemoval = isAllowedInstContentRemoval;
    }

    public static OrgMemberProfile valueOfKey(String name) {

        OrgMemberProfile profile = UNKNOWN;
        try {
            profile = valueOf(name.trim().toUpperCase());
        } catch (Throwable t) {
            LOGGER.error("unknown enum string: " + name);
        }
        return profile;
    }

    public boolean isAllowedInstContentRemoval() {

        return isAllowedInstContentRemoval;
    }

}

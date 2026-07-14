package com.lms.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum OrgMemberProfile {
    UNKNOWN, OFFLINE_USER/* default profile for off-line tabapp uses */, MANAGER(true), TEACHER(
            true), EDITOR, STUDENT, PARENT, SALESPERSON;

    private static final Logger logger = LoggerFactory.getLogger(OrgMemberProfile.class);

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
            logger.error("unknown enum string: " + name);
        }
        return profile;
    }

    public boolean isAllowedInstContentRemoval() {

        return isAllowedInstContentRemoval;
    }
}

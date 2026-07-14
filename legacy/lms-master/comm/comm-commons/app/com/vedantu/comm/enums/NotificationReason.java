package com.vedantu.comm.enums;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.enums.EntityType;

public enum NotificationReason {
    // @formatter:off
	UNKNOWN,
	SOURCE,
	ROOT_OWNER, 
	OWNER, 
	ACTOR, 
	SHARED_WITH,
	SHARED_WITH_GROUP, 
	SHARED_WITH_ORG,
	SHARED_WITH_PROGRAM,
	SHARED_WITH_CENTER, 
	SHARED_WITH_SECTION, 
	SHARED_WITH_DEPARTMENT, 
	INVOLVED, 
	FOLLOWING_OWNER,
	FOLLOWING_ACTOR, 
	FOLLOWING_SHARED_WITH, 
	FOLLOWING_INVOLVED,
	FOLLOWING_SOURCE,
	ATTEMPTED,
	ADDED_SOLUTION,
	COMMENTED,
	;
	// @formatter:on
    static {
        notificationList = formNotificaitonList();
    }

    public static List<NotificationReason> notificationList;

    public static NotificationReason valueOfKey(String key) {

        NotificationReason activityReason = UNKNOWN;
        try {
            activityReason = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return activityReason;
    }

    public static NotificationReason getSharedWithActivityReason(EntityType type) {

        if (EntityType.ORGANIZATION == type) {
            return SHARED_WITH_ORG;
        } else if (EntityType.PROGRAM == type) {
            return SHARED_WITH_PROGRAM;
        }
        return SHARED_WITH;
    }

    private static List<NotificationReason> formNotificaitonList() {

        List<NotificationReason> reasonList = new ArrayList<NotificationReason>();

        reasonList.add(OWNER);
        reasonList.add(ROOT_OWNER);
        reasonList.add(INVOLVED);
        reasonList.add(SHARED_WITH);
        reasonList.add(SHARED_WITH_SECTION);
        reasonList.add(SHARED_WITH_CENTER);
        reasonList.add(ATTEMPTED);
        reasonList.add(COMMENTED);
        reasonList.add(FOLLOWING_SOURCE);
        reasonList.add(ADDED_SOLUTION);

        return reasonList;
    }

    public static List<NotificationReason> getNotificationReasonSet() {

        return notificationList;
    }

    public static NotificationReason compare(NotificationReason reason,
            NotificationReason compareReason) {

        if (notificationList.contains(compareReason) && notificationList.contains(reason)) {
            return notificationList.indexOf(reason) < notificationList.indexOf(compareReason) ? reason
                    : compareReason;

        }
        return reason;
    }
}

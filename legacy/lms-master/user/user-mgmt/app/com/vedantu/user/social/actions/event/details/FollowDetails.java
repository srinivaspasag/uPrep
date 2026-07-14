package com.vedantu.user.social.actions.event.details;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;

public class FollowDetails extends UserEntityActionDetails {

    public FollowDetails() {

        super();
    }

    public FollowDetails(String userId, EventType eventType, SrcEntity target, String actionId) {

        super(userId, eventType, target, actionId);
    }

    @Override
    public boolean enableNotifcation(boolean value) {
        notificationEnabled=value;
        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return true;
    }
}

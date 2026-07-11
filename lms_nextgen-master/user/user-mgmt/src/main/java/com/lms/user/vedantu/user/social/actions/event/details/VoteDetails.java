package com.lms.user.vedantu.user.social.actions.event.details;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EventType;

public class VoteDetails extends UserEntityActionDetails {

    public VoteDetails() {
        super();
    }

    public VoteDetails(String userId, EventType eventType, SrcEntity target, String actionId) {
        super(userId, eventType, target, actionId);
    }

    @Override
    public boolean enableNotifcation(boolean value) {
        notificationEnabled = true;
        return true;
    }

    @Override
    public boolean getNotificationEnabled() {
        return true;
    }

}

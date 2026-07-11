package com.vedantu.user.social.actions.event.details;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.SrcEntity;

public class VoteDetails extends UserEntityActionDetails {

    public VoteDetails() {
        super();
    }

    public VoteDetails(String userId, EventType eventType, SrcEntity target, String actionId) {
        super(userId, eventType, target, actionId);
    }

    @Override
    public boolean enableNotifcation(boolean value) {
        notificationEnabled=true;
        return true;
    }

    @Override
    public boolean getNotificationEnabled() {
        return true;
    }

}

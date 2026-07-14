package com.vedantu.eventbus.email.details;

import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class FollowEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public FollowEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.FOLLOW_ENTITY));
    }

    @Override
    public String getSubject() {

        return null;
    }
}

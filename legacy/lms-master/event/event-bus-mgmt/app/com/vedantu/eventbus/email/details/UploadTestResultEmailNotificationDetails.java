package com.vedantu.eventbus.email.details;

import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class UploadTestResultEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public UploadTestResultEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE
                .getTemplateConfigurationKey(EventType.UPLOAD_TEST_RESULT));
    }

    @Override
    public String getSubject() {

        return null;
    }
}

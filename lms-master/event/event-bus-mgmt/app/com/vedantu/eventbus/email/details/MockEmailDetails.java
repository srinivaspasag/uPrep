package com.vedantu.eventbus.email.details;

import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails;

public class MockEmailDetails extends AbstractEmailTemplateDetails {

    public MockEmailDetails() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEAMPLATE_EMAIL_MOCK);

    }

    @Override
    public String getSubject() {

        return subject;
    }

    @Override
    public boolean verify() {

        return true;
    }

}

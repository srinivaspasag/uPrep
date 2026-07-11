package com.vedantu.eventbus.email.details;

import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails;

public class EmailAccessCode extends AbstractEmailTemplateDetails {

    public EmailAccessCode() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEMPLATE_EMAIL_ACCESS_CODE);

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

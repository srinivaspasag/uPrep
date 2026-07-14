package com.vedantu.user.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.utils.JSONUtils;

public class ForgotPasswordDetails extends IndividualEmailTemplateDetails {

    public static final String VERIFICATION_LINK = "verificationLink";

    public String              verificationLink;

    public ForgotPasswordDetails() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEMPLATE_EMAIL_FORGOT_PASSWORD);
    }

    @Override
    public String getSubject() {

        return "Reset Password for " + getFullName();
    }

    @Override
    public boolean verify() {

        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(VERIFICATION_LINK, verificationLink);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        verificationLink = JSONUtils.getString(json, VERIFICATION_LINK);
    }
}

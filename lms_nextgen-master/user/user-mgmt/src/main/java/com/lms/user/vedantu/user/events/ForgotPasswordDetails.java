package com.lms.user.vedantu.user.events;

import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

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

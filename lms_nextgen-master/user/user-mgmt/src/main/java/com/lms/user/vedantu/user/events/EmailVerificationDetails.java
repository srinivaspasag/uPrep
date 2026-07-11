package com.lms.user.vedantu.user.events;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.EmailConfigurationConstants;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class EmailVerificationDetails extends IndividualEmailTemplateDetails {

    public static final String VERIFICATION_LINK = "verificationLink";

    public String              verificationLink;

    public ModelExtendedInfo orgInfo;
    public String              orgId;

    public EmailVerificationDetails() throws ClassNotFoundException {

        super(EmailConfigurationConstants.TEMPLATE_EMAIL_VERIFICATION);

    }

    @Override
    public String getSubject() {

        return "Email Verification for " + getFullName();
    }

    @Override
    public boolean verify() {

        if (!user.firstName.isEmpty()&& !(verificationLink).isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public SrcEntity __getSrcEntity() {

       return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();

        json.put(VERIFICATION_LINK, verificationLink);
        json.put(ConstantsGlobal.ORG_ID, orgId);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

        verificationLink = JSONUtils.getString(json, VERIFICATION_LINK);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);

    }

}

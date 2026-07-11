package com.vedantu.user.event.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.mongo.VedantuBasicDAO;

public class EmailVerificationDetails extends IndividualEmailTemplateDetails {

    public static final String VERIFICATION_LINK = "verificationLink";

    public String              verificationLink;

    public ModelExtendedInfo   orgInfo;
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

        if (StringUtils.isNotEmpty(user.firstName) && StringUtils.isNotEmpty(verificationLink)) {
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
        if (StringUtils.isNotEmpty(orgId)) {
            VedantuBasicDAO orgDAO = EntityTypeDAOFactory.INSTANCE.get(EntityType.ORGANIZATION);
            orgInfo = orgDAO.getExtendedInfo(orgId);
        }
    }

}

package com.vedantu.comm.email.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.user.event.details.IndividualEmailTemplateDetails;

public class FeatureEmailDetails extends IndividualEmailTemplateDetails {

    private static final String  SENDER_NAME            = "senderName";
    public static final String   SENDER_ID              = "senderId";
    public static final String   X_CONVERSATION_ID      = "X-CONVERSATION-ID";
    public static final String   X_USER_CONVERSATION_ID = "X-USER-CONVERSATION-ID";
    public static final String   X_MESSAGE_ID           = "X-MESSAGE-ID";
    public static final String   X_USER_MESSAGE_ID      = "X-USER-MESSAGE-ID";
    private final static ALogger LOGGER                 = Logger.of(FeatureEmailDetails.class);
    private static final String  LAST_NAME              = "lastName";
    public static final String   FIRST_NAME             = "firstName";

    public String                firstName;
    public String                lastName;
    public String                orgId;
    public String                senderId;
    public String                senderName;

    public String                messageContent;

    public String                organizationName;

    public FeatureEmailDetails() {

        super(EmailConfigurationConstants.EMAIL_FEATURE);
    }

    @Override
    public String getSubject() {

        return subject;
    }

    @Override
    public boolean verify() {

        if (StringUtils.isNotEmpty(messageContent)) {

            return true;
        }
        LOGGER.debug("Verification for message details failed ");
        return false;

    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(FIRST_NAME, firstName);
        json.put(LAST_NAME, lastName);
        json.put(ConstantsGlobal.CONTENT, messageContent);
        json.put(ConstantsGlobal.ORG_ID, orgId);
        json.put(SENDER_ID, senderId);
        json.put(SENDER_NAME, senderName);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        firstName = JSONUtils.getString(json, FIRST_NAME);
        lastName = JSONUtils.getString(json, LAST_NAME);
        messageContent = JSONUtils.getString(json, ConstantsGlobal.CONTENT);

        senderId = JSONUtils.getString(json, SENDER_ID);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
        senderName = JSONUtils.getString(json, SENDER_NAME);

    }

    public String getMessageContent() {

        return messageContent;
    }

    @Override
    public String __getContent() {

        return messageContent;
    }

}

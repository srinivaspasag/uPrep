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
import com.vedantu.user.pojos.UserEmailInfo;

public class MessageEmailDetails extends IndividualEmailTemplateDetails {

    public static final String   USER_CONVERSATION_ID   = "userConversationId";
    private static final String  SENDER_INFO            = "senderInfo";
    public static final String   SENDER_ID              = "senderId";
    public static final String   X_CONVERSATION_ID      = "X-CONVERSATION-ID";
    public static final String   X_USER_CONVERSATION_ID = "X-USER-CONVERSATION-ID";
    public static final String   X_MESSAGE_ID           = "X-MESSAGE-ID";
    public static final String   X_USER_MESSAGE_ID      = "X-USER-MESSAGE-ID";
    private final static ALogger LOGGER                 = Logger.of(MessageEmailDetails.class);
    private static final String  LAST_NAME              = "lastName";
    public static final String   FIRST_NAME             = "firstName";

    public String                orgId;
    public UserEmailInfo         senderInfo;
    public String                userConversationId;
    public String                messageContent;
    public String                organizationName;

    public MessageEmailDetails() {

        super(EmailConfigurationConstants.EMAIL_MESSAGE);
    }

    public MessageEmailDetails(MessageEmailDetails details) {

        super(details);
        this.orgId = details.orgId;
        this.senderInfo = details.senderInfo;
        this.userConversationId = details.userConversationId;
        this.messageContent = details.messageContent;
        this.organizationName = details.organizationName;
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

        LOGGER.debug("Sender Info" + senderInfo);
       
        json.put(ConstantsGlobal.CONTENT, messageContent);
        json.put(ConstantsGlobal.ORG_ID, orgId);
        json.put(SENDER_INFO, senderInfo.toJSON());
        json.put(USER_CONVERSATION_ID, userConversationId);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        messageContent = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);

        senderInfo = (UserEmailInfo) JSONUtils.getJSONAware(new UserEmailInfo(), json, SENDER_INFO);

        userConversationId = JSONUtils.getString(json, USER_CONVERSATION_ID);
    }

    public String getMessageContent() {

        return messageContent;
    }

    @Override
    public MessageEmailDetails clone() throws CloneNotSupportedException {

        return new MessageEmailDetails(this);
    }
}

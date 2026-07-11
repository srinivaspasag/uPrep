package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class AddChallengeEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public AddChallengeEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.INDEX_CHALLENGE));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");
        StringBuilder sb = _getStringBuilderWithActorData();
        sb.append(" added a new challenge");
        return sb.toString().trim().replaceAll("\\s", " ");

    }
}

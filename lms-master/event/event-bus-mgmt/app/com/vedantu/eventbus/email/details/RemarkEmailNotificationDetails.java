package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class RemarkEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public RemarkEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.POST_REMARK));
    }

    @Override
    public String getSubject() {
        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();
        return   sb.toString() +" just left you a remark!";
    }
}

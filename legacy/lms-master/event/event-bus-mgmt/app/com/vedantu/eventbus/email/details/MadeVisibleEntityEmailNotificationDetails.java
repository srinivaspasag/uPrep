package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.ui.utils.GrammerUtils;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class MadeVisibleEntityEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public MadeVisibleEntityEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.MADE_VISIBLE));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();
        sb.append(" added");

        String contentType = JSONUtils.getString(src, ConstantsGlobal.TYPE).toLowerCase();
        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));
        if (why == NotificationReason.OWNER) {
            sb.append(" " + "your" + " " + getTypeName(contentType));
        } else {
            sb.append(" " + GrammerUtils.getArticle(getTypeName(contentType)) + " "
                    + getTypeName(contentType));
        }
        sb.append(" " + "to your library");
        return sb.toString();
    }
}

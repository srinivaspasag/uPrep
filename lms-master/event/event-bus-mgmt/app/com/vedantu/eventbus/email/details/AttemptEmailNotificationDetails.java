package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class AttemptEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public AttemptEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.ATTEMPT_ENTITY));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();
        sb.append(" attempted");

        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));
        String entityType = JSONUtils.getString(src, ConstantsGlobal.TYPE).toLowerCase();
        switch (why) {
        case OWNER:
            sb.append(" your " + getTypeName(entityType));
            break;
        case ATTEMPTED:
            sb.append(" " + getTypeName(entityType) + " that you attempted");
            break;
        case ADDED_SOLUTION:
            sb.append(" "+getTypeName(entityType) + " that you added solution to");
            break;
        default:
            sb.append(" a " + getTypeName(entityType));
            break;
        }

        return sb.toString();
    }
}

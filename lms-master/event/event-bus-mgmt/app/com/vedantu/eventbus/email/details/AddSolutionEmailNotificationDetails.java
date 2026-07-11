package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class AddSolutionEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public AddSolutionEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.ADD_SOLUTION));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();
        sb.append(" added solution to");

        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));
        String entityType = JSONUtils.getString(src, ConstantsGlobal.TYPE).toLowerCase();

        switch (why) {
        case OWNER:
            sb.append(" your " + entityType);
            break;
        case ATTEMPTED:
            sb.append(" " + entityType + " you attempted");
            break;
        case ADDED_SOLUTION:
            sb.append(" " + entityType + " you added solution to");
            break;
        default:
            sb.append(" a " + entityType);
            break;
        }

        sb.append(" on vedantu");
        return sb.toString().trim().replaceAll("\\s", " ");
    }
}

package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class EndChallengeEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public EndChallengeEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.END_CHALLENGE));
    }

    @Override
    public String getSubject() {
        JSONObject src = JSONUtils.getJSONObject(details, "src");
        StringBuilder sb = new StringBuilder();
        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));

        sb.append("Challenge ");
        sb.append(JSONUtils.getString(src, ConstantsGlobal.NAME));

        switch (why) {
            case OWNER:
                sb.append(" you added ended now");
                break;
            case ATTEMPTED:
                sb.append(" you attempted ended now");
                break;
            default:
                break;
        }
        return sb.toString().trim().replaceAll("\\s", " ");
    }
}

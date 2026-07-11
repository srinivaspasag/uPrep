package com.vedantu.eventbus.email.details;

import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class VoteEmailNotificationDetails extends AbstractEmailNotificationDetails {

    private static final ALogger LOGGER = Logger.of(VoteEmailNotificationDetails.class);

    public VoteEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.VOTE_ENTITY));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();
        sb.append(" upvoted");

        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));

        LOGGER.debug("Reason " + why);

        JSONObject root = JSONUtils.getJSONObject(src, "rootDetails");

        String upvotedEntityName = JSONUtils.getString(src, ConstantsGlobal.TYPE).toLowerCase();
        if (root != null) {
            EntityType rootType = EntityType.valueOfKey(JSONUtils.getString(root,
                    ConstantsGlobal.TYPE).toLowerCase());
            if (rootType == EntityType.DISCUSSION) {

                upvotedEntityName = "answer";
            }

        }

        switch (why) {
        case OWNER:
            sb.append(" your");
            sb.append(" " + getTypeName(upvotedEntityName));
            break;
        case ROOT_OWNER:
            sb.append(" " + getTypeName(upvotedEntityName));

            sb.append(" added on");
            sb.append(" your ");
            sb.append(" "
                    + getTypeName(EntityType.valueOfKey(JSONUtils.getString(root,
                            ConstantsGlobal.TYPE).toLowerCase())) + " "
                    + JSONUtils.getString(root, ConstantsGlobal.NAME));
            break;
        case COMMENTED:

            upvotedEntityName = JSONUtils.getString(src, ConstantsGlobal.TYPE).toLowerCase();
            if (root != null) {
                EntityType rootType = EntityType.valueOfKey(JSONUtils.getString(root,
                        ConstantsGlobal.TYPE).toLowerCase());
                if (rootType == EntityType.DISCUSSION) {

                    upvotedEntityName = "answer";
                }

            }
            sb.append(" your");
            sb.append(" " + getTypeName(upvotedEntityName));
            break;
        case ATTEMPTED:
            sb.append(" " + getTypeName(upvotedEntityName) + " that you attempted");
            break;
        default:
            break;

        }

        return sb.toString();
    }

}

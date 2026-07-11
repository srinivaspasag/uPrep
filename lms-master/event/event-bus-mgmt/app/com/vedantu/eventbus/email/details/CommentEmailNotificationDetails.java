package com.vedantu.eventbus.email.details;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.eventbus.factory.EmailTemplateFactory;

public class CommentEmailNotificationDetails extends AbstractEmailNotificationDetails {

    public CommentEmailNotificationDetails() {

        super(EmailTemplateFactory.INSTANCE.getTemplateConfigurationKey(EventType.ADD_COMMENT));
    }

    @Override
    public String getSubject() {

        JSONObject src = JSONUtils.getJSONObject(details, "src");

        StringBuilder sb = _getStringBuilderWithActorData();

        EntityType srcType = EntityType.valueOfKey(JSONUtils.getString(src, "type"));

        JSONObject root = JSONUtils.getJSONObject(src, "rootDetails");
        JSONObject parent = JSONUtils.getJSONObject(src, "parentDetails");
        String rootTypeString = JSONUtils.getString(root, ConstantsGlobal.TYPE);
        String parentTypeString = JSONUtils.getString(parent, ConstantsGlobal.TYPE);

        EntityType rootType = EntityType.valueOfKey(rootTypeString);

        EntityType parentType = EntityType.valueOfKey(parentTypeString);

        NotificationReason why = NotificationReason.valueOfKey(JSONUtils.getString(details, "why"));

        String rootTypeName = null;
        if (StringUtils.isNotEmpty(rootTypeString)) {
            rootTypeName = (rootType == EntityType.DISCUSSION) ? "doubt"
                    : ((srcType == EntityType.COMMENT && (rootType == EntityType.DISCUSSION || parentType == EntityType.DISCUSSION)) ? "answer"
                            : srcType.name().toLowerCase());
        } else {
            rootTypeName = (srcType == EntityType.SOLUTION) ? "question" : srcType.name();
        }

        String commentOrAnswerOrReply = (srcType == EntityType.DISCUSSION) ? "answered"
                : ((srcType == EntityType.COMMENT && (rootType == EntityType.DISCUSSION || parentType == EntityType.DISCUSSION)) ? "replied"
                        : "commented");

        String entityTypeName = (srcType == EntityType.DISCUSSION) ? "doubt"
                : ((srcType == EntityType.COMMENT && (rootType == EntityType.DISCUSSION || parentType == EntityType.DISCUSSION)) ? "answer"
                        : srcType.name().toLowerCase());

        sb.append(" " + commentOrAnswerOrReply + " on ");

        switch (why) {
        case OWNER:
            sb.append(" your");
            sb.append(" " + getTypeName(entityTypeName));
            break;
        case ROOT_OWNER:
            sb.append(" " + getTypeName(entityTypeName) + " added on");
            sb.append(" your ");
            sb.append(" ");
            sb.append(getTypeName(rootTypeName));
            if (root != null
                    && StringUtils.isNotEmpty(JSONUtils.getString(root, ConstantsGlobal.NAME))) {
                sb.append(" ");

                sb.append(JSONUtils.getString(root, ConstantsGlobal.NAME));
            }
            break;
        case COMMENTED:

            String commentLevelAnswerOrReply = (srcType == EntityType.DISCUSSION) ? "answered"
                    : ((srcType == EntityType.COMMENT && (rootType == EntityType.DISCUSSION || parentType == EntityType.DISCUSSION)) ? "replied"
                            : "commented");

            sb.append(" the " + getTypeName(entityTypeName) + " you also " + commentLevelAnswerOrReply);
            break;

        case FOLLOWING_SOURCE:
            sb.append(" " + getTypeName(entityTypeName)+ " you are following" );

            break;

        case ATTEMPTED:
            sb.append(" " + getTypeName(entityTypeName) + " that you attempted");
            break;
        default:
            break;
        }
        //
        // if (rootType == EntityType.DISCUSSION) {
        // if (parentType == EntityType.COMMENT) {
        // sb.append(" commented on");
        // if (why == NotificationReason.OWNER) {
        // sb.append(" your answer");
        // } else if (why == NotificationReason.ROOT_OWNER) {
        // sb.append(" an answer on your doubt");
        // } else {
        // sb.append(" an answer on the doubt you are following");
        // }
        // } else {
        // sb.append(" added an answer on");
        // switch (why) {
        // case OWNER:
        // sb.append(" your");
        // sb.append(" " + srcType);
        // break;
        // case ROOT_OWNER:
        // sb.append(" " + srcType);
        //
        // sb.append(" added on");
        // sb.append(" your ");
        // sb.append(" "
        // + getTypeName(EntityType.valueOfKey(JSONUtils.getString(root,
        // ConstantsGlobal.TYPE).toLowerCase())) + " "
        // + JSONUtils.getString(root, ConstantsGlobal.NAME));
        // break;
        // case COMMENTED:
        // sb.append(" the " + srcType + " you commented");
        // break;
        // }
        // if (why == NotificationReason.OWNER) {
        // sb.append(" your answer");
        // } else {
        // sb.append(" the doubt you are following");
        // }
        // }
        // } else {
        //
        // }

        return sb.toString();
    }
}

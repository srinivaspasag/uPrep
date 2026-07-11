package com.lms.pojos;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.user.vedantu.user.events.IndividualEmailTemplateDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractEmailNotificationDetails extends
        IndividualEmailTemplateDetails {

    public JSONObject details;

    public AbstractEmailNotificationDetails(String configurationName) {

        super(configurationName);
    }

    public static String getTypeName(String entityType) {

        EntityType value = EntityType.valueOfKey(entityType);
        if (value == EntityType.UNKNOWN) {
            return entityType;
        }
        return getTypeName(value);
    }

    public static String getTypeName(EntityType entityType) {

        switch (entityType) {
            case DISCUSSION:
                return "doubt";
            case STATUSFEED:
                return "post";
            default:
                return entityType.name().toLowerCase();
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put("details", details);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        details = JSONUtils.getJSONObject(json, "details");

    }

    @Override
    public String getSubject() {

        return null;
    }

    @Override
    public boolean verify() {

        return true;
    }

    public StringBuilder _getStringBuilderWithActorData() {

        StringBuilder sb = new StringBuilder();
        sb.append(getActor());
        return sb;
    }

    public String getActor() {
        JSONObject actor = JSONUtils.getJSONObject(details, "actor");

        StringBuilder sb = new StringBuilder();
        sb.append(JSONUtils.getString(actor, ConstantsGlobal.FIRST_NAME));
        sb.append(" ");
        if (JSONUtils.getString(actor, ConstantsGlobal.LAST_NAME) != null) {
            sb.append(JSONUtils.getString(actor, ConstantsGlobal.LAST_NAME));
        }
        return sb.toString();
    }

}

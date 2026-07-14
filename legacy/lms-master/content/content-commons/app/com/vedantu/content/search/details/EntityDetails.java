package com.vedantu.content.search.details;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class EntityDetails implements IEventDetails {

    public String         userId;
    public String         id;

    // The following entity is the 'entity' which created this event
    public SrcEntity      entity;
    public UserActionType userAction;
    public boolean        notificationEnabled;

    public EntityDetails() {

        notificationEnabled = true;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json != null) {
            userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
            id = JSONUtils.getString(json, ConstantsGlobal.ID);
            JSONObject src = JSONUtils.getJSONObject(json, ConstantsGlobal.ENTITY);
            if (src != null) {
                entity = new SrcEntity();
                entity.id = JSONUtils.getString(src, ConstantsGlobal.ID);
                entity.type = EntityType.valueOfKey(JSONUtils.getString(src, ConstantsGlobal.TYPE));
            }
            userAction = UserActionType.valueOf(JSONUtils.getString(json,
                    ConstantsGlobal.USER_ACTION));
            notificationEnabled = JSONUtils.getBoolean(json, ConstantsGlobal.NOTIFICATION_ENABLED);
        }
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return entity;
    }

    // @Override
    // public NewsActivity toNewsActivity() {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // public ITemplateDetails toITempletDetails() {
    // return null;
    // }

    public boolean enableNotifcation(boolean value) {

        notificationEnabled = value;
        return notificationEnabled;
    }

    public boolean getNotificationEnabled() {

        return notificationEnabled;
    }

    public NewsActivity toNewsActivity() throws VedantuException {

        return null;
    }

}

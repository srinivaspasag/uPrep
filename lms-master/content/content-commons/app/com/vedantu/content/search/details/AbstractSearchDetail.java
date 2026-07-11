package com.vedantu.content.search.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.IMongoAware;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public abstract class AbstractSearchDetail implements IEventDetails, IMongoAware {

    private String            action;
    public String             id;
    public VedantuRecordState recordState;
    public UserActionType     userAction;
    public long               timeCreated;
    public long               lastUpdated;
    public long               lastIndexTime;

    public boolean            isNotificationEnabled;

    public ModelBasicInfo     user;                 // this object is used to send user info to ui

    public AbstractSearchDetail() {

        lastIndexTime = System.currentTimeMillis();
        isNotificationEnabled = false;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.ID, id);

        json.put(Constants.ACTION, action);
        json.put(Constants.LAST_INDEX_TIME, lastIndexTime);
        json.put(ConstantsGlobal.LAST_UPDATED, lastUpdated);
        json.put(ConstantsGlobal.TIME_CREATED, timeCreated);
        if (recordState != null) {
            json.put(ConstantsGlobal.RECORD_STATE, recordState.name());
        }
        if (userAction != null) {
            json.put(ConstantsGlobal.USER_ACTION, userAction.name());
        }
        json.put(ConstantsGlobal.IS_NOTIFICATION_ENABLED, isNotificationEnabled);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json != null) {
            id = JSONUtils.getString(json, ConstantsGlobal.ID);
            action = JSONUtils.getString(json, Constants.ACTION);

            lastIndexTime = JSONUtils.getLong(json, Constants.LAST_INDEX_TIME);
            lastUpdated = JSONUtils.getLong(json, ConstantsGlobal.LAST_UPDATED);
            timeCreated = JSONUtils.getLong(json, ConstantsGlobal.TIME_CREATED);
            userAction = UserActionType.valueOfKey(JSONUtils.getString(json,
                    ConstantsGlobal.USER_ACTION));
            isNotificationEnabled = JSONUtils.getBoolean(json,
                    ConstantsGlobal.IS_NOTIFICATION_ENABLED);
            recordState = VedantuRecordState.valueOfKey(JSONUtils.getString(json,
                    ConstantsGlobal.RECORD_STATE));

        }

    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        if (mongoModel != null) {
            lastUpdated = mongoModel.lastUpdated;
            timeCreated = mongoModel.timeCreated;
            id = mongoModel._getStringId();
            recordState = mongoModel.recordState;
        }
    }

    public UniqueId _getUniqueId() {

        return new UniqueId(ConstantsGlobal.ID, id);
    }

    private class Constants {

        final static String ACTION          = "action";
        final static String LAST_INDEX_TIME = "lastIndexTime";
    }

    public UserActionType getUserAction() {

        return userAction;
    }

    public void setUserAction(UserActionType userAction) {

        this.userAction = userAction;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        isNotificationEnabled = value;
        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return isNotificationEnabled;
    }

    @Override
    public String toString() {

        return " [action=" + action + ", id=" + id + ", userAction=" + userAction
                + ", timeCreated=" + timeCreated + ", lastUpdated=" + lastUpdated
                + ", lastIndexTime=" + lastIndexTime + ", isNotificationEnabled="
                + isNotificationEnabled + ", user=" + user + ", toString()=" + super.toString()
                + "]";
    }

    public abstract boolean _isIndexable();
}

package com.lms.common.news;

import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class AbstractInfo implements JSONAware, Serializable {

    public String         className  = this.getClass().getName();
    public UserActionType actionType = UserActionType.UNKNOWN;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(CLASSNAME, className);
        json.put(ACTION_TYPE, actionType);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        className = JSONUtils.getString(json, CLASSNAME);
        actionType = UserActionType.valueOfKey(JSONUtils.getString(json, ACTION_TYPE));
    }

    protected static final String CLASSNAME   = "className";
    protected static final String ACTION_TYPE = "actionType";


    public abstract void populate(String userId, String orgId );
}

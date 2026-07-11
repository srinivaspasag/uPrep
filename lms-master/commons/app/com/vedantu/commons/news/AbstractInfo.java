package com.vedantu.commons.news;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

@SuppressWarnings("serial")
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

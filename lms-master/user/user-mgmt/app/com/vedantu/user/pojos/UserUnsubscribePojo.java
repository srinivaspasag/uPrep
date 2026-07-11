package com.vedantu.user.pojos;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.MailCategory;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

public class UserUnsubscribePojo implements JSONAware {

    public final static String CATEGORY = "category";

    public String              userId;
    public String              email;
    public MailCategory        category;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.USER_ID, userId);
        json.put(ConstantsGlobal.EMAIL, email);
        json.put(CATEGORY, category);
        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        email = JSONUtils.getString(json, ConstantsGlobal.EMAIL);
        category = MailCategory.valueOfKey(JSONUtils.getString(json, CATEGORY));
    }

    @Override
    public String toString() {

        return "UserUnsubscribePojo [userId=" + userId + ", email=" + email + ", category="
                + category + "]";
    }

}

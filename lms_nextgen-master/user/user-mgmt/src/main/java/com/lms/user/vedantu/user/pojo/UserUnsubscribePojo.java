package com.lms.user.vedantu.user.pojo;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.event.api.JSONAware;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Getter
@Setter
public class UserUnsubscribePojo implements JSONAware {
    private static final Logger logger = LoggerFactory.getLogger(UserUnsubscribePojo.class);

    public final static String CATEGORY = "category";

    public String              userId;
    public String              email;
    public MailCategory category;

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
        userId = getString(json, ConstantsGlobal.USER_ID);
        email = getString(json, ConstantsGlobal.EMAIL);
        category = MailCategory.valueOfKey(getString(json, CATEGORY));
    }

    public static String getString(JSONObject json, String key) {

        return getString(json, key, HardCodedConstants.emptyString);

    }
    public static String getString(JSONObject json, String key, String defaultValue) {

        String value = defaultValue;
        if (json == null) {
            return value;
        }
        try {
            value = json.getString(key);
            if (value == null) {
                value = HardCodedConstants.emptyString;
            }
        } catch (JSONException e) {
            logger.error("missing key : " + key + " in json : " + json);
        }
        return value;
    }
    @Override
    public String toString() {

        return "UserUnsubscribePojo [userId=" + userId + ", email=" + email + ", category="
                + category + "]";
    }



}

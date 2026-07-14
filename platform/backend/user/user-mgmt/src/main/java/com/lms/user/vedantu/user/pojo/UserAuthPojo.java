package com.lms.user.vedantu.user.pojo;

import common.utils.JSONUtils;
import com.lms.common.vedantu.event.api.JSONAware;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class UserAuthPojo implements JSONAware {

    public final static String USERNAME       = "userName";
    public final static String PASSWORD       = "password";
    public final static String ORG_ID         = "orgId";
    public final static String IS_MEMBER_AUTH = "isMemberAuth";

    public String              userName;
    public String              password;
    public String              orgId;
    public boolean             isMemberAuth   = false;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(USERNAME, userName);
        json.put(PASSWORD, password);
        json.put(ORG_ID, orgId);
        json.put(IS_MEMBER_AUTH, isMemberAuth);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        userName = JSONUtils.getString(json, USERNAME);
        password = JSONUtils.getString(json, PASSWORD);
        orgId = JSONUtils.getString(json, ORG_ID);
        isMemberAuth = JSONUtils.getBoolean(json, IS_MEMBER_AUTH);
    }



    @Override
    public String toString() {

        return "UserAuthPojo [userName=" + userName + ", orgId=" + orgId + ", isMemberAuth="
                + isMemberAuth + "]";
    }

}
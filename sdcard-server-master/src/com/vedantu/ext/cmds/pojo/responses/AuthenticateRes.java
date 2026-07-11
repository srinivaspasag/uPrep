package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class AuthenticateRes implements JSONAware {

    public String id;
    public String firstName;
    public String lastName;
    public String thumb;

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        firstName = JSONUtils.getString(json, "firstName");
        lastName = JSONUtils.getString(json, "lastName");
        thumb = JSONUtils.getString(json, "thumbnail");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}

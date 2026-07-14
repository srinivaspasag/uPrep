package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class ValidateOrgAppCredentialsRes implements JSONAware {

    public boolean valid;

    @Override
    public void fromJSON(JSONObject json) {

        valid = JSONUtils.getBoolean(json, "valid");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }
}

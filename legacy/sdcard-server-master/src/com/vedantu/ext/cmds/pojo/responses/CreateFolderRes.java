package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONUtils;

public class CreateFolderRes extends AbstractResourceRes {

    public long   createdOn;
    public String parent;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        createdOn = JSONUtils.getLong(json, "createdOn");
        parent = JSONUtils.getString(json, "parent");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}

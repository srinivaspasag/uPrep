package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetOrgInfoRes implements JSONAware {

    public String name;
    public String id;
    public String thumb;
    public String slug;
    public String adminUserId;
    public String key;

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        thumb = JSONUtils.getString(json, "orgThumbnail");
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        slug = JSONUtils.getString(json, ConstantGlobal.SLUG);
        adminUserId = JSONUtils.getString(json, "adminUserId");
        key = JSONUtils.getString(json, "key");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}

package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONUtils;

public class GetResourcesRes extends ListResponse<GetResourceRes> {

    public long serverTime;
    public long latestContent;

    public GetResourcesRes() {

        super(GetResourceRes.class);
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        serverTime = JSONUtils.getLong(json, "serverTime");
        latestContent = JSONUtils.getLong(json, "latestContent");
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = super.toJSON();
        JSONUtils.putValue("serverTime", serverTime, json);
        JSONUtils.putValue("latestContent", latestContent, json);
        return json;
    }

}

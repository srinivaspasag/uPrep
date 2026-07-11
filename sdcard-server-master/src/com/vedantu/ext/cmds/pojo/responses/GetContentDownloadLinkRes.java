package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetContentDownloadLinkRes implements JSONAware {

    public boolean allowed;
    public String  url;
    public String  passphrase;
    public String  encLevel;

      @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{allowed:").append(allowed).append(", url:").append(url).append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

        allowed = JSONUtils.getBoolean(json, "allowed");
        url = JSONUtils.getString(json, "url");
        passphrase = JSONUtils.getString(json, "passphrase");
        encLevel = JSONUtils.getString(json, "encLevel");

    }

    @Override
    public JSONObject toJSON() {

        // TODO Auto-generated method stub
        return null;
    }

}

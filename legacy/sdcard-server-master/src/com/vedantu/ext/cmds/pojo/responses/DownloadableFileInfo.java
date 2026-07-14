package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class DownloadableFileInfo implements JSONAware {

    public String name;
    public String entityType;
    public String entityId;
    public long   size;
    public String downloadUrl;
    public String mediaType;

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        entityType = JSONUtils.getString(json, "entityType");
        entityId = JSONUtils.getString(json, "entityId");
        size = JSONUtils.getLong(json, "size");
        downloadUrl = JSONUtils.getString(json, "downloadUrl");
        mediaType = JSONUtils.getString(json, "mediaType");
        
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}

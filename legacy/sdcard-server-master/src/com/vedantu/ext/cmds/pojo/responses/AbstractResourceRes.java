package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public abstract class AbstractResourceRes implements JSONAware {

    public String id;
    public String name;
    public String type;
    public String userId;

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        type = JSONUtils.getString(json, ConstantGlobal.TYPE);
        userId = JSONUtils.getString(json, ConstantGlobal.USER_ID);
    }

    
    public String getId() {
    
        return id;
    }

    
    public String getName() {
    
        return name;
    }

    
    public String getType() {
    
        return type;
    }

    
    public String getUserId() {
    
        return userId;
    }
}

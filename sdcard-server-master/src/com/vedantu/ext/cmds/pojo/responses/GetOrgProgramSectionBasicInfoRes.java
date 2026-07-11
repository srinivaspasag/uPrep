package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetOrgProgramSectionBasicInfoRes implements JSONAware {

    public String  accessScope;
    public String  revenueModel;
    public String  desc;
    public long    timeJoined;
    public String  orderId;
    public long    size;
    public boolean sdOnly;
    @Override
    public void fromJSON(JSONObject json) {

        
        size = JSONUtils.getLong(json, "size");
    }
    @Override
    public JSONObject toJSON() {

        // TODO Auto-generated method stub
        return null;
    }

}

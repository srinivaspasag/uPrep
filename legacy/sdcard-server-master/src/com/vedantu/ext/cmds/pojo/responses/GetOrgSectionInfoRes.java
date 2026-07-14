package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;
import com.vedantu.ext.cmds.web.JSONUtils;

public class GetOrgSectionInfoRes implements JSONAware {

    public GetOrgProgramSectionBasicInfoRes info = new GetOrgProgramSectionBasicInfoRes();

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        JSONObject programBasicInfoJson = JSONUtils.getJSONObject(json, "info");

        info.fromJSON(programBasicInfoJson);

    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

}

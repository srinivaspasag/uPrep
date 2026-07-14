package com.lms.question;

import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.pojos.QuestionFormat;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SolutionFormat extends QuestionFormat implements JSONAware {

    public String globalSolId;

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        this.globalSolId = JSONUtils.getString(json, "globalSolId");
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("globalSolId", globalSolId);
        return json;
    }


}

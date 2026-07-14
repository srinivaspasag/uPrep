package com.vedantu.content.news.pojos;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class AddSolutionInfo extends EntityNewsInfo {

    /**
     * 
     */
    private static final long     serialVersionUID = 1L;
    protected static final String SOLUTION         = "solution";
    protected static final String CONTENT          = "content";
    public SrcEntity              solution;
    public String                 content;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(CLASSNAME, className);
        json.put(ACTION_TYPE, actionType);
        json.put(CONTENT, content);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        solution = new SrcEntity();
        solution = (SrcEntity) JSONUtils.getJSONAware(solution, json, SOLUTION);
        content = JSONUtils.getString(json, SOLUTION);
    }

}

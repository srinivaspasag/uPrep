package com.vedantu.cmds.pojos.content.question;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

public class SolutionFormat extends QuestionFormat implements JSONAware {

	public String	globalSolId;

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

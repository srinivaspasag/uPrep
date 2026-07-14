package com.vedantu.commons.events.apis;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONAware {

	JSONObject toJSON() throws JSONException;

	void fromJSON(JSONObject json);
}

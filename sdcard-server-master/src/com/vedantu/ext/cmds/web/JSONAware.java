package com.vedantu.ext.cmds.web;

import org.json.JSONObject;

public interface JSONAware {

	public void fromJSON(JSONObject json);

	public JSONObject toJSON();

}

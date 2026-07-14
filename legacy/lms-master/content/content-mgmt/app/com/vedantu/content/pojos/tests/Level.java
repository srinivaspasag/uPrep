package com.vedantu.content.pojos.tests;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.constants.QuestionConstants;
import com.vedantu.content.enums.Difficulty;

public class Level implements Cloneable, JSONAware {

	public String		name;
	public String		id;
	public Difficulty	difficulty;

	@Override
	public int hashCode() {
		return ((name == null) ? 0 : name.hashCode())
				+ ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Level other = (Level) obj;
		return name != null && other.name.equals(name)
				&& (StringUtils.equals(id, other.id));
	}

	@Override
	public Level clone() throws CloneNotSupportedException {
		return (Level) super.clone();
	}

	@Override
	public String toString() {
		return "{name:" + name + ", difficulty:" + difficulty + "}";
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(ConstantsGlobal.NAME, name);
		json.put(ConstantsGlobal.ID, id);
		json.put(QuestionConstants.DIFFICULTY, difficulty.name());
		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		name = JSONUtils.getString(json, ConstantsGlobal.NAME);
		id = JSONUtils.getString(json, ConstantsGlobal.ID);
		difficulty = Difficulty.valueOfKey(JSONUtils.getString(json,
				QuestionConstants.DIFFICULTY));
	}
}

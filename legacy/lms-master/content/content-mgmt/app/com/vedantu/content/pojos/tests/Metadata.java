package com.vedantu.content.pojos.tests;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.constants.QuestionConstants;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;

public class Metadata implements JSONAware {
	private transient String	errorLine;		// only
												// UI
	public Difficulty			difficulty;
	public String				name;
	public String				origRefName;
	public QuestionType			type;
	public Set<String>			tags;
	public Set<String>			brdIds;
	public Set<String>			targetIds;

	public Metadata(Difficulty difficulty, String title, String source,
			QuestionType type, String questionSetName) {
		this.name = questionSetName;
		this.origRefName = source;

		this.difficulty = difficulty;
		this.type = type;
	}

	public Metadata() {
		// TODO Auto-generated constructor stub
	}

	// public Set<Center> getCentres() {
	// return centers;
	// }

	// public void setCentres(Set<Center> centres) {
	// this.centers = centres;
	// }

	public String __getErrorLine() {
		return errorLine;
	}

	public void __setErrorLine(String errorLine) {
		this.errorLine = errorLine;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		if (difficulty != null) {
			json.put(ConstantsGlobal.DIFFICULTY, difficulty.name());
		}

		json.put(ConstantsGlobal.NAME, name);
		json.put(ConstantsGlobal.SOURCE, origRefName);
		json.put(ConstantsGlobal.TYPE, type);

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {

		String difficultyString = JSONUtils.getString(json,
				QuestionConstants.DIFFICULTY);
		difficulty = Difficulty.valueOfKey(difficultyString);

		name = JSONUtils.getString(json, ConstantsGlobal.NAME);
		origRefName = JSONUtils.getString(json, ConstantsGlobal.SOURCE);
		String typeString = JSONUtils.getString(json, ConstantsGlobal.TYPE);
		type = QuestionType.valueOfKey(typeString);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metdata :");
		builder.append(" difficulty");
		builder.append(difficulty);
		builder.append("  brdIds");
		builder.append(brdIds);
		builder.append(" targetIds");
		builder.append(targetIds);

		return builder.toString();
	}
}

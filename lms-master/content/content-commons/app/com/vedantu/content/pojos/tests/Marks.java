package com.vedantu.content.pojos.tests;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;

public class Marks implements JSONAware, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int positive;
	public int negative;
	public QuestionResultStatus status;

	private static transient final String POSITIVE = "positive";
	private static transient final String NEGATIVE = "negative";

	public Marks() {
		super();
	}

	public Marks(int positive, int negative) {
		this.positive = positive;
		this.negative = negative;
		this.status = QuestionResultStatus.ACTIVE;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(POSITIVE, positive);
		json.put(NEGATIVE, negative);
		return json;
	}

	@Override
	public void fromJSON(JSONObject json)  {
		positive = JSONUtils.getInt(json, POSITIVE);
		negative = JSONUtils.getInt(json, NEGATIVE);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Marks [positive:").append(positive)
				.append(", negative:").append(negative).append("]");
		return builder.toString();
	}

}

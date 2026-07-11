package com.vedantu.cmds.pojos.content.solution.metadata;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.commons.util.LatexProcessor;
import com.vedantu.cmds.pojos.content.question.OptionFormat;
import com.vedantu.commons.utils.JSONUtils;


public class NumericSolutionInfo extends SolutionInfo {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public String	answer;

	public NumericSolutionInfo() {
		this(new OptionFormat(), new String());
	}

	public NumericSolutionInfo(OptionFormat op, String answer) {
		super(op);
		this.answer = answer;
	}

	@Override
	public void addHook() {
		super.addHook();
		if (answer != null && !answer.isEmpty()) {

			answer = LatexProcessor.addHookToLatex(answer);
		}

	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		answer = JSONUtils.getString(json, ANSWER, StringUtils.EMPTY);

	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put(ANSWER, this.answer);

		return json;
	}
}

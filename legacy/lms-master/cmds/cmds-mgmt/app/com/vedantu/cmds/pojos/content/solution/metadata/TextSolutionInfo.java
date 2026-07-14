package com.vedantu.cmds.pojos.content.solution.metadata;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.commons.util.LatexProcessor;
import com.vedantu.cmds.pojos.content.question.OptionFormat;
import com.vedantu.commons.utils.JSONUtils;

@SuppressWarnings("serial")
public class TextSolutionInfo extends SolutionInfo {
	public String	answer;

	public TextSolutionInfo() {
		this(new OptionFormat(), new String());
	}

	public TextSolutionInfo(OptionFormat op, String answer) {
		super(op);
		this.answer = answer;
	}

	@Override
	public void addHook() {
		super.addHook();
		if (StringUtils.isNotEmpty(answer)) {
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

package com.vedantu.cmds.pojos.content.solution.metadata;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.cmds.commons.util.LatexProcessor;
import com.vedantu.cmds.pojos.content.question.OptionFormat;
import com.vedantu.commons.utils.JSONUtils;

@SuppressWarnings("serial")
public class MCQsolutionInfo extends SolutionInfo {

	public List<String>	answer;

	public MCQsolutionInfo() {
		this(new OptionFormat(), new ArrayList<String>());
	}

	public MCQsolutionInfo(OptionFormat op, List<String> answers) {
		super(op);
		this.answer = answers;
	}

	@Override
	public void addHook() {
		super.addHook();
		if (answer != null && !answer.isEmpty()) {
			List<String> newAnswers = new ArrayList<String>();
			for (String option : answer) {
				newAnswers.add(LatexProcessor.addHookToLatex(option));
			}
			answer = newAnswers;
		}

	}

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		answer = JSONUtils.getList(json, ANSWER);
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put(ANSWER, answer);
		return json;
	}

}

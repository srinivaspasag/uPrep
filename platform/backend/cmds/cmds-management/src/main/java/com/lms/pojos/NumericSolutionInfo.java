package com.lms.pojos;

import com.lms.cmds.SolutionInfo;
import com.lms.common.utils.LatexProcessor;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.question.OptionFormat;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class NumericSolutionInfo extends SolutionInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String answer;

    public NumericSolutionInfo() {
        this(new OptionFormat(), "");
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
        answer = JSONUtils.getString(json, ANSWER, HardCodedConstants.emptyString);

    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put(ANSWER, this.answer);

        return json;
    }
}

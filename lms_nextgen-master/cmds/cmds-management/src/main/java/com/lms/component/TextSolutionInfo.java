package com.lms.component;

import com.lms.cmds.SolutionInfo;
import com.lms.common.utils.LatexProcessor;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.question.OptionFormat;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class TextSolutionInfo extends SolutionInfo {
    public String answer;

    public TextSolutionInfo() {
        this(new OptionFormat(), "");
    }

    public TextSolutionInfo(OptionFormat op, String answer) {
        super(op);
        this.answer = answer;
    }

    @Override
    public void addHook() {
        super.addHook();
        if (StringUtils.isEmpty(answer)) {
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

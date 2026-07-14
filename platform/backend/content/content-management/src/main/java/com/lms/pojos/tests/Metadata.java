package com.lms.pojos.tests;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.constants.QuestionConstants;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionType;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class Metadata implements JSONAware {
    // UI
    public Difficulty difficulty;
    public String name;
    public String origRefName;
    public QuestionType type;
    public Set<String> tags;
    public Set<String> brdIds;
    public Set<String> targetIds;
    private transient String errorLine;        // only

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

package com.lms.pojos.tests;

import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Marks implements JSONAware, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static transient final String POSITIVE = "positive";
    private static transient final String NEGATIVE = "negative";
    public int positive;
    public int negative;
    public QuestionResultStatus status;

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
    public void fromJSON(JSONObject json) {
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

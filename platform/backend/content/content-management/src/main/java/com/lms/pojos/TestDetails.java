package com.lms.pojos;

import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.enums.QuestionType;
import com.lms.pojos.tests.Marks;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestDetails implements Serializable, JSONAware {
    private static final Logger logger = LoggerFactory.getLogger(TestDetails.class);
    private static final long serialVersionUID = 1L;

    public String id;
    public String name;
    public QuestionType type;
    public int qusCount;
    public int currentQuesCount;
    public Marks marks;                        // this is temp
    public List<String> qIds;

    public TestDetails() {
        super();
    }

    public TestDetails(QuestionType type, int qusCount, int positiveMarks,
                       int negativeMarks) {
        super();
        this.type = type;
        this.qusCount = qusCount;
        this.marks = new Marks(positiveMarks, negativeMarks);
        this.qIds = new ArrayList<String>();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.TYPE, type);
        json.put(ConstantsGlobal.QUS_COUNT, qusCount);
        if (marks != null) {
            json.put(ConstantsGlobal.MARKS, marks.toJSON());
        }
        json.put(ConstantsGlobal.QIDS, qIds);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        type = QuestionType.valueOfKey(JSONUtils.getString(json,
                ConstantsGlobal.TYPE));
        qusCount = JSONUtils.getInt(json, ConstantsGlobal.QUS_COUNT);

        marks = new Marks(0, 0);
        JSONObject jsonMarks = JSONUtils.getJSONObject(json,
                ConstantsGlobal.MARKS);
        marks.fromJSON(jsonMarks);

        qIds = JSONUtils.getList(json, ConstantsGlobal.QIDS);
    }

    @Override
    public int hashCode() {
        return ((type == null) ? 0 : type.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        TestDetails other = (TestDetails) obj;
        return type == other.type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TestDetails [type:").append(type).append(", qusCount:")
                .append(qusCount).append(", marks:").append(marks)
                .append(", qIds:").append(qIds).append("]");
        return builder.toString();
    }

}

package com.vedantu.content.pojos.tests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.enums.QuestionType;

public class TestDetails implements JSONAware, Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
    public String              id;              
    public String              name;   
	public QuestionType			type;
	public int					qusCount;
	public int                 currentQuesCount;
	public Marks				marks;						// this is temp
	public List<String>			qIds;
    public int                  maxQuestionsTobeAttempted;

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
		json.put(ConstantsGlobal.MAX_QUESTIONS_TO_BE_ATTEMPTED, maxQuestionsTobeAttempted);
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
		maxQuestionsTobeAttempted = JSONUtils.getInt(json, ConstantsGlobal.MAX_QUESTIONS_TO_BE_ATTEMPTED);
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
				.append(qusCount).append(", maxQuestionsTobeAttempted:")
				.append(maxQuestionsTobeAttempted).append(", marks:").append(marks)
				.append(", qIds:").append(qIds).append("]");
		return builder.toString();

	}

	

}

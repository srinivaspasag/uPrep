package com.vedantu.content.pojos.analytics;

import java.util.List;

import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.mongo.VedantuRecordState;

public class QuestionAnalyticsExtendedInfo extends ModelExtendedInfo {

	public QuestionType type;

	public QuestionMeasures measures;

	public List<AnswerGivenCount> answerGivenCounts;

	public AnswerGivenCount userAnswerGivenCount = new AnswerGivenCount();

	public AnswerGivenCount correctAnswerGivenCount = new AnswerGivenCount();

	public AnswerCorrectness isUserAnswerCorrect;

	public QuestionAnalyticsExtendedInfo(String id,
			VedantuRecordState recordState, String name, long timeCreated,
			long lastUpdated, QuestionMeasures measures,
			List<AnswerGivenCount> answerGivenCounts) {
		super(id, recordState, name, timeCreated, lastUpdated);
		this.measures = measures;
		this.answerGivenCounts = answerGivenCounts;
	}

}

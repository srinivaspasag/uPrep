package com.vedantu.content.utils;

import java.util.Comparator;

import com.vedantu.content.pojos.responses.analytics.answers.QuestionAttemptStatsInfoDetail;

public class EntityQuestionAttemptInfoComparator implements
		Comparator<QuestionAttemptStatsInfoDetail> {

	@Override
	public int compare(QuestionAttemptStatsInfoDetail o1, QuestionAttemptStatsInfoDetail o2) {
		return o1.qusNo > o2.qusNo ? 1 : -1;
	}

}

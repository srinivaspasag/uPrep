package com.lms.utils;

import com.lms.pojos.responce.analytics.answers.QuestionAttemptStatsInfoDetail;

import java.util.Comparator;

public class EntityQuestionAttemptInfoComparator implements
        Comparator<QuestionAttemptStatsInfoDetail> {

    @Override
    public int compare(QuestionAttemptStatsInfoDetail o1, QuestionAttemptStatsInfoDetail o2) {
        return o1.qusNo > o2.qusNo ? 1 : -1;
    }

}

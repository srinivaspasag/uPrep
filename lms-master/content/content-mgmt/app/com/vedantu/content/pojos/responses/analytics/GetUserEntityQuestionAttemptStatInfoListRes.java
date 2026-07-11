package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.pojos.responses.analytics.answers.QuestionAttemptStatsInfo;

public class GetUserEntityQuestionAttemptStatInfoListRes extends
        ListResponse<QuestionAttemptStatsInfo> {

    public long endTime;
    public long startTime;
    public long timeTaken;
    public boolean analyticsGenerated;
}

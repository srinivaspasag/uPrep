package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojos.responce.analytics.answers.QuestionAttemptStatsInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserEntityQuestionAttemptStatInfoListRes extends
        ListResponse<QuestionAttemptStatsInfo> {
    public long endTime;
    public long startTime;
    public long timeTaken;
}

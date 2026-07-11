package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojos.responce.analytics.answers.QuestionAttemptStatsInfoDetail;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetEntityQuestionAttemptInfoListRes extends
        ListResponse<QuestionAttemptStatsInfoDetail> {

}

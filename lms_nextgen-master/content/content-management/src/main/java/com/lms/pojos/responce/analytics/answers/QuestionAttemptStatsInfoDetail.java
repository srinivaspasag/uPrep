package com.lms.pojos.responce.analytics.answers;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.models.QuestionMeasures;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuestionAttemptStatsInfoDetail implements IListResponseObj {
    public int qusNo;
    public QuestionSearchIndexDetails info;
    public QuestionMeasures measures;

    public QuestionAttemptStatsInfoDetail() {
        super();
    }

    public QuestionAttemptStatsInfoDetail(int qusNo, QuestionSearchIndexDetails info,
                                          QuestionMeasures measures) {
        this.qusNo = qusNo;
        this.info = info;
        this.measures = measures;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{qusNo:");
        builder.append(qusNo);
        builder.append(", info:");
        builder.append(info);
        builder.append(", measures:");
        builder.append(measures);
        builder.append("}");
        return builder.toString();
    }

}

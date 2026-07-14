package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.AnswerCorrectness;
import com.lms.enums.QuestionType;
import com.lms.models.QuestionMeasures;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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

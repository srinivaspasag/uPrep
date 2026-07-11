package com.lms.models.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.Question;
import com.lms.models.QuestionMeasures;
import com.lms.pojos.analytics.AnswerGivenCount;
import com.lms.pojos.analytics.QuestionAnalyticsExtendedInfo;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Document(value = "questionanalytics")
@CompoundIndexes(@CompoundIndex(name = "qId, parentEntity.type, parentEntity.id"))
public class QuestionAnalytics extends VedantuBaseMongoModel {
    private static final Logger logger = LoggerFactory.getLogger(QuestionAnalytics.class);
    public String qId;

    // test, challenge, assignment
    public SrcEntity parentEntity;

    public QuestionMeasures measures;

    public Map<String, Long> answerGivenCount = new HashMap<String, Long>();

    public QuestionAnalytics() {

        super();
    }

    public QuestionAnalytics(String qId, SrcEntity parentEntity, long attempts, long correct,
                             long partial, long incorrect, long left, long timeTaken) {

        super();
        this.qId = qId;
        this.parentEntity = parentEntity;
        this.measures = new QuestionMeasures(attempts, correct, partial, incorrect, left, timeTaken);
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        List<AnswerGivenCount> answerGivenCounts = AnswerGivenCount
                .toAnswerGivenCounts(this.answerGivenCount);

        QuestionAnalyticsExtendedInfo info = new QuestionAnalyticsExtendedInfo(_getStringId(),
                recordState, "", timeCreated, lastUpdated, measures,
                answerGivenCounts);

        return info;
    }

    public long findAnswerCount(Question question, List<String> answer,
                                Map<String, List<String>> matrixAnswerGiven) {


        String answerKey = AnswerGivenCount.toAnswerKey(question, answer, matrixAnswerGiven);
        Long answerCount = null;
        if (!(answerKey).isEmpty()) {
            answerKey = answerKey.replace(AnswerGivenCount.NUMERIC_DOT_REPLACER, ".").trim();
            logger.debug("answerGivenCountMap: " + answerGivenCount + ", key:" + answerKey);
            answerCount = this.answerGivenCount.get(answerKey);
        }
        return null != answerCount ? answerCount : 0L;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qId:");
        builder.append(qId);
        builder.append(", parentEntity:");
        builder.append(parentEntity);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", answerGivenCount:");
        builder.append(answerGivenCount);
        builder.append(", id:");
        builder.append(id);
        builder.append(", timeCreated:");
        builder.append(timeCreated);
        builder.append(", lastUpdated:");
        builder.append(lastUpdated);
        builder.append(", recordState:");
        builder.append(recordState);
        builder.append("}");
        return builder.toString();
    }

}

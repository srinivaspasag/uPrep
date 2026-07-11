package com.vedantu.content.models.analytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.content.models.Question;
import com.vedantu.content.pojos.analytics.AnswerGivenCount;
import com.vedantu.content.pojos.analytics.QuestionAnalyticsExtendedInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "questionanalytics", noClassnameStored = true)
@Indexes(@Index(value = "qId, parentEntity.type, parentEntity.id"))
public class QuestionAnalytics extends VedantuBaseMongoModel {

    private static final ALogger LOGGER           = Logger.of(QuestionAnalytics.class);
    public String                qId;

    // test, challenge, assignment
    public SrcEntity             parentEntity;

    public QuestionMeasures      measures;

    public Map<String, Long>     answerGivenCount = new HashMap<String, Long>();

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
                recordState, StringUtils.EMPTY, timeCreated, lastUpdated, measures,
                answerGivenCounts);

        return info;
    }

    public long findAnswerCount(Question question, List<String> answer,
            Map<String, List<String>> matrixAnswerGiven) {

        String answerKey = AnswerGivenCount.toAnswerKey(question, answer, matrixAnswerGiven);
        Long answerCount = null;
        if (StringUtils.isNotEmpty(answerKey)) {
            answerKey = answerKey.replace(AnswerGivenCount.NUMERIC_DOT_REPLACER, ".").trim();
            LOGGER.debug("answerGivenCountMap: " + answerGivenCount + ", key:" + answerKey);
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

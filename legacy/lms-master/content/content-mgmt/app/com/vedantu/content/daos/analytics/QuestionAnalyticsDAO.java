package com.vedantu.content.daos.analytics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.analytics.QuestionAnalytics;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.content.pojos.analytics.AnswerGivenCount;
import com.vedantu.mongo.VedantuBasicDAO;

public class QuestionAnalyticsDAO extends VedantuBasicDAO<QuestionAnalytics, ObjectId> {

    private static final ALogger             LOGGER   = Logger.of(QuestionAnalyticsDAO.class);

    public static final QuestionAnalyticsDAO INSTANCE = new QuestionAnalyticsDAO();

    private QuestionAnalyticsDAO() {

        super(QuestionAnalytics.class);
    }

    public QuestionAnalytics getAnalytics(String qId, SrcEntity parentEntity) {

        LOGGER.debug("getAnalytics qId: " + qId + ", parentEntity: ");

        QuestionAnalytics questionAnalytics = getQuery().filter("qId", qId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id).get();
        LOGGER.info("getAnalytics questionAnalytics: " + questionAnalytics);
        if (questionAnalytics != null) {
            Map<String, Long> answerGivenCountMap = new TreeMap<String, Long>();
            for (Entry<String, Long> entry : questionAnalytics.answerGivenCount.entrySet()) {
                answerGivenCountMap.put(
                        entry.getKey().replace(AnswerGivenCount.NUMERIC_DOT_REPLACER, "."),
                        entry.getValue());
            }
            questionAnalytics.answerGivenCount = answerGivenCountMap;
        }
        return questionAnalytics;
    }

    public boolean addAnalytics(String qId, SrcEntity parentEntity, QuestionMeasures measures,
            String answerGivenKey) {

        UpdateOperations<QuestionAnalytics> updateOps = getDS()
                .createUpdateOperations(QuestionAnalytics.class)
                .inc("measures.attempts", measures.attempts)
                .inc("measures.correct", measures.correct)
                .inc("measures.partial", measures.partial)
                .inc("measures.incorrect", measures.incorrect).inc("measures.left", measures.left)
                .inc("measures.timeTaken", measures.timeTaken);
        if (StringUtils.isNotEmpty(answerGivenKey)) {
            // . is replace with _ as map key can not contain . in mongo
            answerGivenKey = answerGivenKey.replace(".", AnswerGivenCount.NUMERIC_DOT_REPLACER);
            updateOps.inc("answerGivenCount." + answerGivenKey, measures.attempts);
        }

        Query<QuestionAnalytics> query = getQuery().filter("qId", qId)
                .filter("parentEntity.type", parentEntity.type)
                .filter("parentEntity.id", parentEntity.id);

        final boolean createIfNotPresent = true;
        UpdateResults<QuestionAnalytics> updateResult = getDS().update(query, updateOps,
                createIfNotPresent);

        log(LOGGER, updateResult);

        return !updateResult.getHadError();
    }

}

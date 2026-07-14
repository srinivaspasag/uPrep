package com.vedantu.content.pojos.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.Question;

public class AnswerGivenCount {

    public static final String NUMERIC_DOT_REPLACER = "#";
    public List<String>        answerGiven;
    public long                count;

    public AnswerGivenCount() {

        super();
        answerGiven = new ArrayList<String>();
    }

    public AnswerGivenCount(List<String> answerGiven, long count) {

        super();
        this.answerGiven = answerGiven;
        this.count = count;
    }

    private static final String ANSWER_KEY_SEPARATOR = "_";

    public static String toAnswerKey(Question question, List<String> answers,
            Map<String, List<String>> matrixAnswerGiven) {

        if (answers == null || null == question || null == question.type
                || !question.type.isJudgeable()) {
            return null;
        } else {
            String key = StringUtils.join(answers,ANSWER_KEY_SEPARATOR);
//            String key = (question.type != QuestionType.MATRIX ? StringUtils.join(answers,
//                    ANSWER_KEY_SEPARATOR) : toAnswerKey(matrixAnswerGiven));
            return StringUtils.isEmpty(key) ? null : key.replace(".", NUMERIC_DOT_REPLACER);
        }
    }

    private static String toAnswerKey(Map<String, List<String>> matrixAnswerGiven) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (Entry<String, List<String>> entry : matrixAnswerGiven.entrySet()) {
            if (!start) {
                sb.append(",");
            }
            sb.append(entry.getKey());
            sb.append("#");
            sb.append(StringUtils.join(entry.getValue(), ANSWER_KEY_SEPARATOR));

            start = false;
        }
        return sb.toString();
    }

    // TODO: right a method to convert from answerKey to martixAnswerGiven
    // format
    private static List<String> toAnswers(String answerKey) {

        if (StringUtils.isEmpty(answerKey)) {
            return null;
        }
        return Arrays.asList(StringUtils.split(answerKey, ANSWER_KEY_SEPARATOR));
    }

    public static List<AnswerGivenCount> toAnswerGivenCounts(Map<String, Long> answerGivenCountMap) {

        List<AnswerGivenCount> result = new ArrayList<AnswerGivenCount>();

        if (MapUtils.isNotEmpty(answerGivenCountMap)) {

            List<String> answerKeyList = new ArrayList<String>(answerGivenCountMap.keySet());
            Collections.sort(answerKeyList);

            for (String answerKey : answerKeyList) {

                List<String> answerGiven = toAnswers(answerKey);
                Long count = answerGivenCountMap.get(answerKey);
                result.add(new AnswerGivenCount(answerGiven, count));

            }
        }

        return result;
    }

}

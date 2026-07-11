package com.lms.pojos;

import com.lms.models.Question;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
@Setter
public class AnswerGivenCount {
    public static final String NUMERIC_DOT_REPLACER = "#";
    private static final String ANSWER_KEY_SEPARATOR = "_";
    public List<String> answerGiven;
    public long count;

    public AnswerGivenCount() {

        super();
        answerGiven = new ArrayList<String>();
    }

    public AnswerGivenCount(List<String> answerGiven, long count) {

        super();
        this.answerGiven = answerGiven;
        this.count = count;
    }

    public static String toAnswerKey(Question question, List<String> answers,
                                     Map<String, List<String>> matrixAnswerGiven) {

        if (answers == null || null == question || null == question.type
                || !question.type.isJudgeable()) {
            return null;
        } else {
            String key = answers + ANSWER_KEY_SEPARATOR;
//            String key = (question.type != QuestionType.MATRIX ? StringUtils.join(answers,
//                    ANSWER_KEY_SEPARATOR) : toAnswerKey(matrixAnswerGiven));
            return (key).isEmpty() ? null : key.replace(".", NUMERIC_DOT_REPLACER);
        }
    }

    private static String toAnswerKey(Map<String, List<String>> matrixAnswerGiven) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (Map.Entry<String, List<String>> entry : matrixAnswerGiven.entrySet()) {
            if (!start) {
                sb.append(",");
            }
            sb.append(entry.getKey());
            sb.append("#");
            sb.append(entry.getValue() + ANSWER_KEY_SEPARATOR);

            start = false;
        }
        return sb.toString();
    }

    // TODO: right a method to convert from answerKey to martixAnswerGiven
    // format
    private static List<String> toAnswers(String answerKey) {

        if ((answerKey).isEmpty()) {
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

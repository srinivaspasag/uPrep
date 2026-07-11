package com.vedantu.content.daos;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.Answer;
import com.vedantu.content.models.Question;
import com.vedantu.mongo.VedantuBasicDAO;

public class AnswerDAO extends VedantuBasicDAO<Answer, ObjectId> {
    private static final ALogger  LOGGER   = Logger.of(AnswerDAO.class);

    public static final AnswerDAO INSTANCE = new AnswerDAO();

    private AnswerDAO() {
        super(Answer.class);
    }

    public Answer addAnswer(String userId, String qId, QuestionType qType, List<String> answers,
            Map<String, List<String>> gridAnswer) throws VedantuException {
        Answer ans = new Answer(qId, userId, qType);
        ans.answer = answers;
        ans.matrixAnswer = gridAnswer;
        LOGGER.debug("saving answer : " + ans);
        save(ans);
        return ans;
    }

    public Answer getQuestionAnswer(String qId) {
        return getQuery().filter(ConstantsGlobal.QID, qId).get();
    }

    public Map<String, Answer> getQuestionAnswerMap(Collection<String> qIds) {
        Map<String, Answer> answerMap = new HashMap<String, Answer>();
        Query<Answer> query = getQuery().field(ConstantsGlobal.QID).in(qIds);
        List<Answer> answers = query.asList();
        for (Answer ans : answers) {
            answerMap.put(ans.qId, ans);
        }
        for (String qId : qIds) {
            if (!answerMap.containsKey(qId)) {
                Question question = QuestionDAO.INSTANCE.getById(qId);
                if (question.answerId != null && !question.answerId.isEmpty()) {
                    Answer answer = AnswerDAO.INSTANCE.getById(question.answerId);
                    answerMap.put(qId, answer);
                }
            }
        }
        return answerMap;
    }

    public Answer getByQuestionId(String questionId) {
        Answer answer = getQuery().filter("qId", questionId).get();
        if (answer == null) {
            LOGGER.error("Cannot find answer with the quesiton id :" + questionId);
        }
        return answer;
    }
}

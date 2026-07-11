package com.vedantu.content.daos;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.LatexType;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.Question;
import com.vedantu.mongo.MongoManager;

public class QuestionDAO extends AbstractAttemptableDAO<Question, ObjectId> {

    private static final ALogger    LOGGER   = Logger.of(QuestionDAO.class);

    public static final QuestionDAO INSTANCE = new QuestionDAO();

    private QuestionDAO() {

        super(Question.class);
    }

    public Question
            addQuestion(String userId, String content, String source, List<String> brdIds,
                    QuestionType type, List<String> options, List<String> targetIds,
                    List<String> tags, Map<String, List<String>> grid, Scope scope,
                    Difficulty difficulty, SrcEntity contentSrc) throws VedantuException {

        Question qus = new Question(content, userId, type, source, new HashSet<String>(),
                LatexType.MATHJAX, options);
        qus.matrix = grid;
        qus.difficulty = difficulty;
        qus.contentSrc = contentSrc;
        qus.scope = scope;
        qus.addBoards(brdIds);
        qus.addTargets(targetIds);
        qus.addTags(tags);
        LOGGER.debug("saving question : " + qus);
        save(qus);

        return qus;
    }

    public Question getQuestion(String id, List<String> fields) throws VedantuException {

        Question question = findOne(new BasicDBObject(FIELD_ID, new ObjectId(id)),
                MongoManager.getFieldsDBObject(fields, MongoManager.INCLUDE_FIELD));
        if (question == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND);
        }
        return question;
    }

    public Question getQuestion(String id) throws VedantuException {

        Question question = getById(id);
        if (question == null) {
            throw new VedantuException(VedantuErrorCode.QUESTION_NOT_FOUND,
                    "no question found with id:" + id);
        }
        return question;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        return null;
    }

    public Question getByCmdsQuestionId(String cmdsQuestionId) {
        Question question = getQuery().filter("cmdsQId", cmdsQuestionId).get();
        if (question == null) {
            LOGGER.error("Cannot find question with the cmds quesiton id :" + cmdsQuestionId);
        }

        return question;
    }
}

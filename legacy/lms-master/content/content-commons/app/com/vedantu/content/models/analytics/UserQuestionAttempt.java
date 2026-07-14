package com.vedantu.content.models.analytics;

import java.util.List;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userquestionattempts", noClassnameStored = true)
@Indexes({ @Index(value = "userId, parentEntity.type, parentEntity.id, qId"),
        @Index(value = "attemptId") })
public class UserQuestionAttempt extends VedantuBaseMongoModel {

    public String                    userId;
    public String                    userName;
    public String                    userProfilePic;
    public String                    attemptId;

    // test, challenge, assignment
    // will be question in case of question
    public SrcEntity                 parentEntity;

    public String                    qId;
    public QuestionType              type;
    public List<String>              answerGiven;
    public Map<String, List<String>> matrixAnswerGiven;

    public boolean                   isJudgeable;
    public AnswerCorrectness         isCorrect;
    public double                       score;
    // weather this score is being considered for test/challenge
    public boolean                   isFinalized;
    public long                      timeTaken;

    public UserQuestionAttempt() {

        super();
    }

    public UserQuestionAttempt(String userId, String attemptId, SrcEntity parentEntity, String qId,
            List<String> answerGiven, boolean isJudgeable, AnswerCorrectness isCorrect, double score,
            long timeTaken) {

        super();
        this.userId = userId;
        this.attemptId = attemptId;
        this.parentEntity = parentEntity;
        this.qId = qId;
        this.answerGiven = answerGiven;
        this.isJudgeable = isJudgeable;
        this.isCorrect = isCorrect;
        this.score = score;
        this.timeTaken = timeTaken;
    }

    @Override
    public String toString() {
        return "UserQuestionAttempt{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userProfilePic='" + userProfilePic + '\'' +
                ", attemptId='" + attemptId + '\'' +
                ", parentEntity=" + parentEntity +
                ", qId='" + qId + '\'' +
                ", type=" + type +
                ", answerGiven=" + answerGiven +
                ", matrixAnswerGiven=" + matrixAnswerGiven +
                ", isJudgeable=" + isJudgeable +
                ", isCorrect=" + isCorrect +
                ", score=" + score +
                ", isFinalized=" + isFinalized +
                ", timeTaken=" + timeTaken +
                ", id=" + id +
                '}';
    }
}

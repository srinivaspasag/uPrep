package com.lms.models.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.AnswerCorrectness;
import com.lms.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(value = "userquestionattempts")
@Setter
@Getter
public class UserQuestionAttempt extends VedantuBaseMongoModel
{
    @Indexed
    public String                    userId;
    public String                    userName;
    public String                    userProfilePic;
    @Indexed
    public String                    attemptId;

    // test, challenge, assignment
    // will be question in case of question
    @Indexed
    public SrcEntity parentEntity;
    @Indexed
    public String                    qId;
    public QuestionType type;
    public List<String> answerGiven;
    public Map<String, List<String>> matrixAnswerGiven;

    public boolean                   isJudgeable;
    public AnswerCorrectness isCorrect;
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

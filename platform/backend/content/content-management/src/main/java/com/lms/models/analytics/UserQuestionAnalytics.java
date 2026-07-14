package com.lms.models.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.AnswerCorrectness;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "userquestionanalytics")
@CompoundIndexes({@CompoundIndex(name = "userId, parentEntity, qId"),
        @CompoundIndex(name = "attemptId"),
        @CompoundIndex(name = "qId,isCorrect,parentEntity")})
@Setter
@Getter
public class UserQuestionAnalytics extends UserQuestionAttempt {

    public UserQuestionAnalytics() {

        super();
    }

    public UserQuestionAnalytics(String userId, String attemptId, SrcEntity parentEntity,
                                 String qId, List<String> answerGiven, boolean isJudgeable, AnswerCorrectness isCorrect,
                                 double score, long timeTaken) {

        super(userId, attemptId, parentEntity, qId, answerGiven, isJudgeable, isCorrect, score,
                timeTaken);
        this.isFinalized = true;
    }

}

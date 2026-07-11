package com.vedantu.content.models.analytics;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.AnswerCorrectness;

@Entity(value = "userquestionanalytics", noClassnameStored = true)
@Indexes({ @Index(value = "userId, parentEntity, qId"),
           @Index(value = "attemptId"),
           @Index(value = "qId,isCorrect,parentEntity")})
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

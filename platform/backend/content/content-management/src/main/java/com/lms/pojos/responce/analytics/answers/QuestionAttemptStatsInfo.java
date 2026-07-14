package com.lms.pojos.responce.analytics.answers;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.enums.AnswerCorrectness;
import com.lms.enums.AttemptStatus;
import com.lms.models.ContentSearchDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestionAttemptStatsInfo implements IListResponseObj {
    public int qusNo;
    public SrcEntity entity;
    public String qId;
    public AttemptStatus status;
    public List<String> answerGiven;
    public Map<String, List<String>> matrixAnswerGiven;
    public AnswerCorrectness isCorrect;
    public double score;
    public long timeTaken;
    public long attemptStartTime;
    public ContentSearchDetails content;

    public QuestionAttemptStatsInfo() {

        super();
    }

    public QuestionAttemptStatsInfo(int qusNo, SrcEntity entity, String qId, AttemptStatus status,
                                    List<String> answerGiven, Map<String, List<String>> matrixAnswerGiven,
                                    AnswerCorrectness isCorrect, double score, long timeTaken, long attemptStartTime) {

        super();
        this.qusNo = qusNo;
        this.entity = entity;
        this.qId = qId;
        this.status = status;
        this.answerGiven = answerGiven;
        this.matrixAnswerGiven = matrixAnswerGiven;
        this.isCorrect = isCorrect;
        this.score = score;
        this.timeTaken = timeTaken;
        this.attemptStartTime = attemptStartTime;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qusNo:").append(qusNo).append(", entity:").append(entity).append(", qId:")
                .append(qId).append(", status:").append(status).append(", answerGiven:")
                .append(answerGiven).append(", matrixAnswerGiven:").append(matrixAnswerGiven)
                .append(", isCorrect:").append(isCorrect).append(", score:").append(score)
                .append(", timeTaken:").append(timeTaken).append(", attemptStartTime:")
                .append(attemptStartTime).append(", content:").append(content)
                .append(", getClass():").append(getClass()).append(", hashCode():")
                .append(hashCode()).append(", toString():").append(super.toString()).append("}");
        return builder.toString();
    }
}

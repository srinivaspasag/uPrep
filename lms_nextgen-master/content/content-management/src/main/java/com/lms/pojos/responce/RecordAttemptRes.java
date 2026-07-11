package com.lms.pojos.responce;

import com.lms.enums.AnswerCorrectness;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecordAttemptRes {
    public List<String> userAnswer;
    public boolean isJudgeable;
    public List<String> correctAnswer;
    public AnswerCorrectness isUserAnswerCorrect;
    public boolean isOnline; // Used for other cases

    @Override
    public String toString() {
        return "RecordAttemptRes{" +
                "userAnswer=" + userAnswer +
                ", isJudgeable=" + isJudgeable +
                ", correctAnswer=" + correctAnswer +
                ", isUserAnswerCorrect=" + isUserAnswerCorrect +
                '}';
    }
}

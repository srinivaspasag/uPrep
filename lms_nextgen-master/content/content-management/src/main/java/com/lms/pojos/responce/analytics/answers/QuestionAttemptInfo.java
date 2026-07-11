package com.lms.pojos.responce.analytics.answers;

import com.lms.enums.AnswerCorrectness;
import com.lms.enums.AttemptStatus;
import com.lms.pojos.responce.analytics.IQuestionAnswer;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuestionAttemptInfo {
    public QuestionSearchIndexDetails info;
    public IQuestionAnswer answer;
    public AttemptStatus status;
    // remove this field as it is already present answer object
    public AnswerCorrectness isCorrect;

    public QuestionAttemptInfo() {
        super();
    }

    public QuestionAttemptInfo(QuestionSearchIndexDetails info,
                               IQuestionAnswer answer, AnswerCorrectness isCorrect, AttemptStatus status) {
        this.info = info;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.status = status;
    }
}

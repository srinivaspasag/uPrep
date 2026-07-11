package com.lms.pojos.responce.analytics.answers;

import com.lms.enums.AnswerCorrectness;
import com.lms.pojos.responce.analytics.IQuestionAnswer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class QuestionListAnswer implements IQuestionAnswer {
	public List<String> answerGiven;
	public List<String> correctAnswer;
	public AnswerCorrectness isCorrect;
	public long timeTaken;

	public QuestionListAnswer() {
		super();
	}

	public QuestionListAnswer(List<String> answerGiven,
							  List<String> correctAnswer, AnswerCorrectness isCorrect, long timeTaken) {

		this.answerGiven = answerGiven;
		this.correctAnswer = correctAnswer;
		this.isCorrect = isCorrect;
		this.timeTaken = timeTaken;
	}
}

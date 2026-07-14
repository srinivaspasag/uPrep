package com.vedantu.content.pojos.responses.analytics.answers;

import java.util.List;

import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.pojos.responses.analytics.IQuestionAnswer;

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

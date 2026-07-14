package com.vedantu.content.pojos.responses.analytics.answers;

import java.util.List;
import java.util.Map;

import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.pojos.responses.analytics.IQuestionAnswer;

public class QuestionMatrixAnswer implements IQuestionAnswer {
	public Map<String, List<String>> answerGiven;
	public Map<String, List<String>> correctAnswer;
	public AnswerCorrectness isCorrect;
	public long timeTaken;

	public QuestionMatrixAnswer() {
	}

	public QuestionMatrixAnswer(Map<String, List<String>> answerGiven,
			Map<String, List<String>> correctAnswer, AnswerCorrectness isCorrect, long timeTaken) {

		this.answerGiven = answerGiven;
		this.correctAnswer = correctAnswer;
		this.isCorrect = isCorrect;
		this.timeTaken = timeTaken;
	}
}

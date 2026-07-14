package com.vedantu.content.pojos.responses.analytics.answers;

import com.vedantu.content.enums.AnswerCorrectness;
import com.vedantu.content.enums.AttemptStatus;
import com.vedantu.content.pojos.responses.analytics.IQuestionAnswer;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;

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

package com.vedantu.content.pojos.tests;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;

public class TestQTypeQuestions implements ITestQuestion {

	public QuestionType type;
	public List<QuestionSearchIndexDetails> questions;
	public int totalMarks;
	public int totalParagraphs;
	public int maxQuestionsToBeAttempted; //this filed is for NTAPattern test

	public void addQuestion(QuestionSearchIndexDetails queston) {
		if (questions == null) {
			questions = new ArrayList<QuestionSearchIndexDetails>();
		}
		questions.add(queston);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{type:").append(type).append(", questions:")
				.append(questions).append("}");
		return builder.toString();
	}

}

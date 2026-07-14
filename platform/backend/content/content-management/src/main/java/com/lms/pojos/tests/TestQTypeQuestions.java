package com.lms.pojos.tests;

import com.lms.enums.QuestionType;
import com.lms.pojos.search.details.QuestionSearchIndexDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TestQTypeQuestions implements ITestQuestion {

	public QuestionType type;
	public List<QuestionSearchIndexDetails> questions;
	public int totalMarks;
	public int totalParagraphs;

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

package com.vedantu.content.pojos.responses.tests;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.content.pojos.tests.ITestQuestion;
import com.vedantu.content.search.details.boards.BoardSearchEntity;

public class TestBoardWiseQuestions extends BoardSearchEntity {

	public List<ITestQuestion> questions;

	public TestBoardWiseQuestions() {
		super();
	}

	public TestBoardWiseQuestions(String name, String id) {
		super(name, id, null);
	}

	public void addITestQuestion(ITestQuestion iTestQueston) {
		if (questions == null) {
			questions = new ArrayList<ITestQuestion>();
		}
		questions.add(iTestQueston);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{questions:").append(questions).append("}");
		return builder.toString();
	}

}

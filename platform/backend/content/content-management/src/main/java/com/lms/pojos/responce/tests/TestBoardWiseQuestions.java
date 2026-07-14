package com.lms.pojos.responce.tests;

import com.lms.pojos.search.details.BoardSearchEntity;
import com.lms.pojos.tests.ITestQuestion;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
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

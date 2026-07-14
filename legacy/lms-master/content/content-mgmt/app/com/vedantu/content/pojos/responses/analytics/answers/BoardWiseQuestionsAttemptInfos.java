package com.vedantu.content.pojos.responses.analytics.answers;

import java.util.ArrayList;
import java.util.List;

public class BoardWiseQuestionsAttemptInfos {
	public String name;
	public String id;
	public List<QuestionAttemptInfo> questions;

	public BoardWiseQuestionsAttemptInfos() {

	}

	public BoardWiseQuestionsAttemptInfos(String name, String id) {
		super();
		this.name = name;
		this.id = id;
	}

	public void addQuestionAttemptInfo(QuestionAttemptInfo info) {
		if (questions == null) {
			questions = new ArrayList<QuestionAttemptInfo>();
		}
		if (info == null) {
			return;
		}
		questions.add(info);
	}
}

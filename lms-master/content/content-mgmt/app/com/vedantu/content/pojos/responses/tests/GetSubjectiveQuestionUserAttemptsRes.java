package com.vedantu.content.pojos.responses.tests;

import java.util.List;

import com.vedantu.content.models.analytics.UserQuestionAttempt;

public class GetSubjectiveQuestionUserAttemptsRes {
	public List<UserQuestionAttempt> userQuestionAttempts;
	public String subjectiveQuestionId;
	public List<TestBoardWiseQuestions> boards;
	public long totalhits;
}

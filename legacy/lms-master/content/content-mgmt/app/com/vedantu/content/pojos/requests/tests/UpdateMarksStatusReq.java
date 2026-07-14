package com.vedantu.content.pojos.requests.tests;

import play.data.validation.Constraints.Required;

import com.vedantu.content.pojos.tests.QuestionResultStatus;

public class UpdateMarksStatusReq {
	@Required
	public String testId;
	@Required
	public QuestionResultStatus status;
	@Required
	public String questionId;
}

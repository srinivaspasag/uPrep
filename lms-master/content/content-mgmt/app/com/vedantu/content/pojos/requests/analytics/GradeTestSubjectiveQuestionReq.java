package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.enums.AnswerCorrectness;

public class GradeTestSubjectiveQuestionReq extends AbstractAuthCheckReq {
	@Required
	public String qId;
	@Required
	public String attemptId;
	public String testId;
	public String orgId;
	public String studentUserId;
	public AnswerCorrectness isCorrect;
	public double score;
}

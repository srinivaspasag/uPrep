package com.vedantu.content.pojos.requests.tests;

import com.vedantu.content.pojos.requests.AbstractGetContentReq;

import play.data.validation.Constraints.Required;

public class GetSubjectiveQuestionUserAttemptsReq extends AbstractGetContentReq{
	@Required
	public String testId;
	public int start;
	public int size;
	public boolean loadQuestionInfo;
}

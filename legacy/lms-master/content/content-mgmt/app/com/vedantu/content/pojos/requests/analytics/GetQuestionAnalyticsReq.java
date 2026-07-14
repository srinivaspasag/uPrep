package com.vedantu.content.pojos.requests.analytics;

import com.vedantu.content.enums.AnswerCorrectness;
import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetQuestionAnalyticsReq extends AbstractAuthCheckReq {

	@Required
	public String entityId;
	@Required
	public EntityType entityType;
	@Required
	public String qId;
	public String orgId;

	public AnswerCorrectness isCorrect;
	public SrcEntity parentEntity;

}

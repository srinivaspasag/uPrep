package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.AnswerCorrectness;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetQuestionAnalyticsReq extends AbstractAuthCheckReq {

	@NotBlank(message = "entityId should not be empty")
	public String entityId;

	public EntityType entityType;
	@NotBlank(message = "qId should not be empty")
	public String qId;
	public String orgId;

	public AnswerCorrectness isCorrect;
	public SrcEntity parentEntity;

}

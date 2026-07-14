package com.vedantu.content.pojos.requests.challenges;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetChallengeHintReq extends AbstractAuthCheckReq {

	@Required
	public String token;
}

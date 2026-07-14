package com.vedantu.content.pojos.requests.challenges;

import java.util.List;

import play.data.validation.Constraints.Required;

public class AttemptChallengeReq extends GetChallengeHintReq {

	@Required
	public List<String> answer;
}

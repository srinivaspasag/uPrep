package com.vedantu.cmds.pojos.requests.questions;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.content.apis.IRequestParamsValidator;

public class PublishQuestionAsChallengeReq extends PublishQuestionReq implements
		IRequestParamsValidator {

	@Required
	public String channelId;
	
	@Required
	public String name;
	public Scope scope;
	public int lifeTime;
	public int duration;
	public int maxBid;
	@Required
	public String publishType;
	public String difficulty;
	public int initialBidPool;
	public List<Integer> hintsDeduction;

	@Override
	public boolean validateRequestParams() throws VedantuException {

		if ((lifeTime < duration) || maxBid == 0) {
			throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
		}
		return false;
	}
}

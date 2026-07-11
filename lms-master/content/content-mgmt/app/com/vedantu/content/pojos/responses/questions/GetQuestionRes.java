package com.vedantu.content.pojos.responses.questions;

import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.IAttemptableEntity;
import com.vedantu.content.pojos.responses.analytics.IQuestionAnswer;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;

public class GetQuestionRes extends QuestionSearchIndexDetails implements
		IListResponseObj, IAttemptableEntity {

	public boolean attempted;
	public boolean voted;
	public boolean hasSeenAnswer;
	public FollowType followType;
	public IQuestionAnswer answer;

	@Override
	public void _setVoted(boolean voted) {
		this.voted = voted;
	}

	@Override
	public void _setFollowType(FollowType followType) {
		this.followType = followType;
	}

	@Override
	public void _setAttempted(boolean attempted) {
		this.attempted = attempted;
		this.hasSeenAnswer = this.attempted;
	}

	@Override
	public String _getEntityId() {
		return id;
	}

}

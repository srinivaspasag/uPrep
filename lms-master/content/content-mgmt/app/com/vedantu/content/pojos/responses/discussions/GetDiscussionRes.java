package com.vedantu.content.pojos.responses.discussions;

import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.ISocialEntity;
import com.vedantu.content.search.details.DiscussionSearchIndexDetails;

public class GetDiscussionRes extends DiscussionSearchIndexDetails implements
		IListResponseObj, ISocialEntity {

	public boolean voted;
	public FollowType followType;

	@Override
	public void _setVoted(boolean voted) {
		this.voted = voted;
	}

	@Override
	public void _setFollowType(FollowType followType) {
		this.followType = followType;
	}

	@Override
	public String _getEntityId() {
		return id;
	}

}

package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.pojos.search.details.VideoSearchIndexDetails;
import com.lms.utils.ISocialEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetVideoRes extends VideoSearchIndexDetails implements
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

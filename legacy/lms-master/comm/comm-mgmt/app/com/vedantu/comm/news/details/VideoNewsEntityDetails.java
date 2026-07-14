package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class VideoNewsEntityDetails extends AbstractSocialEntity implements
		ISocialEntity {
	public long duration;
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	public String name;

	public VideoNewsEntityDetails() {

		type = EntityType.VIDEO;
	}

	public VideoNewsEntityDetails(String id) {

		type = EntityType.VIDEO;
		this.id = id;

	}

	@Override
	public String _getEntityId() {

		return id;
	}

	@Override
	public void _setVoted(boolean voted) {
	}

	@Override
	public void _setFollowType(FollowType followType) {

	}

}

package com.vedantu.comm.news.details;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;

public class ModuleNewsEntityDetails extends AbstractSocialEntity implements
		ISocialEntity {
	public long totalContentCount;
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	public String name;

	public ModuleNewsEntityDetails() {

		type = EntityType.MODULE;
	}

	public ModuleNewsEntityDetails(String id) {

		type = EntityType.MODULE;
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

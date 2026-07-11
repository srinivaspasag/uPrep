package com.vedantu.commons.utils;

import com.vedantu.commons.enums.FollowType;

public interface ISocialEntity {

	public String _getEntityId();

	public void _setVoted(boolean voted);

	public void _setFollowType(FollowType followType);
}

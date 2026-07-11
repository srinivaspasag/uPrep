package com.vedantu.content.pojos.responses;

import java.util.Set;

import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.utils.ISocialEntity;

public abstract class AbstractContentUserActionRes extends
		AbstractContentStatsRes implements ISocialEntity {

	public boolean voted;
	public FollowType followType;

	public AbstractContentUserActionRes(String id, int upVotes, int views,
			int followers, int comments, long timeCreated, long lastUpdated,
			boolean voted, Scope scope, Set<String> tags) {
		super(id, upVotes, views, followers, comments, timeCreated,
				lastUpdated, scope, tags);
		this.voted = voted;
		this.followType = FollowType.NONE;
	}

	@Override
	public void _setVoted(boolean voted) {
		this.voted = voted;
	}

	@Override
	public void _setFollowType(FollowType followType) {
		this.followType = followType;
	}
}

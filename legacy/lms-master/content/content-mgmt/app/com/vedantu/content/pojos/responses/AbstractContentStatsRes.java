package com.vedantu.content.pojos.responses;

import java.util.Set;

import com.vedantu.commons.enums.Scope;

public abstract class AbstractContentStatsRes extends AbstractContentRes {

	public int upVotes;
	public int views;
	public int followers;
	public int comments;
	public Scope scope;
	public Set<String> tags;

	public AbstractContentStatsRes(String id, int upVotes, int views,
			int followers, int comments, long timeCreated, long lastUpdated,
			Scope scope, Set<String> tags) {
		super(id, timeCreated, lastUpdated);
		this.upVotes = upVotes;
		this.views = views;
		this.followers = followers;
		this.comments = comments;
		this.lastUpdated = lastUpdated;
		this.scope = scope;
		this.tags = tags;

	}
}

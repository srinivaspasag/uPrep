package com.vedantu.content.pojos.responses.comments;

import java.util.Set;

import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.CommentType;

public class AddCommentRes extends GetCommentRes {

	public AddCommentRes(String id, int upVotes, int views, int followers,
			int comments, long timeCreated, long lastUpdated, boolean voted,
			String content, SrcEntity parent, CommentType type,
			boolean hasMedia, SrcEntity base, SrcEntity root, Scope scope,
			Set<String> tags) {
		super(id, upVotes, views, followers, comments, timeCreated, lastUpdated,
				voted, content, parent, type, hasMedia, base, root, scope, tags);
	}

}

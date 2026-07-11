package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.CommentType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
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

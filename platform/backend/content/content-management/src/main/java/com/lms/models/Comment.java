package com.lms.models;

import com.lms.common.utils.URLUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.CommentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(value = "comments")
@Setter
@Getter
public class Comment extends AbstractContentStatsModel {

    public String content;
    @Indexed
    public SrcEntity parent;
    public CommentType type;
    public boolean hasMedia;

    public SrcEntity base; // comment on a page highlight --> root==PAGE,
    // base=DOCUMENT, parent=HIGHLIGHT
    public SrcEntity root;// comment to which type of element i.e.
    // page,highlight, comment, document etc..

    public Comment() {
        super();
    }

    public Comment(String userId, String content, SrcEntity parent,
                   CommentType type, SrcEntity base, SrcEntity root, Scope scope,
                   Set<String> tags) {
        super();
        this.userId = userId;
        this.content = content;
        this.parent = parent;
        this.type = type;
        this.hasMedia = URLUtils.containURL(content);
        this.base = base;
        this.root = root;
        this.scope = scope;
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Comment [content=" + content + ", parent=" + parent + ", type="
                + type + ", hasMedia=" + hasMedia + ", base=" + base
                + ", root=" + root + ", upVotes=" + upVotes + ", views="
                + views + ", followers=" + followers + ", comments=" + comments
                + ", userId=" + userId + ", boardIds=" + boardIds
                + ", targetIds=" + targetIds + ", difficulty=" + difficulty
                + ", contentSrc=" + contentSrc + ", scope=" + scope + ", tags="
                + tags + ", id=" + id + ", timeCreated=" + timeCreated
                + ", lastUpdated=" + lastUpdated + ", recordState="
                + recordState + "]";
    }

}

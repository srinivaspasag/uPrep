package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.enums.DoubtState;

@Entity(value = "discussions", noClassnameStored = true)
public class Discussion extends AbstractContentStatsModel implements IIndexable {

    public String content; // description of the discussion..
    public DoubtState state;
    public int rating;
    public Discussion() {

        this(null, null, null);
    }

    public Discussion(String name, String content, String userId) {

        super();
        this.name = name;
        this.content = content;
        this.userId = userId;
        this.contentType = EntityType.DISCUSSION;
        this.state = DoubtState.UNASSIGNED;
    }

    @Override
    public String toString() {
        return "Discussion [content=" + content + ", state=" + state + ", rating=" + rating
                + ", upVotes=" + upVotes + ", views=" + views + ", followers=" + followers
                + ", comments=" + comments + ", shares=" + shares + ", boardIds=" + boardIds
                + ", targetIds=" + targetIds + ", difficulty=" + difficulty + ", contentSrc="
                + contentSrc + ", tags=" + tags + ", completed=" + completed + ", userId=" + userId
                + ", name=" + name + "]";
    }

}

package com.lms.models;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.DoubtState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "discussions")
@Setter
@Getter
public class Discussion extends AbstractContentStatsModel  {

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

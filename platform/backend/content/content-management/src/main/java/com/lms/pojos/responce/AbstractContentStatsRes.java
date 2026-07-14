package com.lms.pojos.responce;

import com.lms.common.vedantu.enums.Scope;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Setter
@Getter
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

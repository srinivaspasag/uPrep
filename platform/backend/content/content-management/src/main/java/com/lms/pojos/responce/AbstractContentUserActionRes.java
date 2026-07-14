package com.lms.pojos.responce;

import com.lms.common.vedantu.enums.FollowType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.utils.ISocialEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Setter
@Getter
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

package com.vedantu.content.pojos.responses.tests;

import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.IAttemptableEntity;
import com.vedantu.content.search.details.AssignmentSearchIndexDetails;

public class GetAssignmentInfoRes extends AssignmentSearchIndexDetails implements IListResponseObj,
        IAttemptableEntity {

    public boolean    attempted;
    public boolean    voted;
    public FollowType followType;

    @Override
    public void _setVoted(boolean voted) {
        this.voted = voted;
    }

    @Override
    public void _setFollowType(FollowType followType) {
        this.followType = followType;
    }

    @Override
    public String _getEntityId() {
        return id;
    }

    @Override
    public void _setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{attempted:").append(attempted).append(", voted:").append(voted)
                .append(", followType:").append(followType).append(", cmdsId:").append(cmdsId)
                .append(", code:").append(code).append(", desc:").append(desc).append(", target:")
                .append(target).append(", qusCount:").append(qusCount).append(", duration:")
                .append(duration).append(", totalMarks:").append(totalMarks).append(", avgMarks:")
                .append(avgMarks).append(", attempts:").append(attempts).append(", published:")
                .append(published).append(", metadata:").append(metadata).append(", type:")
                .append(type).append(", boardTree:").append(boardTree).append(", boards:")
                .append(boards).append(", targets:").append(targets).append(", contentSrc:")
                .append(contentSrc).append(", tags:").append(tags).append(", scope:").append(scope)
                .append(", avgRating:").append(avgRating).append(", views:").append(views)
                .append(", followers:").append(followers).append(", comments:").append(comments)
                .append(", upVotes:").append(upVotes).append(", difficulty:").append(difficulty)
                .append(", name:").append(name).append(", userId:").append(userId).append(", id:")
                .append(id).append(", userAction:").append(userAction).append(", timeCreated:")
                .append(timeCreated).append(", lastUpdated:").append(lastUpdated)
                .append(", lastIndexTime:").append(lastIndexTime)
                .append(", isNotificationEnabled:").append(isNotificationEnabled).append(", user:")
                .append(user).append("}");
        return builder.toString();
    }

}

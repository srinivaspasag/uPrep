package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.pojos.search.details.TestSearchIndexDetails;
import com.lms.utils.IAttemptableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTestInfoRes extends TestSearchIndexDetails implements
        IListResponseObj, IAttemptableEntity {
    public boolean attempted;
    public boolean voted;
    public FollowType followType;
    public String pdfId;
    public String password;
    public String resultPassword;
    public String testStatus;
    public boolean processed;

    public long avgTimeTaken;
    public boolean enablePartialMarks;

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
        builder.append("{attempted:").append(attempted).append(", voted:")
                .append(voted).append(", followType:").append(followType)
                .append(", avgTimeTaken:").append(avgTimeTaken)
                .append(", qrTestId:").append(qrTestId).append(", code:")
                .append(code).append(", desc:").append(desc)
                .append(", target:").append(target).append(", qusCount:")
                .append(qusCount).append(", duration:").append(duration)
                .append(", totalMarks:").append(totalMarks)
                .append(", avgMarks:").append(avgMarks).append(", attempts:")
                .append(attempts).append(", published:").append(published)
                .append(", metadata:").append(metadata).append(", type:")
                .append(type).append(", boardTree:").append(boardTree)
                .append(", boards:").append(boards).append(", targets:")
                .append(targets).append(", contentSrc:").append(contentSrc)
                .append(", tags:").append(tags).append(", scope:")
                .append(scope).append(", avgRating:").append(avgRating)
                .append(", views:").append(views).append(", followers:")
                .append(followers).append(", comments:").append(comments)
                .append(", upVotes:").append(upVotes).append(", difficulty:")
                .append(difficulty).append(", name:").append(name)
                .append(", userId:").append(userId).append(", id:").append(id)
                .append(", userAction:").append(userAction)
                .append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated)
                .append(", lastIndexTime:").append(lastIndexTime)
                .append(", isNotificationEnabled:")
                .append(isNotificationEnabled).append(", user:").append(user)
                .append("}");
        return builder.toString();
    }

}

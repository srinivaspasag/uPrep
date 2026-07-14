package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetChallengeLeaderBoardRes implements IListResponseObj {

    public long rank;
    public long timeTaken;
    public int hint;
    public String userId;
    public UserInfo user;

    public GetChallengeLeaderBoardRes(long rank, long timeTaken, int hint,
                                      String userId) {
        super();
        this.rank = rank;
        this.timeTaken = timeTaken;
        this.hint = hint;
        this.userId = userId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{rank:").append(rank).append(", timeTaken:")
                .append(timeTaken).append(", hint:").append(hint)
                .append(", userId:").append(userId).append(", user:")
                .append(user).append("}");
        return builder.toString();
    }

}

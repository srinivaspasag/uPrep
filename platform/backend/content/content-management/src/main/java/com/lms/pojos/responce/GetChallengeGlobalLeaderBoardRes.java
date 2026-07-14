package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.enums.RankType;
import com.lms.user.vedantu.user.pojo.UserInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class GetChallengeGlobalLeaderBoardRes implements IListResponseObj {
    public String userId;
    public UserInfo user;
    public int points;
    public int totalAttempts;
    public int correctAttempts;
    public float strikeRate;
    public Map<Integer, Integer> hintsCountMap;
    public RankType type;

    public long rank;

    public GetChallengeGlobalLeaderBoardRes(String userId, int points,
                                            int totalAttempts, int correctAttempts, float strikeRate,
                                            Map<Integer, Integer> hintsCountMap, RankType type) {
        super();
        this.userId = userId;
        this.points = points;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.strikeRate = strikeRate;
        this.hintsCountMap = hintsCountMap;
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", user:")
                .append(user).append(", points:").append(points)
                .append(", totalAttempts:").append(totalAttempts)
                .append(", correctAttempts:").append(correctAttempts)
                .append(", strikeRate:").append(strikeRate)
                .append(", hintsCountMap:").append(hintsCountMap)
                .append(", type:").append(type).append(", toString():")
                .append(super.toString()).append("}");
        return builder.toString();
    }

}

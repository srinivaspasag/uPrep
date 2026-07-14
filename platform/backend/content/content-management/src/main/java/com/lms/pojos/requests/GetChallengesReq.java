package com.lms.pojos.requests;

import com.lms.enums.ChallengeStatus;
import com.lms.enums.ChallengeType;
import com.lms.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetChallengesReq extends AbstractContentSearchReq {

    public String channelId;
    public ChallengeStatus status;
    public ChallengeType type;
    public String qType;
    public Difficulty difficulty;

}

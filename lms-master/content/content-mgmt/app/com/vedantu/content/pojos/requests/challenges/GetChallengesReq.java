package com.vedantu.content.pojos.requests.challenges;

import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.challenges.ChallengeStatus;
import com.vedantu.content.enums.challenges.ChallengeType;
import com.vedantu.content.pojos.requests.AbstractContentSearchReq;

public class GetChallengesReq extends AbstractContentSearchReq {

    public String          channelId;
    public ChallengeStatus status;
    public ChallengeType   type;
    public String          qType;
    public Difficulty      difficulty;

}

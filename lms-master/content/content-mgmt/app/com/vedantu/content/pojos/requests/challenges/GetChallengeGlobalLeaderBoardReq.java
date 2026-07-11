package com.vedantu.content.pojos.requests.challenges;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.vedantu.content.enums.challenges.RankType;

public class GetChallengeGlobalLeaderBoardReq extends AbstractOrgListReq {

    @Required
    public RankType rankType;
}

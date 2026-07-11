package com.vedantu.content.pojos.requests.challenges;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetChallengeLeaderBoardReq extends AbstractOrgListReq {

    @Required
    public String id; // challengeId

}

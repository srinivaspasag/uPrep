package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.enums.RankType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetChallengeGlobalLeaderBoardReq extends AbstractOrgListReq {

    @NotBlank(message = "ranktype should not be null")
    public RankType rankType;
}

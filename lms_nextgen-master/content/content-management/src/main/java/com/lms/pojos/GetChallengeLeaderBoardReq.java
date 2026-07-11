package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetChallengeLeaderBoardReq extends AbstractOrgListReq {

    @NotBlank(message = "id should not be null //challengeId")
    public String id; // challengeId

}

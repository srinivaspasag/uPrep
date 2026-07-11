package com.vedantu.board.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class DeleteBoardReq extends AbstractAuthCheckReq {

    @Required
    public String brdId;
    public String parentBrdId;
}

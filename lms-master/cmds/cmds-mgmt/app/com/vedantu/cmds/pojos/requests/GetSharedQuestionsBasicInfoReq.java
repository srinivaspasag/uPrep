package com.vedantu.cmds.pojos.requests;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import play.data.validation.Constraints.Required;

public class GetSharedQuestionsBasicInfoReq extends AbstractAuthCheckReq {
    @Required
    public String orgId;

}

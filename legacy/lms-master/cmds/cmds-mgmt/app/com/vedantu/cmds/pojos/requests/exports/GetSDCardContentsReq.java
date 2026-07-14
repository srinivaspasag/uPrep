package com.vedantu.cmds.pojos.requests.exports;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.pojos.requests.AbstractGetResourcesReq;

public class GetSDCardContentsReq extends AbstractGetResourcesReq {

    @Required
    public String id;
}

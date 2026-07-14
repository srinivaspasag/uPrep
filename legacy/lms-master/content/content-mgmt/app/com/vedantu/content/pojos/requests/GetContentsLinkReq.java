package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetContentsLinkReq extends AbstractOrgListReq {

    @Required
    public String         targetUserId;

    @Required
    public SrcEntity      target;

    @Required
    public UserActionType linkType;

    public long           addedAfter;

    public boolean        addContent;
}

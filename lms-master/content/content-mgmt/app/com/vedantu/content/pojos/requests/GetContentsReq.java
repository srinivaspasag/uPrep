package com.vedantu.content.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetContentsReq extends AbstractOrgListReq {

    @Required
    public String       targetUserId;
    @Required
    public List<String> ids;

    @Required
    public EntityType   type;
    public boolean      addAnswer;
}

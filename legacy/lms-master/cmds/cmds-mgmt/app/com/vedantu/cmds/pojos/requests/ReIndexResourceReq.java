package com.vedantu.cmds.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class ReIndexResourceReq extends AbstractAuthCheckReq {

    @Required
    public EntityType          type;
    public List<String>        includes;
    public long                minTimeCreated;
    public long                maxTimeCreated;
    public CmdsContentLinkType linkType;
    public EntityType          containerType;
}

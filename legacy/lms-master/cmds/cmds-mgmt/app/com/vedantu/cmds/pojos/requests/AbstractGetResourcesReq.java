package com.vedantu.cmds.pojos.requests;

import java.util.List;
import java.util.Map;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.mongo.MongoManager.SortOrder;

public abstract class AbstractGetResourcesReq extends AbstractAuthCheckReq {

    public List<String>     brdIds;
    public String           query;
    public List<EntityType> includes;
    public List<EntityType> excludes;
    public String           includeTypes;
    public String           includeDifficulty;
    public String           orderBy;
    public String           sortOrder = SortOrder.DESC.name().toLowerCase();

    @Required
    public int              start;
    public int              size;
    @Required
    public String           orgId;

    public AbstractGetResourcesReq() {

        super();
    }

    public AbstractGetResourcesReq(String callingUserId, String userId) {

        super(callingUserId, userId);
    }

    public AbstractGetResourcesReq(Map<String, String[]> form) {

        super(form);
    }

}
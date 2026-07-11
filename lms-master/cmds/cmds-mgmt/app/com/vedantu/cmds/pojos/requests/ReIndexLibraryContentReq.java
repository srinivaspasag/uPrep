package com.vedantu.cmds.pojos.requests;

import java.util.List;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class ReIndexLibraryContentReq extends AbstractAuthCheckReq {

    public EntityType     entityType;
    public UserActionType linkType;
    public List<String>   ids;
    public long           fromTime;
    public long           toTime;
    public List<String>   linkIds;
}

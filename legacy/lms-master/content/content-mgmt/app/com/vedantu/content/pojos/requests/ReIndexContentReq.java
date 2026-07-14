package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.UserActionType;

public class ReIndexContentReq {

    @Required
    public EntityType     entityType;

    public UserActionType linkType;
    public String[]       ids;
}

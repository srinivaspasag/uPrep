package com.vedantu.commons.pojos.requests;

import play.data.validation.Constraints.Required;

public abstract class AbstractOrgListReq extends AbstractListReq {

    @Required
    public String           orgId;
  
}

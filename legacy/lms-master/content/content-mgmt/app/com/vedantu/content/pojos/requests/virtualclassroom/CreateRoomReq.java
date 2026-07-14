package com.vedantu.content.pojos.requests.virtualclassroom;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class CreateRoomReq extends AbstractOrgScopeReq {
    @Required
    public String description;
    public boolean recordClass;
    public long startTime;
    public long endTime;
    public boolean cancelled;
    public boolean audioOnly;
}

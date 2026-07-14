package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

public class UpdateEndTimeMappingReq {
	
    @Required
    public String           orgId;
    @Required
    public String           targetUserId;
    @Required
    public String           sectionId;
    @Required
    public long	            endTime;

}

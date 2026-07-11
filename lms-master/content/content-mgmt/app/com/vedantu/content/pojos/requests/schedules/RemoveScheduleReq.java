package com.vedantu.content.pojos.requests.schedules;

import play.data.validation.Constraints.Required;

public class RemoveScheduleReq extends GetScheduleReq{
    @Required
    public String boardId;
    @Required
    public String entityType;
    @Required
    public String entityId;
    @Required
    public String entityCmdsId;
}

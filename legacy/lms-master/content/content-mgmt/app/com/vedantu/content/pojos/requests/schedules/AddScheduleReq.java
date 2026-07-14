package com.vedantu.content.pojos.requests.schedules;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.pojos.SrcEntity;

import play.data.validation.Constraints.Required;

public class AddScheduleReq extends GetScheduleReq {
    @Required
    public long day;
    @Required
    public List<SrcEntity> entityList = new ArrayList<SrcEntity>();
    @Required
    public String boardId;
    @Required
    public String boardName;
}

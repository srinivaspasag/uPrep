package com.lms.pojos.responce;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojo.OrgProgramBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class EntityScheduleInfo implements IListResponseObj {

    public ScheduleInfo schedule;
    public List<OrgProgramBasicInfo> programs;
}

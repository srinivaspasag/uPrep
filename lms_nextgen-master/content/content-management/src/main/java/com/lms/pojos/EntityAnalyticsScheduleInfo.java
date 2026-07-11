package com.lms.pojos;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojo.OrgProgramBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class EntityAnalyticsScheduleInfo implements IListResponseObj {

    public ScheduleInfo schedule;
    public List<OrgProgramBasicInfo> programs;
    public EntityTopper topper;
    public EntityAnalyticsBasicInfo entity;

    public EntityAnalyticsScheduleInfo() {
    }

    public EntityAnalyticsScheduleInfo(ScheduleInfo schedule,
                                       List<OrgProgramBasicInfo> programs, EntityTopper topper,
                                       EntityAnalyticsBasicInfo entity) {
        super();
        this.schedule = schedule;
        this.programs = programs;
        this.topper = topper;
        this.entity = entity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{schedule:");
        builder.append(schedule);
        builder.append(", programs:");
        builder.append(programs);
        builder.append(", topper:");
        builder.append(topper);
        builder.append(", entity:");
        builder.append(entity);
        builder.append("}");
        return builder.toString();
    }

}

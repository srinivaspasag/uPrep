package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetEntityScheduleAnalyticsReq extends AbstractOrgListReq {

    @NotBlank(message = "entityType should not be null")
    public EntityType entityType;

    @NotBlank(message = "programId should not be null")
    public String programId;

    public String centerId;

    public String sectionId;

    public String brdId;

    public String courseName = "";
    public String courseId = "";

    public String topicName = "";
    public String topicId = "";

    public String query;
}

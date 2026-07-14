package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class GetScheduleReq extends AbstractOrgScopeReq {
    public String programId;
    public String centerId;
    @NotBlank(message = "sectionId should not be null")
    public String sectionId;
    public long month;
    public long day;
}

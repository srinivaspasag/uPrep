package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class FinishCMDSAssignmentEditReq extends AbstractOrgScopeReq {

    @NotBlank
    public String assignmentId;
}
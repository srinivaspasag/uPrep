package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractOrgListReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetCMDSAssignmentQuestionsReq  extends AbstractOrgListReq {
    @NotBlank
    public String assignmentId;
    public String brdId;
}

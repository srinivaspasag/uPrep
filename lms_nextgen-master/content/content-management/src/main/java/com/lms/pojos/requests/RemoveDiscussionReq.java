package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class RemoveDiscussionReq extends AbstractOrgScopeReq {

    @NotBlank(message = "id should not be null")
    public String id;
}

package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.OrgMemberProfile;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AbstractOrgScopeReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String           orgId;

    public OrgMemberProfile orgMemberProfile;
}

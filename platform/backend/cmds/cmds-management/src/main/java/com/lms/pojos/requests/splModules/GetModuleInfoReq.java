package com.lms.pojos.requests.splModules;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetModuleInfoReq extends AbstractOrgScopeReq {
    @NotBlank(message = "id should not be null")
    public String id;
    public String sectionId;
    public SrcEntity target;

}
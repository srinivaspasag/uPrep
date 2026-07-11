package com.lms.pojos.requests.splModules;

import com.lms.enums.ModuleRun;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class AbstractModuleReq extends AbstractOrgScopeReq {

    public ModuleRun moduleRun;
    public List<String> tags;
    public List<String> brdIds;
    public List<String> targetIds;
    public String prerequsiteModuleID;
}

package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetCMDSModulesRes {
    public List<CMDSModuleNameInfo> modules;
}

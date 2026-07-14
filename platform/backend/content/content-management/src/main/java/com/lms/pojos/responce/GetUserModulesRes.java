package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetUserModulesRes
{
    public List<ModuleAccessStatus> modulesAccessStatus;

}

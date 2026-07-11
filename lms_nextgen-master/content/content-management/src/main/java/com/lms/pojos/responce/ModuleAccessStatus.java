package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleAccessStatus
{
    public String moduleId;
    public int totalContents;
    public int accessedContents;
}

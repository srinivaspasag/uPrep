package com.lms.pojo.responce;

import com.lms.pojo.OrgProgramBasicInfo;
import com.lms.pojo.OrgStructureBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetOrgSectionInfoByAccessCodeRes {
    public GetOrgRes             org;
    public OrgProgramBasicInfo program;
    public OrgStructureBasicInfo center;
    public OrgStructureBasicInfo section;
}

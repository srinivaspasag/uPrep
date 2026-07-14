package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class GetOrgSectionInfoByAccessCodeRes {

    public GetOrgRes             org;
    public OrgProgramBasicInfo   program;
    public OrgStructureBasicInfo center;
    public OrgStructureBasicInfo section;

}

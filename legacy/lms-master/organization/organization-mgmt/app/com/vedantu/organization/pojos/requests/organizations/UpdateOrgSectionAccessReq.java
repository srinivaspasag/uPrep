package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

public class UpdateOrgSectionAccessReq {
    @Required
   public List<OrgSectionAccessInfo> sectionAccessInfos;  
}

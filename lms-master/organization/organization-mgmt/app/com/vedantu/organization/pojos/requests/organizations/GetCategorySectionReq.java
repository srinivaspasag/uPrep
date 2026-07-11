package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetCategorySectionReq extends AbstractOrgScopeReq {

   
    @Required
    public String sectionId;
    

    public GetCategorySectionReq() {

        userId = "PUBLIC"; // this api can be used with both userId specify or not specified
        callingUserId = "PUBLIC"; // this api can be used with both userId specify or not specified

    }

}

package com.vedantu.organization.pojos.requests.organizations;

import java.util.Set;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class AddCategoryReq extends AbstractOrgScopeReq{

    @Required
    public String name;
    public Set<String> sectionIds; 
        
}

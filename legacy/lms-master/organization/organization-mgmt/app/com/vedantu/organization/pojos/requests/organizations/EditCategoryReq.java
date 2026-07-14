package com.vedantu.organization.pojos.requests.organizations;

import java.util.Set;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class EditCategoryReq extends AbstractOrgScopeReq{

    @Required
    public String id;
    public String name;
    public Set<String> sectionIds; 
}

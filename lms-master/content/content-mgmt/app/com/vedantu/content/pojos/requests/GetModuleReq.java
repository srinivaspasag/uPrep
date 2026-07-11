package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class GetModuleReq extends AbstractOrgScopeReq{
    @Required      
    public String id;
          
}

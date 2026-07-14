package com.vedantu.organization.pojos.requests.microsite;

import java.util.Map;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class AddMicrositeConfigReq extends AbstractOrgScopeReq {

    public String              privacy;
    public Map<String, String> config;
    public String              templateId;

    
}

package com.vedantu.content.pojos.requests;

import java.util.Map;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public abstract class AbstractGetContentReq extends AbstractOrgScopeReq {

    @Required
    public String               id;

    // will be set from API call wherever required
    private Map<String, String> sessionParams;

    public Map<String, String> __getSessionParams() {

        return sessionParams;
    }

    public void __setSessionParams(Map<String, String> sessionParams) {

        this.sessionParams = sessionParams;
    }
}

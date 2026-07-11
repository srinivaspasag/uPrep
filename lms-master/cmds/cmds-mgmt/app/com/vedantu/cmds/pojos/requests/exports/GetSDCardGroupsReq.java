package com.vedantu.cmds.pojos.requests.exports;

import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetSDCardGroupsReq extends AbstractOrgScopeReq {

    public String      sectionId;

    public int         start;
    public int         size;
    public AccessScope state;
    public long        addedAfter;

}

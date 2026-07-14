package com.vedantu.cmds.pojos.requests.exports;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetExportDetailsReq extends AbstractOrgScopeReq {

    public String  exportId;
    public boolean fetchContent;
    public int     start;
    public int     size;
}

package com.vedantu.cmds.pojos.requests.exports;

import com.vedantu.cmds.enums.ExportState;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetExportsReq extends AbstractOrgScopeReq {

    public String      programId;
    public String      centerId;
    public String      sectionId;

    public ExportState state;
    public int         start;
    public int         size;

}

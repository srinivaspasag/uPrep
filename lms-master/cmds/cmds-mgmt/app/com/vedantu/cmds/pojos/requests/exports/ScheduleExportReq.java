package com.vedantu.cmds.pojos.requests.exports;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ScheduleExportReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity       orgEntity;
    public List<SrcEntity> sources;

    @Required
    public String          name;
    public String          targetUserId;
}

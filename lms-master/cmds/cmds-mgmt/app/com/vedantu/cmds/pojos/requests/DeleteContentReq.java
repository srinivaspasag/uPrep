package com.vedantu.cmds.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class DeleteContentReq extends AbstractOrgScopeReq {

    @Required
    public List<SrcEntity> entities;

}

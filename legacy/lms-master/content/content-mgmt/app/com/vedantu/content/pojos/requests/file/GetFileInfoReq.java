package com.vedantu.content.pojos.requests.file;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetFileInfoReq extends AbstractOrgScopeReq {

    @Required
    public List<SrcEntity> contents;
}

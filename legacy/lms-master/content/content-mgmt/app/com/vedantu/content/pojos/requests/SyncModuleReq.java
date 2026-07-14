package com.vedantu.content.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class SyncModuleReq extends AbstractAppCheckReq{
    @Required
    public String  userId;
    @Required
    public String moduleId;
    public List<SrcEntity> entities;
}

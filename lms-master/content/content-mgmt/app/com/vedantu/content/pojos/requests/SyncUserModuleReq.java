package com.vedantu.content.pojos.requests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.content.models.ModuleEntry;

public class SyncUserModuleReq extends AbstractAppCheckReq{
    @Required
    public String  userId;
    @Required
    public String moduleId;
    public List<ModuleEntry> moduleEntries;
}

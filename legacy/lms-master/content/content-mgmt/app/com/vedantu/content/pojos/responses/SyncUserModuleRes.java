package com.vedantu.content.pojos.responses;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.content.models.ModuleEntry;


public class SyncUserModuleRes {
    @Required
    public String  userId;
    @Required
    public String moduleId;
    public List<ModuleEntry> moduleEntries;
}

package com.vedantu.content.pojos.requests;

import java.util.List;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.models.ModuleRun;
import com.vedantu.content.pojos.ModuleEntryInfo;
import com.vedantu.content.pojos.tests.ResourceInfo;
import com.vedantu.mongo.VedantuRecordState;



public class ModuleInfo extends ResourceInfo {

    public List<ModuleEntryInfo> children;
    public ModuleRun             moduleRun;
    public String              prerequsiteModuleId;

    public ModuleInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, ModuleRun moduleRun,
            VedantuRecordState recordState, List<ModuleEntryInfo> children, String prerequsiteModuleId2) {

        super(id, name, EntityType.MODULE, timeCreated, lastUpdated, addedBy,
                programsAddedTo, recordState);
        this.children = children;
        this.moduleRun = moduleRun;
        this.prerequsiteModuleId = prerequsiteModuleId2;
    }

}


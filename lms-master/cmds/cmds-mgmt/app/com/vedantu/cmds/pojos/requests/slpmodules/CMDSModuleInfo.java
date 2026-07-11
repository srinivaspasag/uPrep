package com.vedantu.cmds.pojos.requests.slpmodules;

import java.util.List;

import com.vedantu.cmds.pojos.content.question.CMDSResourceInfo;
import com.vedantu.cmds.pojos.responses.slpmodules.ModuleEntryInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.models.ModuleRun;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSModuleInfo extends CMDSResourceInfo {

    public List<ModuleEntryInfo> children;
    public ModuleRun             moduleRun;
    public String                prerequsiteModuleId;

    public CMDSModuleInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, ModuleRun moduleRun,
            VedantuRecordState recordState, List<ModuleEntryInfo> children,
            String prerequsiteModuleId2, long size) {

        super(id, name, EntityType.CMDSMODULE, orgId, timeCreated, lastUpdated, addedBy,
                programsAddedTo, published, completed, converted, id, recordState, size);
        this.children = children;
        this.moduleRun = moduleRun;
        this.prerequsiteModuleId = prerequsiteModuleId2;
    }

    public CMDSModuleInfo(String id, String name, EntityType type, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, ModuleRun moduleRun,
            VedantuRecordState recordState, List<ModuleEntryInfo> children,
            String prerequsiteModuleId2) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, moduleRun, recordState, children,
                prerequsiteModuleId2, 0);
    }
}

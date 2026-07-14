package com.lms.pojos.requests.splModules;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.content.CMDSResourceInfo;
import com.lms.enums.ModuleRun;
import com.lms.pojos.ModuleEntryInfo;

import java.util.List;

public class CMDSModuleInfo extends CMDSResourceInfo {

    public List<ModuleEntryInfo> children;
    public ModuleRun moduleRun;
    public String prerequsiteModuleId;

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

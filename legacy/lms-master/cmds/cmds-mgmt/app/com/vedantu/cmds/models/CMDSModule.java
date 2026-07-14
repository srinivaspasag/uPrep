package com.vedantu.cmds.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.pojos.requests.slpmodules.CMDSModuleInfo;
import com.vedantu.cmds.pojos.responses.slpmodules.ModuleEntryInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.models.AbstractModuleModel;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.ModuleRun;

@Entity(value = "cmdsmodules", noClassnameStored = true)
public class CMDSModule extends AbstractModuleModel implements ICMDSModel {

    public boolean publishingInProgress;
    public String globalModuleId;

    public CMDSModule() {

        this.contentType = EntityType.CMDSMODULE;
    }

    public CMDSModule(String name, List<ModuleEntry> children, ModuleRun moduleRun) {

        super(name, children, moduleRun);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        CMDSModuleInfo moduleInfo = new CMDSModuleInfo(_getStringId(), name, EntityType.CMDSMODULE,
                orgId, timeCreated, lastUpdated, userId, 0, published, completed, true,
                globalModuleId, moduleRun, recordState, new ArrayList<ModuleEntryInfo>(),
                prerequsiteModuleId,this.getExportableSize());
        return moduleInfo;
    }

    @Override
    public String getGlobalId() {

        return globalModuleId;
    }

    @Override
    public long getExportableSize() {

        if (size != null) {
            return size.getTotalSize();
        }
        return 0;
    }

}
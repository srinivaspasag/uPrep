package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.ModuleRun;
import com.lms.interfaces.ICMDSModel;
import com.lms.pojos.ModuleEntryInfo;
import com.lms.pojos.requests.splModules.CMDSModuleInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "cmdsmodules")
@Setter
@Getter
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

        String orgId = (contentSrc != null) ? contentSrc.id : HardCodedConstants.emptyString;
        CMDSModuleInfo moduleInfo = new CMDSModuleInfo(_getStringId(), name, EntityType.CMDSMODULE,
                orgId, timeCreated, lastUpdated, userId, 0, published, completed, true,
                globalModuleId, moduleRun, recordState, new ArrayList<ModuleEntryInfo>(),
                prerequsiteModuleId, this.getExportableSize());
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
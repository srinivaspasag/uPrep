package com.vedantu.cmds.models;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.cmds.enums.ExportState;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.cmds.pojos.export.ExportRecordInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.OrgSection;

@Entity(value = "exports", noClassnameStored = true)
public class ExportRecord extends BaseRecord {

    public long                     total;
    public ExportState              state;
    public String                   fileId;
    public long                     compressedSize;
    public long                     exportedSize;
    public List<EntityExportRecord> sources;

    public ExportRecord() {

        super();
        this.contentType = EntityType.EXPORTRECORD;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        ExportRecordInfo info = new ExportRecordInfo(this._getStringId(), this.recordState,
                this.name, this.timeCreated, this.lastUpdated);
        OrgSection section = OrgSectionDAO.INSTANCE.getById(target.id);
        info.sectionInfo = section.toBasicInfo();
        info.programInfo = OrgProgramDAO.INSTANCE.getBasicInfo(section.programId);
        info.centerInfo = OrgCenterDAO.INSTANCE.getBasicInfo(section.centerId);
        info.orgInfo = OrganizationDAO.INSTANCE.getBasicInfo(section.orgId);
        info.userInfo = AbstractContentManager.getUserInfo(this.contentSrc.id, this.userId, true);

        if (StringUtils.isNotEmpty(this.targetUserId)) {
            info.exportedFor = AbstractContentManager.getUserInfo(this.contentSrc.id, this.userId,
                    true);
        }
        info.encLevel = this.encLevel;
        return info;

    }
}

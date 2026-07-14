package com.vedantu.cmds.pojos.content.question;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.managers.CMDSLibraryManager;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.pojos.tests.ResourceInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;

public class CMDSResourceInfo extends ResourceInfo {

    private static final ALogger LOGGER   = Logger.of(CMDSResourceInfo.class);

    public boolean               published;

    public String                globalId = null;

    public String                subType;

    public boolean               completed;

    public boolean               converted;

    public long                  size;

    public CMDSResourceInfo(String id, String name, EntityType type, String orgId,
            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
            boolean published, boolean completed, boolean converted, String globalId,
            VedantuRecordState recordState, long size) {

        super(id, name, type, timeCreated, lastUpdated, null, programsAddedTo, recordState);
        this.published = published;
        this.completed = completed;
        this.converted = converted;
        this.globalId = globalId;
        this.addedBy = (OrgMemberBasicInfo) OrgMemberDAO.INSTANCE.getMemberByUserId(orgId, addedBy)
                .toBasicInfo();
        LOGGER.debug(" values" + this.addedBy.toString());
        SrcEntity entity = new SrcEntity(type, id);
        this.programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(entity,
                CmdsContentLinkType.ADDED);
        this.size= size;


    }

    public CMDSResourceInfo(String id, String name, EntityType type, String orgId,
            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
            boolean published, boolean completed, boolean converted, String globalId,
            VedantuRecordState recordState) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, (long) 0);
  

    }

    @Override
    public String toString() {

        return "CMDSResourceInfo [published=" + published + ", globalId=" + globalId + ", addedBy="
                + addedBy + ", subType=" + subType + ", toString()=" + super.toString() + "]";
    }
}

package com.lms.content;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.pojos.tests.ResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDSResourceInfo extends ResourceInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMDSResourceInfo.class);

    public boolean published;

    public String globalId = null;

    public String subType;

    public boolean completed;

    public boolean converted;

    public long size;

    public CMDSResourceInfo(String id, String name, EntityType type, String orgId,
                            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
                            boolean published, boolean completed, boolean converted, String globalId,
                            VedantuRecordState recordState, long size) {

        super(id, name, type, timeCreated, lastUpdated, null, programsAddedTo, recordState);
        this.published = published;
        this.completed = completed;
        this.converted = converted;
        this.globalId = globalId;
      /*  this.addedBy = (OrgMemberBasicInfo) getMemberByUserId(orgId, addedBy)
                .toBasicInfo();
        LOGGER.debug(" values" + this.addedBy.toString());
        SrcEntity entity = new SrcEntity(type, id);
        this.programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(entity,
                CmdsContentLinkType.ADDED);*/
        this.size = size;


    }

    public CMDSResourceInfo(String id, String name, EntityType type, String orgId,
                            long timeCreated, long lastUpdated, String addedBy, long programsAddedTo,
                            boolean published, boolean completed, boolean converted, String globalId,
                            VedantuRecordState recordState) {

        this(id, name, type, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, 0);


    }

    @Override
    public String toString() {

        return "CMDSResourceInfo [published=" + published + ", globalId=" + globalId + ", addedBy="
                + addedBy + ", subType=" + subType + ", toString()=" + super.toString() + "]";
    }
}

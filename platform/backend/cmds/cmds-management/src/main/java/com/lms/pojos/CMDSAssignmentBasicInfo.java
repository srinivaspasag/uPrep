package com.lms.pojos;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.content.CMDSResourceInfo;

public class CMDSAssignmentBasicInfo extends CMDSResourceInfo {

    public String name;
    public int totalMarks;
    public int qusCount;
    public long duration;

    public CMDSAssignmentBasicInfo(String id, String name, String orgId, long timeCreated,
                                   long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                                   boolean completed, String globalId, VedantuRecordState recordState) {

        this(id, name, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, globalId, recordState, 0);
    }

    public CMDSAssignmentBasicInfo(String id, String name, String orgId, long timeCreated,
                                   long lastUpdated, String addedBy, long programsAddedTo, boolean published,
                                   boolean completed, String globalId, VedantuRecordState recordState, long size) {

        super(id, name, EntityType.CMDSASSIGNMENT, orgId, timeCreated, lastUpdated, addedBy,
                programsAddedTo, published, completed, true, globalId, recordState, size);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:");
        builder.append(name);
        builder.append(", type:");
        builder.append(type);
        builder.append(", published:");
        builder.append(published);
        builder.append(", globalId:");
        builder.append(globalId);
        builder.append(", addedBy:");
        builder.append(addedBy);
        builder.append(", programsAddedTo:");
        builder.append(programsAddedTo);
        builder.append(", timeCreated:");
        builder.append(timeCreated);
        builder.append(", lastUpdated:");
        builder.append(lastUpdated);
        builder.append(", id:");
        builder.append(id);
        builder.append(", recordState:");
        builder.append(recordState);
        builder.append("}");
        return builder.toString();
    }

}

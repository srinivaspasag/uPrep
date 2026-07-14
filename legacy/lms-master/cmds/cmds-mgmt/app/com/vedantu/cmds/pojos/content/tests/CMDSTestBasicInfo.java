package com.vedantu.cmds.pojos.content.tests;

import com.vedantu.cmds.pojos.content.question.CMDSResourceInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSTestBasicInfo extends CMDSResourceInfo {

    public String name;
    public int    qusCount;
    public long   duration;
    public int    totalMarks;

    public CMDSTestBasicInfo(String id, String name, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState,
            long size) {

        super(id, name, EntityType.CMDSTEST, orgId, timeCreated, lastUpdated, addedBy,
                programsAddedTo, published, completed, converted, globalId, recordState,size);
    }

    public CMDSTestBasicInfo(String id, String name, String orgId, long timeCreated,
            long lastUpdated, String addedBy, long programsAddedTo, boolean published,
            boolean completed, boolean converted, String globalId, VedantuRecordState recordState) {

        this(id, name, orgId, timeCreated, lastUpdated, addedBy, programsAddedTo, published,
                completed, converted, globalId, recordState, 0);
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

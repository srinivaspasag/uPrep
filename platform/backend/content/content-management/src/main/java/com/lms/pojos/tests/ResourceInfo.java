package com.lms.pojos.tests;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.pojo.UserInfo;

public class ResourceInfo extends ModelExtendedInfo {

    public UserInfo addedBy;

    public long programsAddedTo;
    // // - stats (defined by Ajith/Pulkit)"
    public String type;

    public ResourceInfo(String id, String name, EntityType type,
                        long timeCreated, long lastUpdated, String addedBy,
                        long programsAddedTo, VedantuRecordState recordState) {
        super(id, recordState, name, timeCreated, lastUpdated);
        this.programsAddedTo = programsAddedTo;
        this.type = type.name();

    }

    @Override
    public String toString() {

        return "ResourceInfo [addedBy=" + addedBy + ", programsAddedTo=" + programsAddedTo
                + ", type=" + type + ", toString()=" + super.toString() + "]";
    }
}

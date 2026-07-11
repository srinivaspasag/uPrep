package com.lms.pojos;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.pojos.tests.ResourceInfo;
import com.lms.user.vedantu.user.pojo.UserInfo;

public class RemarkInfo extends ResourceInfo {

    public String content;
    public UserInfo addedFor;

    public RemarkInfo(String id, String name, EntityType type,
                      long timeCreated, long lastUpdated, String addedBy,
                      long programsAddedTo, VedantuRecordState recordState) {
        super(id, name, type, timeCreated, lastUpdated, addedBy,
                programsAddedTo, recordState);
    }

}

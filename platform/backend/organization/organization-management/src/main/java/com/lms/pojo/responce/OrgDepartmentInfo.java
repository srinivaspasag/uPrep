package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;

public class OrgDepartmentInfo extends AbstractOrgStructureInfo {

    public OrgDepartmentInfo(String id, String name, String code,
                             VedantuRecordState recordState) {
        super(id, name, code, recordState);
    }

}

package com.lms.pojo;

import com.lms.common.vedantu.enums.AuthType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrgBasicInfo extends OrgStructureBasicInfo {

    public String   fullName;
    public String   thumbnail;
    public AuthType authType;

    public OrgBasicInfo(String id, VedantuRecordState recordState, String name) {

        super(id, recordState, name, null, EntityType.ORGANIZATION);

    }

}

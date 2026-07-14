package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.commons.enums.AuthType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class OrgBasicInfo extends OrgStructureBasicInfo {

    public String   fullName;
    public String   thumbnail;
    public AuthType authType;

    public OrgBasicInfo(String id, VedantuRecordState recordState, String name) {

        super(id, recordState, name, null, EntityType.ORGANIZATION);

    }

}

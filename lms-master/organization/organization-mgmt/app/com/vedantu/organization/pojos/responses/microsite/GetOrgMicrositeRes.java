package com.vedantu.organization.pojos.responses.microsite;

import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.pojos.MicrositeInfo;

public class GetOrgMicrositeRes extends MicrositeInfo {

    public GetOrgMicrositeRes(String id) {

        super(id, VedantuRecordState.ACTIVE);
    }
}

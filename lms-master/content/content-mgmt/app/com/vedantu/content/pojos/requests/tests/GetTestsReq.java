package com.vedantu.content.pojos.requests.tests;

import com.vedantu.content.pojos.requests.AbstractContentSearchReq;
import com.vedantu.mongo.VedantuRecordState;

public class GetTestsReq extends AbstractContentSearchReq {

    // will be used for assignments and tests
    public boolean published;
    public VedantuRecordState state = VedantuRecordState.ACTIVE;
}
package com.lms.pojos.requests;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetTestsReq extends AbstractContentSearchReq {

    // will be used for assignments and tests
    public boolean published;
    public VedantuRecordState state = VedantuRecordState.ACTIVE;
}
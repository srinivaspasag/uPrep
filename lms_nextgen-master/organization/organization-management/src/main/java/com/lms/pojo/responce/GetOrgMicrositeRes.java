package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.pojo.MicrositeInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetOrgMicrositeRes extends MicrositeInfo {

    public GetOrgMicrositeRes(String id) {

            super(id, VedantuRecordState.ACTIVE);
        }


}

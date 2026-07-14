package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateOrgMemberRes extends AddOrgMemberRes {

    public UpdateOrgMemberRes(String id, VedantuRecordState recordState,
                              String orgId, String userId) {
        super(id, recordState, orgId, userId);
    }

}

package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RemoveOrgMemberMappingRes extends AddOrgMemberMappingRes {

    public RemoveOrgMemberMappingRes(String id, VedantuRecordState recordState,
                                     boolean done) {
        super(id, recordState, done);
    }

}
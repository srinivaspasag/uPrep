package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractOrgStructureRes {

    public String id;
    public VedantuRecordState recordState;

}

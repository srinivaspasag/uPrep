package com.lms.pojo.responce;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateOrgRes extends AddOrgRes {

    public VedantuRecordState recordState;

}
package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileInfo extends ModelBasicInfo {

    public String name;

    public FileInfo() {

        super();
    }

    public FileInfo(String id, String name, VedantuRecordState recordState) {

        super(id, recordState);
        this.name = name;
    }
}
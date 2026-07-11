package com.vedantu.content.pojos;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

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
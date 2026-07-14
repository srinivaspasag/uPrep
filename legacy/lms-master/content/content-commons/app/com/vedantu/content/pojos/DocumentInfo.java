package com.vedantu.content.pojos;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;

public class DocumentInfo extends ModelBasicInfo {

    public String name;

    public DocumentInfo() {

        super();
    }

    public DocumentInfo(String id, String name, VedantuRecordState recordState) {

        super(id, recordState);
        this.name = name;
    }
}

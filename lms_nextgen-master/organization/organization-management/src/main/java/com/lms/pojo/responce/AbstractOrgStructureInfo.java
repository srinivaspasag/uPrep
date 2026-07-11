package com.lms.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.mongo.VedantuRecordState;

public class AbstractOrgStructureInfo implements IListResponseObj {

    public String             id;
    public String             name;
    public String             code;
    public VedantuRecordState recordState;

    public AbstractOrgStructureInfo(String id, String name, String code,
                                    VedantuRecordState recordState) {

        super();
        this.id = id;
        this.name = name;
        this.code = code;
        this.recordState = recordState;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", name:").append(name).append(", code:")
                .append(code).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
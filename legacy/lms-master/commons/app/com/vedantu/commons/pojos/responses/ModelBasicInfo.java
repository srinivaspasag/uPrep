package com.vedantu.commons.pojos.responses;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.mongo.IVedantuModel;
import com.vedantu.mongo.VedantuRecordState;

public class ModelBasicInfo implements IListResponseObj, IVedantuModel {

    public String             id;
    public VedantuRecordState recordState;


    public ModelBasicInfo() {
    }
    
    public ModelBasicInfo(String id, VedantuRecordState recordState) {

        this.id = id;
        this.recordState = recordState;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ModelBasicInfo)) {
            return false;
        }
        ModelBasicInfo b = (ModelBasicInfo) o;
        return null != b && StringUtils.equals(id, b.id);
    }

    @Override
    public int hashCode() {

        return StringUtils.defaultIfEmpty(id, StringUtils.EMPTY).hashCode();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:");
        builder.append(id);
        builder.append(", recordState:");
        builder.append(recordState);
        builder.append("}");
        return builder.toString();
    }

}

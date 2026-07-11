package com.lms.common.vedantu.commons.pojos.requests.responces;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ModelBasicInfo implements IListResponseObj {


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
        return null != b && id.equals(b.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, recordState);
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

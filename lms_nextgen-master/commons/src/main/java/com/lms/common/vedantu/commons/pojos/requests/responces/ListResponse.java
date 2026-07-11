package com.lms.common.vedantu.commons.pojos.requests.responces;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ListResponse<T extends IListResponseObj> {

    public long totalHits;
    public List<T> list = new ArrayList<T>();
    public VedantuErrorCode cumulativeErrorCode;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{totalHits:");
        builder.append(totalHits);
        builder.append(", list:");
        builder.append(list);
        builder.append("}");
        return builder.toString();
    }
}
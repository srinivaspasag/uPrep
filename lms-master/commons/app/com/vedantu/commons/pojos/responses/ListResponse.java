package com.vedantu.commons.pojos.responses;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.VedantuErrorCode;

public class ListResponse<T extends IListResponseObj> {

    public long             totalHits;
    public List<T>          list = new ArrayList<T>();
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

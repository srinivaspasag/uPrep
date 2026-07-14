package com.lms.common.vedantu.mongo;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;

import java.util.ArrayList;
import java.util.List;

public class VedantuDBResult <T extends VedantuBaseMongoModel> {

    public int totalHits;
    public List<T> results = new ArrayList<T>();

    @Override
    public String toString() {
        return "VedantuDBResult [totalHits=" + totalHits + ", results="
                + results + "]";
    }



}
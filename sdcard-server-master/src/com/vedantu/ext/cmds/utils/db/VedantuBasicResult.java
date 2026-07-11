package com.vedantu.ext.cmds.utils.db;

import java.util.ArrayList;
import java.util.List;

public class VedantuBasicResult<T> {

    public long    totalHits;
    public List<T> list = new ArrayList<T>();

    public void add(T model) {

        list.add(model);
    }

}

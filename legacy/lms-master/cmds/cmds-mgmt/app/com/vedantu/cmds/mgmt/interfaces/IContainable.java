package com.vedantu.cmds.mgmt.interfaces;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;

import com.vedantu.mongo.VedantuRecordState;

public interface IContainable<T> {

    public List<T> getContainers(String id, int start, int size, VedantuRecordState state,
            MutableLong totalHits);
}

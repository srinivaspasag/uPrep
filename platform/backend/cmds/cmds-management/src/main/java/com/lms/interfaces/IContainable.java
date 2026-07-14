package com.lms.interfaces;

import com.lms.common.vedantu.mongo.VedantuRecordState;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public interface IContainable<T> {

    List<T> getContainers(String id, int start, int size, VedantuRecordState state,
                          AtomicLong totalHits);
}

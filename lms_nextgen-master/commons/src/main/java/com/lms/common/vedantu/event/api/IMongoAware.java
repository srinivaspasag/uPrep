package com.lms.common.vedantu.event.api;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;

public interface IMongoAware {
    public void fromMongoModel(VedantuBaseMongoModel mongoModel);

}
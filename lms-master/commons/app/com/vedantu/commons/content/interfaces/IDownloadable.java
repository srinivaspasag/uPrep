package com.vedantu.commons.content.interfaces;

import com.vedantu.mongo.VedantuBaseMongoModel;


public interface IDownloadable {
    /**
     * Returns FileName for download without extension
     * @param id
     * @param mongoModel
     * @return
     */
    public String getDownloadName(String id, VedantuBaseMongoModel mongoModel );
}

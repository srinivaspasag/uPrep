package com.vedantu.commons.content.interfaces;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IPublishable extends IContent {

    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException;

    public SrcEntity getGlobalEntity(String id);

    public boolean isPublished(String id);

    public boolean isReadyToPublished(String id) throws VedantuException;

    public boolean isPublished(VedantuBaseMongoModel model);

    public boolean isReadyToPublished(VedantuBaseMongoModel model) throws VedantuException;

    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException;

}

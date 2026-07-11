package com.lms.common.content.interfaces;

import java.util.List;

import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.DownloadableFileInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;


public interface IContentManager {

    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents)
            throws VedantuException;

    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException;
}

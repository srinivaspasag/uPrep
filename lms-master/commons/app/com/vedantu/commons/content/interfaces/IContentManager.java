package com.vedantu.commons.content.interfaces;

import java.util.List;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface IContentManager {

    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents)
            throws VedantuException;

    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException;
}

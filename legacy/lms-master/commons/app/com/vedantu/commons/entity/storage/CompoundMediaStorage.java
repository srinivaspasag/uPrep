package com.vedantu.commons.entity.storage;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.FileData;

public class CompoundMediaStorage extends AbstractEntityFileStorage {

    public CompoundMediaStorage() {

        super(EntityType.COMPOUNDMEDIA);
    }

    @Override
    public FileData getData(String entityType, String mediaType, String fileName)
            throws EntityFileStorageException {

        return super.getFromFs(folderId, fileName);
    }
}

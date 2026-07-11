package com.vedantu.commons.entity.storage;

import java.io.File;
import java.util.Map;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.FileData;

public class DocumentEntityFileStorage extends AbstractEntityFileStorage {

    public DocumentEntityFileStorage() {
        super(EntityType.DOCUMENT);
    }

    @Override
    public StorageResult storeImage(final String uid, final File file,
            final FileCategory fileCategory, final ImageSize imageSize,
            final Map<String, String> tags) throws EntityFileStorageException {
        return super.storeImage(uid, file, fileCategory, imageSize, tags);
    }

    public StorageResult store(final String uid, final File file,
            final FileCategory fileCategory, final Map<String, String> tags)
            throws EntityFileStorageException {
        // Commented By Shankhoneer: Even if image is uploaded as a doc it
        // should get moved to OS as a doc not image
        
        // ImageFilter imageFilter = new ImageFilter();
        // if( imageFilter.accept(file) ){
        // return super.storeImage(uid, file, fileCategory, ImageSize.ORIGINAL,
        // tags);
        // }

        return super.store(uid, file, MediaType.DOC, fileCategory, tags);
    }

    @Override
    public FileData getData(String entityType, String mediaType, String fileName)
            throws EntityFileStorageException {
        return super.getData(entityType, mediaType, fileName);
    }
}

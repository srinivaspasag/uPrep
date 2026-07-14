package com.lms.common.vedantu.entity.storage;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.enums.ImageSize;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Component;
@Component
public class DocumentEntityFileStorage extends AbstractEntityFileStorage {

   /* public DocumentEntityFileStorage() {
       // super(EntityType.DOCUMENT);
    }*/

    @Override
    public StorageResult storeImage(final String uid, final File file,
                                    final FileCategory fileCategory, final ImageSize imageSize,
                                    final Map<String, String> tags) throws VedantuException {
        return super.storeImage(uid, file, fileCategory, imageSize, tags);
    }

    public StorageResult store(final String uid, final File file,
                               final FileCategory fileCategory, final Map<String, String> tags)
            throws VedantuException {
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
            throws VedantuException {
        return super.getData(entityType, mediaType, fileName);
    }
}

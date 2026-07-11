package com.lms.common.vedantu.entity.storage;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import org.w3c.dom.ranges.Range;


import java.io.File;
import java.util.Map;

public interface IEntityFileStorage {

    public StorageResult storeImage(final String uid, final File file,
                                    final FileCategory fileCategory, final ImageSize imageSize,
                                    final Map<String, String> tags) throws Exception;

    public String computeDisplayUrlComponent(final String uid, final String fileExt,
                                             final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize);

    public FileData getData(String entityType, String mediaType, String fileName)
            throws VedantuException;


    public FileData getData(String entityType, String mediaType, String fileName, Range ranges)
            throws VedantuException;

    public String getStorageId();

    public boolean doesFileExist(String entityType, String mediaType, String fileName);

    public boolean remove(String entityType, String mediaType, String fileName);

    public long size(String uid, EntityType type, String fileExt, MediaType mediaType,
                     FileCategory fileCategory, ImageSize imageSize);

    public FileData getSecuredURL(String uid, EntityType type, String fileExt, MediaType mediaType,
                                  FileCategory fileCategory, ImageSize imageSize) ;

    public String computeDisplayS3UrlComponent(String uid, String webmExtentionWithoutDot,
                                               MediaType video, FileCategory converted);
    public FileData getSecureURL(EntityType eType,MediaType mediaType, String fileName) throws Exception ;


}

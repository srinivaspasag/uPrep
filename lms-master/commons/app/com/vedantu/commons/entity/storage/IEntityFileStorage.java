package com.vedantu.commons.entity.storage;

import java.io.File;
import java.util.Map;

import org.restlet.data.Range;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.FileData;

public interface IEntityFileStorage {

    public StorageResult storeImage(final String uid, final File file,
            final FileCategory fileCategory, final ImageSize imageSize,
            final Map<String, String> tags) throws EntityFileStorageException;

    public String computeDisplayUrlComponent(final String uid, final String fileExt,
            final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize);

    public FileData getData(String entityType, String mediaType, String fileName)
            throws EntityFileStorageException;

    public FileData getData(String entityType, String mediaType, String fileName, Range ranges)
            throws EntityFileStorageException;

    public String getStorageId();

    public boolean doesFileExist(String entityType, String mediaType, String fileName);

    public boolean remove(String entityType, String mediaType, String fileName);

    public long size(String uid, EntityType type, String fileExt, MediaType mediaType,
            FileCategory fileCategory, ImageSize imageSize);

    public FileData getSecuredURL(String uid, EntityType type, String fileExt, MediaType mediaType,
            FileCategory fileCategory, ImageSize imageSize) ;

    public String computeDisplayS3UrlComponent(String uid, String webmExtentionWithoutDot,
            MediaType video, FileCategory converted);

}

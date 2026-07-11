package com.lms.common.fs.handlers;

import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.responce.SignUploadFileRes;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.StorageIdentification;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public interface IFileSystemHandler {
    boolean createParent(String dir) throws VedantuException, FileStoreException;

    boolean store(File file, String destDir, String destFileName, Map<String, String> tags)
            throws FileStoreException, FileNotFoundException;

    FileData get(String sourceDir, String srcFileName) throws VedantuException;

    boolean delete(String sourceDir, String srcFileName) throws VedantuException;

    boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws VedantuException;

    boolean move(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws VedantuException;

    FileData get(String sourceDir, String srcFileName, long index, long size)
            throws VedantuException;

    String getParentName(EntityType entityType, String fwkId);

    SignUploadFileRes signContentUpload(EntityType entityType, String bucketName,
                                        String fileName, String contentType) throws FileStoreException;

    boolean exists(String sourceDir, String srcFileName) throws VedantuException;

    StorageIdentification getIdentification() throws VedantuException;

    boolean removeParent(String dirPath) throws VedantuException;

    long size(String sourceDir, String srcFileName) throws VedantuException;

    FileData getSecureURL(String sourceId, EntityType eType, MediaType mediaType, String fileName) throws FileStoreException;

}

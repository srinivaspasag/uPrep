package com.vedantu.commons.fs.handlers;

import java.io.File;
import java.util.Map;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.StorageIdentification;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.responses.SignUploadFileRes;
import com.vedantu.commons.pojos.FileData;

public interface IFileSystemHandler {

    public boolean createParent(String dir) throws FileStoreException;

    public boolean store(File file, String destDir, String destFileName, Map<String, String> tags)
            throws FileStoreException;

    public FileData get(String sourceDir, String srcFileName) throws FileStoreException;

    public boolean delete(String sourceDir, String srcFileName) throws FileStoreException;

    public boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException;

    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException;

    public FileData get(String sourceDir, String srcFileName, long index, long size)
            throws FileStoreException;

    public String getParentName(EntityType entityType, String fwkId);

    public SignUploadFileRes signContentUpload(EntityType entityType, String bucketName,
            String fileName, String contentType) throws FileStoreException;

    public boolean exists(String sourceDir, String srcFileName) throws FileStoreException;

    public StorageIdentification getIdentification() throws FileStoreException;

    boolean removeParent(String dirPath) throws FileStoreException;

    public long size(String sourceDir, String srcFileName) throws FileStoreException;

    public FileData getSecureURL(EntityType eType,MediaType mediaType, String fileName) throws FileStoreException;

}
package com.vedantu.commons.fs.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.restlet.data.Form;
import org.restlet.data.Range;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.StorageIdentification;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.managers.ObjectStorageManager;
import com.vedantu.commons.fs.objectstorage.Container;
import com.vedantu.commons.fs.objectstorage.ObjectFileEx;
import com.vedantu.commons.fs.responses.SignUploadFileRes;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.utils.FileUtils;

public class ObjectStorageHandler implements IFileSystemHandler {

    private final static ALogger LOGGER        = Logger.of(ObjectStorageHandler.class);
    private static final String  X_OBJECT_META = "X-Object-Meta-";

    @Override
    public boolean store(File localFile, String destDir, String destFileName,
            Map<String, String> tags) throws FileStoreException {

        try {
            LOGGER.debug("Uploading file : " + localFile.getAbsolutePath());
            createParent(destDir);

            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(destDir,
                    destFileName);

            String result = file.uploadFile(localFile, tags);
            LOGGER.debug("Uploaded file : " + localFile.getAbsolutePath() + " Result " + result);

        } catch (Exception exp) {
            // LOGGER.info("Could not store file to ObjectStorage at " +
            // ObjectStorageManager.get().getBaseUrl() );
            throw new FileStoreException("Could not upload file " + localFile.getAbsolutePath()
                    + " to ObjectStorage at " + ObjectStorageManager.get().getBaseUrl(), exp);
        }
        return true;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName) throws FileStoreException {

        try {
            LOGGER.debug("Retrieving file : " + srcFileName);
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(sourceDir,
                    srcFileName);
            Map<String, String> tags = getUserMetadata(file);
            FileData data = new FileData(tags, file.getStream());
            data.setFileSize(file.getStream().available());
            return data;
        } catch (Exception exp) {
            throw new FileStoreException("Could not get file from ObjectStorage", exp);
        }

    }

    @Override
    public boolean createParent(String dir) throws FileStoreException {

        try {
            Container container = ObjectStorageManager.get().getContainer(dir);
            container.create();
        } catch (Exception exception) {
            throw new FileStoreException("Could not create container " + dir
                    + "on ObjectStorage at " + ObjectStorageManager.get().getBaseUrl(), exception);
        }
        return true;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws FileStoreException {

        try {
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(sourceDir,
                    srcFileName);

            file.remove();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOGGER.info("Could not delete file from ObjectStorage at "
                    + ObjectStorageManager.get().getBaseUrl());
            throw new FileStoreException("Could not get file from ObjectStorage at "
                    + ObjectStorageManager.get().getBaseUrl());
        }

        return true;
    }

    @Override
    public boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        try {
            createParent(destDir);
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(destDir,
                    destFileName);
            file.copyFrom(sourceDir, srcFileName);

        } catch (Exception exp) {
            throw new FileStoreException("Could not copy file " + srcFileName + " to  "
                    + destFileName, exp);
        }

        return true;
    }

    @Override
    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        try {
            createParent(destDir);
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(destDir,
                    destFileName);

            file.copyFrom(sourceDir, srcFileName);
            ObjectFileEx srcFile = ObjectStorageManager.get().getObjectFileExtended(sourceDir,
                    srcFileName);
            srcFile.remove();

        } catch (Exception exp) {
            throw new FileStoreException("Could not move file " + srcFileName + " to  "
                    + destFileName, exp);

        }
        return true;
    }

    public static void main(String args[]) {

        try {

            ObjectStorageHandler handler = new ObjectStorageHandler();
            System.out.println(" storing to object store ");
            // handler.createParent("newtest");
            System.out.println(" created document ");

            Map<String, String> tags = new HashMap<String, String>();
            tags.put("id", "myphoto");
            tags.put("date", "jan25");

            tags.put("Content-type", "image/jpeg");
            handler.store(new File("/home/vikram/Documents/1.jpg"), "test_documents",
                    "testfile1.jpeg", tags);
            FileData data = handler.get("test_documents", "testfile1.jpeg");
            System.out.println(data.getFileMetaInfo());
            // handler.delete("documents", "abcd.jpeg") ;

            Container container = ObjectStorageManager.get().getContainer("test_documents");
            System.out.println(container.listObjectFiles());
            // handler.delete("documents", "abcd.jpeg");
            FileData dataw = handler.get("test_documents", "testfile1.jpeg");
            System.out.println(" TagInfo :" + dataw.getFileMetaInfo());

            // handler.move("test_documents",
            // "test_documents2","testfile1.jpeg", "test5.jpeg");

            handler.copy("test_documents", "test_documents2", "testfile1.jpeg", "test9.jpeg");
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size)
            throws FileStoreException {

        try {
            LOGGER.debug("Retrieving file : " + srcFileName);
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(sourceDir,
                    srcFileName);
            Range range = new Range(index, size);
            LOGGER.debug("Object storage requested Range: " + range.getIndex() + " "
                    + range.getSize());

            FileData data = new FileData(null, file.getStream());

            LOGGER.debug("Retrieved file : " + srcFileName + " tags");
            data.fileMetaInfo = getUserMetadata(file);;
            System.out.println("Data size " + file.inputstream.available());
            data.setContentLength(file.inputstream.available());
            return data;
        } catch (Exception exp) {
            throw new FileStoreException("Could not get file from ObjectStorage", exp);
        }

    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {

        return fwkId.toLowerCase() + FileUtils.SEPARATOR_UNDERSCORE
                + entityType._getStorageId().toLowerCase();
    }

    @Override
    public SignUploadFileRes signContentUpload(EntityType entityType, String bucketName,
            String fileName, String contentType) {

        return null;
    }

    public List<ObjectFileEx> getParentContent(String containerName) throws FileStoreException {

        Container container = null;
        List<ObjectFileEx> objectFiles = new ArrayList<ObjectFileEx>();
        try {
            container = ObjectStorageManager.get().getContainer(containerName);
            objectFiles = container.listObjectFiles();

        } catch (Exception e) {
            throw new FileStoreException("Could not get file from ObjectStorage", e);
        }
        return objectFiles;

    }

    @SuppressWarnings("finally")
    public Map<String, String> getUserMetadata(ObjectFileEx file) {

        Map<String, String> tags = new HashMap<String, String>();
        try {
            Object hold = file.getMetaTags().get("org.restlet.http.headers");
            Form form = (Form) hold;

            Map<String, String> metaTags = form.getValuesMap();

            LOGGER.debug(" Metatag type " + metaTags.get("org.restlet.http.headers"));
            tags = new HashMap<String, String>();

            if (metaTags != null) {

                for (String tagkey : metaTags.keySet()) {

                    if (tagkey.startsWith(X_OBJECT_META)) {
                        String newTag = tagkey.substring(X_OBJECT_META.length());
                        tags.put(newTag, metaTags.get(tagkey).toString());
                    }
                }
            }
            LOGGER.debug("Retrieved file : " + file.getName() + " tags" + tags
                    + " file size available " + file.getStream().available());
        } catch (Exception ex) {
            LOGGER.debug("Can not get tags from file " + file.getName());

        } finally {

            return tags;
        }
    }

    @Override
    public boolean exists(String sourceDir, String srcFileName) throws FileStoreException {

        try {
            LOGGER.debug("Retrieving file : " + srcFileName);
            ObjectFileEx file = ObjectStorageManager.get().getObjectFileExtended(sourceDir,
                    srcFileName);

            return CollectionUtils.isNotEmpty(file.getHeaders().entrySet());
        } catch (Exception exp) {
            throw new FileStoreException("Could not get file from ObjectStorage", exp);
        }

    }

    @Override
    public StorageIdentification getIdentification() throws FileStoreException {

        return StorageIdentification.OS;
    }

    @Override
    public boolean removeParent(String dirPath) throws FileStoreException {

        return false;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws FileStoreException {

        // TODO not implemented as we are not going to use it anymore
        return -1;
    }

    @Override
    public FileData getSecureURL(EntityType eType,MediaType mediaType, String fileName) {

        return null;
    }
}

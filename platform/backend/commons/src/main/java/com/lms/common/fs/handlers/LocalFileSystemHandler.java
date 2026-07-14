package com.lms.common.fs.handlers;

import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.responce.SignUploadFileRes;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.StorageIdentification;
import com.lms.common.vedantu.mongo.FileMetaInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalFileSystemHandler implements IFileSystemHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileSystemHandler.class);
    public static final String PATH_SEPARATOR = "/";
    public static final String FILE_EXTENSION_DOT = ".";

    String baseDirectory = null;
    public boolean metadataNeeded = false;

    @Value("${fs.local.basedir}")
    private String fsLocalBaseUrl;
    @Value("${util.temp_dir}")
    private String utilTempDir;

    
   /* public LocalFileSystemHandler() {

        this(true);
    }

    public LocalFileSystemHandler(boolean metadataNeeded) {


        this(metadataNeeded, fsLocalBaseUrl);

    }

  public LocalFileSystemHandler(boolean metadataNeeded, String directoryLocation) {

        this.baseDirectory = directoryLocation;
        File storeDirectory = new File(baseDirectory);
        if (!storeDirectory.exists()) {
            logger.debug(" Created base directory for local file system handler: " + baseDirectory);
            storeDirectory.mkdir();
        }
        this.metadataNeeded = metadataNeeded;

    }*/

    public void localFileSystemHandlerBaseDirectory(boolean metadataNeeded) {
        localFileSystemHandlerDirectory(true, fsLocalBaseUrl);

    }

    public void localFileSystemHandlerTempDirectory(boolean metadataNeeded) {
        localFileSystemHandlerDirectory(true, utilTempDir);

    }

    public void localFileSystemHandlerDirectory(boolean metadataNeeded, String directoryLocation) {
        this.baseDirectory = directoryLocation;
        File storeDirectory = new File(baseDirectory);
        if (!storeDirectory.exists()) {
            logger.debug(" Created base directory for local file system handler: " + baseDirectory);
            storeDirectory.mkdir();
        }
        this.metadataNeeded = metadataNeeded;
    }

    public String getDirectory() {

        return baseDirectory;
    }

    public String getFilePath(String directoryName, String fileName) {

        if (directoryName != null) {
            File directory = new File(baseDirectory + PATH_SEPARATOR + directoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }
            return baseDirectory + PATH_SEPARATOR + directoryName + PATH_SEPARATOR + fileName;
        }
        return baseDirectory + PATH_SEPARATOR + fileName;
    }

    @Override
    public boolean createParent(String dirPath) throws VedantuException, FileStoreException {
        if (baseDirectory != null) {
            File createDirectory = new File(baseDirectory + File.separator + dirPath);
            try {
                FileUtils.forceMkdir(createDirectory);
            } catch (IOException e) {
                throw new FileStoreException("Could not create directory " + dirPath, e);
            }
        }
        return true;
    }

    @Override
    public boolean store(File localFile, String destDir, String destFileName, Map<String, String> tags) throws VedantuException, FileStoreException {
        FileMetaInfo fileInfo = new FileMetaInfo(destFileName);
        fileInfo.add(tags);
        fileInfo.add("name", destFileName);
        if (this.metadataNeeded) {
            // FileMetaInfoDAO.INSTANCE.save(fileInfo);
        }
        File destDirectory = new File(baseDirectory + File.separator + destDir);
        try {
            FileUtils.forceMkdir(destDirectory);
        } catch (IOException e) {
            throw new FileStoreException("Could not create directory " + destDir, e);
        }

        File destFile = new File(destDirectory, destFileName);
        try {
            fileInfo.setSize(localFile.length());
            if (localFile.isDirectory()) {
                FileUtils.copyDirectory(localFile, destFile);
            } else {
                logger.debug("Deleting file first" + destFile.getAbsolutePath() + " copy "
                        + localFile.getAbsolutePath());
                FileUtils.deleteQuietly(destFile);
                FileUtils.copyFile(localFile, destFile);
            }
        } catch (IOException e) {
            throw new FileStoreException("Could not copy file " + localFile.getAbsolutePath()
                    + " to directory " + destFileName, e);
        }

        return true;

    }

    @Override
    public FileData get(String sourceDir, String srcFileName) throws VedantuException {
        return null;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size) throws VedantuException {
        return null;
    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {
        return fwkId.toLowerCase() + com.lms.common.utils.FileUtils.SEPARATOR_UNDERSCORE
                + entityType._getStorageId().toLowerCase();
    }

    @Override
    public SignUploadFileRes signContentUpload(EntityType entityType, String bucketName, String fileName, String contentType) throws VedantuException {
        return null;
    }

    @Override
    public boolean exists(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override

    public StorageIdentification getIdentification() throws VedantuException {

        return StorageIdentification.LOCAL;
    }

    @Override
    public boolean removeParent(String dirPath) throws VedantuException {
        return false;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws VedantuException {
        File file = new File(baseDirectory + File.separator + sourceDir + File.separator
                + srcFileName);
        logger.debug("Getting size for file" + file.getAbsolutePath());
        return file.length();
    }

    @Override
    public FileData getSecureURL(String sourceId, EntityType eType, MediaType mediaType, String fileName) throws FileStoreException {
        FileData data = new FileData();
        data.setContentLength(size(sourceId,
                fileName));

        List<String> pathComponents = new ArrayList<String>();
        pathComponents.add(eType.name().toLowerCase());
        pathComponents.add(mediaType.getAcronym().toLowerCase());
        pathComponents.add(fileName);
        String securedURL = ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL
                + pathComponents.stream().collect(Collectors.joining(com.lms.common.utils.FileUtils.SEPARATOR_FWDSLASH));
        data.setSecuredURL(securedURL);
        return data;
    }

    /**
     * gives back new file with random id
     *
     * @param directoryName
     * @param fileExtension
     * @return
     */
    public File getNewFile(String directoryName, String fileExtension) {

        return new File(getFilePath(directoryName, UUID.randomUUID().toString()
                + FILE_EXTENSION_DOT + fileExtension));

    }

    public File getFileWithSpecifiedName(String directoryName, String fileName,
                                         String fileExtensionWithoutDot) {

        return new File(getFilePath(directoryName, fileName + FILE_EXTENSION_DOT
                + fileExtensionWithoutDot));
    }

    /**
     * gives back new file with random id
     *
     * @param directoryName
     * @param fileExtension
     * @return
     */
    /*public File getNewFile(String directoryName, String fileExtension) {

        return new File(getFilePath(directoryName, UUID.randomUUID().toString()
                + FILE_EXTENSION_DOT + fileExtension));

    }

    /**
     * Takes file name w/o extension
     *
     * @param directoryName
     * @param fileName
     * @param fileExtensionWithoutDot
     * @return
     */
  /*  public File getFileWithSpecifiedName(String directoryName, String fileName,
                                         String fileExtensionWithoutDot) {

        return new File(getFilePath(directoryName, fileName + FILE_EXTENSION_DOT
                + fileExtensionWithoutDot));
    }

    @Override
    public boolean createParent(String dirPath) throws FileStoreException {

        if (baseDirectory != null) {
            File createDirectory = new File(baseDirectory + File.separator + dirPath);
            try {
                FileUtils.forceMkdir(createDirectory);
            } catch (IOException e) {
                throw new FileStoreException("Could not create directory " + dirPath, e);
            }
        }
        return true;
    }

    @Override
    public boolean removeParent(String dirPath) {

        if (baseDirectory != null) {
            File createDirectory = new File(baseDirectory + File.separator + dirPath);

            return FileUtils.deleteQuietly(createDirectory);

        }
        return false;
    }

    @Override
    public boolean store(File localFile, String destDir, String destFileName,
                         Map<String, String> tags) throws FileStoreException {

        FileMetaInfo fileInfo = new FileMetaInfo(destFileName);
        fileInfo.add(tags);
        fileInfo.add("name", destFileName);
        if (this.metadataNeeded) {
            FileMetaInfoDAO.INSTANCE.save(fileInfo);
        }
        File destDirectory = new File(baseDirectory + File.separator + destDir);
        try {
            FileUtils.forceMkdir(destDirectory);
        } catch (IOException e) {
            throw new FileStoreException("Could not create directory " + destDir, e);
        }

        File destFile = new File(destDirectory, destFileName);
        try {
            fileInfo.setSize(localFile.length());
            if (localFile.isDirectory()) {
                FileUtils.copyDirectory(localFile, destFile);
            } else {
                LOGGER.debug("Deleting file first" + destFile.getAbsolutePath() + " copy "
                        + localFile.getAbsolutePath());
                FileUtils.deleteQuietly(destFile);
                FileUtils.copyFile(localFile, destFile);
            }
        } catch (IOException e) {
            throw new FileStoreException("Could not copy file " + localFile.getAbsolutePath()
                    + " to directory " + destFileName, e);
        }

        return true;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName) throws FileStoreException {

        // FileMetaInfo result = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
        // if (result == null && metadataNeeded) {
        // LOGGER.debug("File not found for name " + srcFileName);
        // throw new FileStoreException("File metadata not found", new FileNotFoundException());
        // }
        File file = new File(baseDirectory + File.separator + sourceDir + File.separator
                + srcFileName);

        InputStream instream = null;
        FileData fileData = null;
        try {
            instream = new FileInputStream(file);

            // LOGGER.debug("File found for name " + file.getAbsolutePath() + result.getTags());
            // fileData = new FileData( fileInfo.getDbObject().toMap(), instream
            // );
            fileData = new FileData(null, instream);
            long fileSize = file.length();
            if (fileSize == -1) {
                try {
                    fileSize = instream.available();
                    // result.setSize(fileSize);
                    // if (metadataNeeded) {
                    // FileMetaInfoDAO.INSTANCE.save(result);
                    // }
                } catch (IOException e) {
                    logger.error("Can not calculate file size");
                    fileSize = AbstractEntityFileStorage.MAXIMUM_FILE_SIZE_ALLOWED;
                }
            }
            fileData.setFileSize(fileSize);
            fileData.setContentLength(fileSize);
            // fileData.setTotalContentLength(fileSize);
            e.debug("FileData collected as " + fileData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            IOUtils.closeQuietly(instream);
            throw new Exception("Could not read file " + srcFileName + " from "
                    + sourceDir, e);

        }
        return fileData;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws FileStoreException {

        FileMetaInfo result = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
        if (result == null && metadataNeeded) {
            LOGGER.debug("File not found for name " + srcFileName);
            throw new FileStoreException("File metadata not found", new FileNotFoundException());
        }

        LOGGER.info("deleting following file: " + baseDirectory + File.separator + sourceDir
                + File.separator + srcFileName);
        return FileUtils.deleteQuietly(new File(baseDirectory + File.separator + sourceDir
                + File.separator + srcFileName));
    }

    @Override
    public boolean copy(String srcDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        File srcFile = new File(baseDirectory + File.separator + srcDir + File.separator
                + srcFileName);
        File destFile = new File(baseDirectory + File.separator + destDir + File.separator
                + destFileName);

        FileMetaInfo srcfileInfo = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
        if (srcfileInfo == null && metadataNeeded) {
            LOGGER.debug("File not found for name " + srcFileName);
            throw new FileStoreException("File metadata not found", new FileNotFoundException());
        }

        if (metadataNeeded) {
            FileMetaInfo destFileInfo = new FileMetaInfo(destFileName);
            destFileInfo.setTags(srcfileInfo.getTags());
            FileMetaInfoDAO.INSTANCE.save(destFileInfo);
        }

        try {
            LOGGER.debug("Moving file from " + srcDir + " / " + srcFileName + " to  " + destDir
                    + " / " + destFileName);
            FileUtils.copyFile(srcFile, destFile, true);
        } catch (IOException e) {
            throw new FileStoreException("Could not copy file " + srcFile.getPath() + " to  "
                    + destFile.getPath(), e);
        }
        return true;
    }

    @Override
    public boolean move(String srcDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        File srcFile = new File(baseDirectory + File.separator + srcDir + File.separator
                + srcFileName);
        File destFile = new File(baseDirectory + File.separator + destDir + File.separator
                + destFileName);
        FileMetaInfo srcfileInfo = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
        if (srcfileInfo == null && metadataNeeded) {
            LOGGER.debug("File not found for name " + srcFileName);
            throw new FileStoreException("File metadata not found", new FileNotFoundException());
        }

        if (metadataNeeded) {
            FileMetaInfo destFileInfo = new FileMetaInfo(destFileName);
            destFileInfo.setTags(srcfileInfo.getTags());
            FileMetaInfoDAO.INSTANCE.save(destFileInfo);
        }
        try {
            FileUtils.moveFile(srcFile, destFile);
        } catch (IOException e) {
            // ODO Auto-generated catch block
            throw new FileStoreException("Could not move file " + srcFile.getPath() + " to  "
                    + destFile.getPath(), e);
        }
        if (metadataNeeded) {
            FileMetaInfoDAO.INSTANCE.delete(srcfileInfo);
        }
        return true;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size)
            throws FileStoreException {

        // TODO Auto-generated method stub
        LOGGER.debug("Getting chunk of size :" + size + " of file" + srcFileName
                + "  from directory " + sourceDir + " from byte :" + index);

        FileMetaInfo fileInfo = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);

        if (fileInfo == null) {
            LOGGER.debug("File metadata not found for name " + srcFileName);
            throw new FileStoreException("File metadata not found", new FileNotFoundException());
        }

        File file = new File(baseDirectory + File.separator + sourceDir + File.separator
                + srcFileName);

        InputStream instream = null;

        FileData fileData = null;
        long fileSize = metadataNeeded ? fileInfo.getSize() : 0;
        try {
            // instream = new FileInputStream(file);
            instream = new FileInputStream(file);

            if (index != 0) {
                instream.skip(index);
            }

            if (fileSize == -1) {
                try {
                    fileSize = instream.available();
                    fileInfo.setSize(fileSize);
                    if (metadataNeeded) {
                        FileMetaInfoDAO.INSTANCE.save(fileInfo);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.error("Can not calculate file size");
                    fileSize = AbstractEntityFileStorage.MAXIMUM_FILE_SIZE_ALLOWED;
                }
            }

            size = instream.available() < size ? instream.available() : fileSize;
            instream = new BoundedInputStream(instream, size);
            LOGGER.debug("Available bytes " + instream.available());
            fileData = new FileData(fileInfo.getTags(), instream);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new FileStoreException("Could not read file " + srcFileName + " from "
                    + sourceDir, e);
        }

        LOGGER.debug("File found for name " + file.getAbsolutePath() + fileInfo.getTags());

        // fileData.setTotalContentLength(fileSize);
        if (size == -1) {
            fileData.setContentLength(fileSize);
        } else {

            fileData.setContentLength(size);
        }
        LOGGER.debug("FileData collected as " + fileData);

        return fileData;

    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {
        return null;
    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {

        return fwkId.toLowerCase() + com.vedantu.commons.utils.FileUtils.SEPARATOR_UNDERSCORE
                + entityType._getStorageId().toLowerCase();
    }

    public static void main(String args[]) {

        try {

            LocalFileSystemHandler handler = new LocalFileSystemHandler(false,
                    FileUtils.getTempDirectoryPath());
            System.out.println(FileUtils.getTempDirectoryPath());
            handler.createParent("documents");
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("id", "myphoto");
            tags.put("date", "jan25");
            handler.store(new File("/home/vikram/Documents/1.jpg"), "documents", "abcd.jpeg", tags);
            FileData data = handler.get("documents", "abcd.jpeg");
            System.out.println(data.getFileMetaInfo());
            handler.delete("documents", "abcd.jpeg");
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    @Override
    public SignUploadFileRes signContentUpload(EntityType type, String bucketName, String fileName,
                                               String contentType) {

        LOGGER.debug(" Calculated ContentType " + contentType);

        String domain = Play.application().configuration().getString(Configurations.APP_HOST);
        String port = Integer.toString(Play.application().configuration()
                .getInt(Configurations.APP_PORT));

        String url = "http" + "://" + domain + ":" + port
                + com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH + "cmdsResources"
                + com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH + "upload";

        SignUploadFileRes response = new SignUploadFileRes();
        response.contentType = contentType;
        response.requestParams.put("key", fileName);
        response.requestParams.put("entityType", type.name());
        response.url = url;
        response.verificationRequired = false;
        return response;

    }

    @Override
    public boolean exists(String dir, String fileName) throws FileStoreException {

        File testFile = new File(baseDirectory + File.separator + dir + File.separator + fileName);
        return testFile.exists();

    }

    @Override

    public StorageIdentification getIdentification() throws FileStoreException {

        return StorageIdentification.LOCAL;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws FileStoreException {

        File file = new File(baseDirectory + File.separator + sourceDir + File.separator
                + srcFileName);
        LOGGER.debug("Getting size for file" + file.getAbsolutePath());
        return file.length();
    }


    @Override
    public FileData getSecureURL(EntityType eType, MediaType mediaType, String fileName)
            throws FileStoreException {

        FileData data = new FileData();
        data.setContentLength(size(EntityStorageFactory.INSTANCE.get(eType).getStorageId(),
                fileName));

        List<String> pathComponents = new ArrayList<String>();
        pathComponents.add(eType.name().toLowerCase());
        pathComponents.add(mediaType.getAcronym().toLowerCase());
        pathComponents.add(fileName);
        String securedURL = ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URLsto
                + StringUtils.join(pathComponents,
                com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH);

        data.setSecuredURL(securedURL);
        return data;

    }*/

}

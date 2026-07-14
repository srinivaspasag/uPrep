package com.vedantu.ext.cmds.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalFileSystemHandler extends AbstractManager {

    //
    // public static final String PATH_SEPARATOR = File.pathSeparator;
    // public static final String FILE_EXTENSION_DOT = ".";
    //
    // String baseDirectory = null;
    // public boolean metadataNeeded = false;
    //
    public LocalFileSystemHandler(String baseDirectory) {

    }

    
    
    //
    public boolean copy(String srcDir, String destDir, String srcFileName, String destFileName) {

        File destinationDirectory = new File(destDir);
        if(! destinationDirectory.exists()){
            destinationDirectory.mkdirs();
        }
        File srcFile = new File(srcDir + File.separator + srcFileName);
        File destFile = new File(destDir + File.separator + destFileName);
        LOGGER.debug("SrcFile"+ srcFile.getAbsolutePath() + " "+srcFile.exists() + " Dest dir "+ destFile.getAbsolutePath()+" "+ destFile.exists());
        if (!srcFile.exists()) {
            LOGGER.error("SrcFile " + srcFile.getAbsolutePath() + " not exists");
            return false;
        }
        if(destFile.exists()) {
            LOGGER.debug("DestFile " + destFile.getAbsolutePath() + " already exists, hence skipping");
            return true;
        }
        try {
            LOGGER.debug("Copying file from " + srcFile.getAbsolutePath() + " to  " + destFile.getAbsolutePath());

            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(srcFile);
                os = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                os.close();
            }

        } catch (IOException e) {
            LOGGER.error("Can not copy", e);

        }
        return true;
    }

    public File getFile(String directory, String fileName) {

        return new File(directory + File.separator + fileName);
    }
    //
    // @Override
    // public boolean move(String srcDir, String destDir, String srcFileName, String destFileName)
    // throws FileStoreException {
    //
    // File srcFiFileUtils utils = new FileUtils();
    // utils.cle = new File(baseDirectory + File.separator + srcDir + File.separator
    // + srcFileName);
    // File destFile = new File(baseDirectory + File.separator + destDir + File.separator
    // + destFileName);
    // FileMetaInfo srcfileInfo = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
    // if (srcfileInfo == null && metadataNeeded) {
    // LOGGER.debug("File not found for name " + srcFileName);
    // throw new FileStoreException("File metadata not found", new FileNotFoundException());
    // }
    //
    // if (metadataNeeded) {
    // FileMetaInfo destFileInfo = new FileMetaInfo(destFileName);
    // destFileInfo.setTags(srcfileInfo.getTags());
    // FileMetaInfoDAO.INSTANCE.save(destFileInfo);
    // }
    // try {
    // FileUtils.moveFile(srcFile, destFile);
    // } catch (IOException e) {
    // // ODO Auto-generated catch block
    // throw new FileStoreException("Could not move file " + srcFile.getPath() + " to  "
    // + destFile.getPath(), e);
    // }
    // if (metadataNeeded) {
    // FileMetaInfoDAO.INSTANCE.delete(srcfileInfo);
    // }
    // return true;
    // }
    //
    // @Override
    // public FileData get(String sourceDir, String srcFileName, long index, long size)
    // throws FileStoreException {
    //
    // // TODO Auto-generated method stub
    // LOGGER.debug("Getting chunk of size :" + size + " of file" + srcFileName
    // + "  from directory " + sourceDir + " from byte :" + index);
    //
    // FileMetaInfo fileInfo = FileMetaInfoDAO.INSTANCE.findByFileId(srcFileName);
    //
    // if (fileInfo == null) {
    // LOGGER.debug("File metadata not found for name " + srcFileName);
    // throw new FileStoreException("File metadata not found", new FileNotFoundException());
    // }
    //
    // File file = new File(baseDirectory + File.separator + sourceDir + File.separator
    // + srcFileName);
    //
    // InputStream instream = null;
    //
    // FileData fileData = null;
    // long fileSize = metadataNeeded ? fileInfo.getSize() : 0;
    // try {
    // // instream = new FileInputStream(file);
    // instream = new FileInputStream(file);
    //
    // if (index != 0) {
    // instream.skip(index);
    // }
    //
    // if (fileSize == -1) {
    // try {
    // fileSize = instream.available();
    // fileInfo.setSize(fileSize);
    // if (metadataNeeded) {
    // FileMetaInfoDAO.INSTANCE.save(fileInfo);
    // }
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // LOGGER.error("Can not calculate file size");
    // fileSize = AbstractEntityFileStorage.MAXIMUM_FILE_SIZE_ALLOWED;
    // }
    // }
    //
    // size = instream.available() < size ? instream.available() : size;
    // instream = new BoundedInputStream(instream, size);
    // LOGGER.debug("Available bytes" + instream.available());
    // fileData = new FileData(fileInfo.getTags(), instream);
    //
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // throw new FileStoreException("Could not read file " + srcFileName + " from "
    // + sourceDir, e);
    // }
    //
    // LOGGER.debug("File found for name " + file.getAbsolutePath() + fileInfo.getTags());
    //
    // // fileData.setTotalContentLength(fileSize);
    // if (size == -1) {
    // fileData.setContentLength(fileSize);
    // } else {
    //
    // fileData.setContentLength(size);
    // }
    // LOGGER.debug("FileData collected as " + fileData);
    //
    // return fileData;
    //
    // }
    //
    // @Override
    // public String getParentName(EntityType entityType, String fwkId) {
    //
    // return fwkId.toLowerCase() + com.vedantu.commons.utils.FileUtils.SEPARATOR_UNDERSCORE
    // + entityType._getStorageId().toLowerCase();
    // }
    //
    // public static void main(String args[]) {
    //
    // try {
    //
    // LocalFileSystemHandler handler = new LocalFileSystemHandler(false,
    // FileUtils.getTempDirectoryPath());
    // System.out.println(FileUtils.getTempDirectoryPath());
    // handler.createParent("documents");
    // Map<String, String> tags = new HashMap<String, String>();
    // tags.put("id", "myphoto");
    // tags.put("date", "jan25");
    // handler.store(new File("/home/vikram/Documents/1.jpg"), "documents", "abcd.jpeg", tags);
    // FileData data = handler.get("documents", "abcd.jpeg");
    // System.out.println(data.getFileMetaInfo());
    // handler.delete("documents", "abcd.jpeg");
    // } catch (Exception exp) {
    // exp.printStackTrace();
    // }
    //
    // }
    //
    // @Override
    // public SignUploadFileRes signContentUpload(EntityType type, String bucketName, String
    // fileName,
    // String contentType) {
    //
    // LOGGER.debug(" Calculated ContentType " + contentType);
    //
    // String domain = Play.application().configuration().getString(Configurations.APP_HOST);
    // String port = Integer.toString(Play.application().configuration()
    // .getInt(Configurations.APP_PORT));
    //
    // String url = "http" + "://" + domain + ":" + port
    // + com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH + "cmdsResources"
    // + com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH + "upload";
    //
    // SignUploadFileRes response = new SignUploadFileRes();
    // response.contentType = contentType;
    // response.requestParams.put("key", fileName);
    // response.requestParams.put("entityType", type.name());
    // response.url = url;
    // response.verificationRequired = false;
    // return response;
    //
    // }
    //
    // @Override
    // public boolean exists(String dir, String fileName) throws FileStoreException {
    //
    // File testFile = new File(baseDirectory + File.separator + dir + File.separator + fileName);
    // return testFile.exists();
    //
    // }
    //
    // @Override
    // public StorageIdentification getIdentification() throws FileStoreException {
    //
    // return StorageIdentification.LOCAL;
    // }
    //
    // @Override
    // public long size(String sourceDir, String srcFileName) throws FileStoreException {
    //
    // File file = new File(baseDirectory + File.separator + sourceDir + File.separator
    // + srcFileName);
    // LOGGER.debug("Getting size for file" + file.getAbsolutePath());
    // return file.length();
    // }
    //
    // @Override
    // public FileData getSecureURL(EntityType eType, String fileName) throws FileStoreException {
    //
    // FileData data = new FileData();
    // data.setContentLength(size(EntityStorageFactory.INSTANCE.get(eType).getStorageId(),
    // fileName));
    // MediaType mediaType = MediaTypeMapper.INSTANCE().getMediaType(
    // com.vedantu.commons.utils.FileUtils.getExtensionWithoutDOT(fileName));
    //
    // List<String> pathComponents = new ArrayList<String>();
    // pathComponents.add(eType.name().toLowerCase());
    // pathComponents.add(mediaType.name().toLowerCase());
    // pathComponents.add(fileName);
    // String securedURL = ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL
    // + StringUtils.join(pathComponents,
    // com.vedantu.commons.utils.FileUtils.SEPARATOR_FWDSLASH);
    //
    // data.setSecuredURL(securedURL);
    // return data;
    //
    // }
}

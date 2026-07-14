package com.lms.common.vedantu.entity.storage;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.fs.handlers.S3Handler;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageFilter;
import com.lms.common.utils.ImageUtils;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.ranges.Range;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractEntityFileStorage implements IEntityFileStorage {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEntityFileStorage.class);

    public static final int MAXIMUM_FILE_SIZE_ALLOWED = 1000000000;

    protected EntityType entityType = null;
    protected String folderId = null;
    @Value("${fwkId}")
    private String fwkId;
    @Autowired
    private LocalFileSystemHandler localFileSystemHandler;
    @Autowired
    private S3Handler s3Handler;
    @Value("${fs.class}")
    private String fsClasss;
    public static String FILE_CLASS = "S3Handler";

    @Autowired
    private Environment env;

    @Override
    public StorageResult storeImage(final String uid, final File file,
                                    final FileCategory fileCategory, final ImageSize imageSize,
                                    final Map<String, String> tags) throws VedantuException {

        if (uid.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "cannot store file with null uid");
        }
        if (null == file) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, "cannot store null file");
        }
        if (null == fileCategory) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"cannot store file for null fileCategory");
        }

        final String fileId = computeFileId(uid, file, MediaType.IMAGE, fileCategory, imageSize);
        boolean isStored = storeInFS(file, fileId, tags);
        String displayUrlComponent = isStored ? computeDisplayUrlComponent(uid, file,
                MediaType.IMAGE, fileCategory, imageSize) : null;
        return new StorageResult(uid, folderId, fileId, isStored, displayUrlComponent);
    }



    @Override
    public FileData getData(String entityType, String mediaType, String fileName) throws VedantuException {
        return null;
    }

    @Override
    public FileData getData(String entityType, String mediaType, String fileName, Range ranges) throws VedantuException {
        return null;
    }

    @Override
    public String getStorageId() {
        return null;
    }

    @Override
    public boolean doesFileExist(String entityType, String mediaType, String fileName) {
        return false;
    }

    @Override
    public boolean remove(String entityType, String mediaType, String fileName) {
        return false;
    }

    @Override
    public long size(String uid, EntityType type, String fileExt, MediaType mediaType, FileCategory fileCategory, ImageSize imageSize) {
        return 0;
    }

    @Override
    public String computeDisplayS3UrlComponent(String uid, String webmExtentionWithoutDot, MediaType video, FileCategory converted) {
        return null;
    }

    public void AbstractEntityFileStorageEntity(EntityType entityType) {

        this.entityType = entityType;
        String frameworkId = fwkId;
        if ("S3Handler".equalsIgnoreCase(FILE_CLASS)) {
            this.folderId = s3Handler.getParentName(entityType, frameworkId);
        } else {
            this.folderId = localFileSystemHandler.getParentName(entityType, frameworkId);

        }

    }

    public StorageResult store(final String uid, final File file, final MediaType mediaType,
                               final FileCategory fileCategory, final Map<String, String> tags)
            throws VedantuException {

        if (StringUtils.isEmpty(uid)) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"cannot store file with null uid");
        }
        if (null == file) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"cannot store null file");
        }
        if (null == mediaType || MediaType.IMAGE == mediaType) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"api not supported for mediaType : " + mediaType);
        }
        if (null == fileCategory) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE,"cannot store file for null fileCategory");
        }

        final String fileId = computeFileId(uid, file, mediaType, fileCategory, null);
        boolean isStored = storeInFS(file, fileId, tags);
        String displayUrlComponent = isStored ? computeDisplayUrlComponent(uid, file, mediaType,
                fileCategory, null) : null;
        return new StorageResult(uid, folderId, fileId, isStored, displayUrlComponent);
    }

    public boolean storeInFS(final File file, final String fileId, final Map<String, String> tags)
            throws VedantuException {

        try {
            if (fsClasss.equalsIgnoreCase(FILE_CLASS)) {
                return s3Handler.store(file, folderId, fileId,
                        (null != tags ? tags : new HashMap<String, String>()));
            } else {
                localFileSystemHandler.localFileSystemHandlerBaseDirectory(true);
                return localFileSystemHandler.store(file, folderId, fileId,
                        (null != tags ? tags : new HashMap<String, String>()));
            }


        } catch (Exception e) {
            throw new VedantuException(VedantuErrorCode.INVALID_CODE, e);
        }
    }

    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        String name = convFile.getName();
        return convFile;
    }

    public File createImage(File image, ImageSize size, String fileName)
            throws Exception {
        logger.info("createImage image: " + image + ", size: " + size
                + ", fileName: " + fileName);

        ImageFilter imageFilter = new ImageFilter();
        boolean isImage = imageFilter.accept(new File(fileName));
        logger.debug("createImage isImage: " + isImage);

        if (!isImage) {
            return null;
        }

		logger.debug("Creating " + size.name() + " for imgFile "
				+ image.getAbsolutePath() + " as "
				+ getWidthPropertyValue(size)  + " as width "
				+ getHeightPropertyValue(size) + " & height: "
				);
        String imageType = imageFilter.getImageType();
        logger.debug("createImage imageType: " + imageType);

        String imageName = fileName;
        logger.debug("createImage imageName: " + imageName);

        imageName = "";//StringUtils.substring(imageName,StringUtils.indexOf(imageName, "."));
        logger.info("Uploaded image name is : " + imageName);
        logger.info("file extention is " + imageType);

        //LocalFileSystemHandler tempFileSystemHandler = FileSystemFactory.INSTANCE
        //	.getTempFS();
        localFileSystemHandler.localFileSystemHandlerTempDirectory(false);
        File generatedFile = localFileSystemHandler.getNewFile("ImageTemp",
                FileUtils.JPG_EXTENTION_WITHOUT_DOT);

        logger.debug("Creating " + size.name() + " for imgFile "
                + image.getAbsolutePath() + " as "
                + generatedFile.getAbsolutePath() + " as width "
                + getWidthPropertyValue(size) + " & height: "
                + getHeightPropertyValue(size));

        if (size == ImageSize.ORIGINAL) {

            ImageUtils.createThumbnail(image.getAbsolutePath(),
                    generatedFile.getAbsolutePath());
        } else {

            ImageUtils.createThumbnail(image.getAbsolutePath(),
                    generatedFile.getAbsolutePath(),
                    getWidthPropertyValue(size), 0);

        }

        return generatedFile;
    }

    public int getWidthPropertyValue(ImageSize imageSize) {
        String size = env.getProperty("image" + "." + imageSize.getAcronym() + "." + "width");
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    public int getHeightPropertyValue(ImageSize imageSize) {
        String size = env.getProperty("image" + "." + imageSize.getAcronym() + "." + "height");
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return 0;
        }


    }

    /* public FileData getFromFs(String folderId, String fileId) throws EntityFileStorageException {

         try {
             FileData fData = FileSystemFactory.INSTANCE.getFS().get(folderId, fileId);
             Logger.info("sucessfully loaded fileId: " + fileId);
             return fData;
         } catch (FileStoreException e) {
             throw new EntityFileStorageException(e);
         }
     }

     private String computeFileId(final String uid, final File file, final MediaType mediaType,
                                  final FileCategory fileCategory, final ImageSize imageSize) {

         final String fileExt = StringUtils.substringAfterLast(file.getName(),
                 FileUtils.SEPARATOR_DOT);
         return computeFileId(uid, this.getEntityType(), fileExt, mediaType, fileCategory, imageSize);
     }

     public static String computeFileId(final String uid, final EntityType type,
                                        final String fileExt, final MediaType mediaType, final FileCategory fileCategory,
                                        final ImageSize imageSize) {

         List<String> fileIdComponents = new ArrayList<String>();
         fileIdComponents.add(uid);
         fileIdComponents.add(type.getAcronym());
         fileIdComponents.add(mediaType.getAcronym());
         if (FileCategory.UNSPECIFIED != fileCategory) {
             fileIdComponents.add(fileCategory.getAcronym());
         }
         if (MediaType.IMAGE == mediaType && null != imageSize) {
             fileIdComponents.add(imageSize.getAcronym());
         }
         fileIdComponents.add(fileExt);
         return StringUtils.join(fileIdComponents, FileUtils.SEPARATOR_DOT);
     }

     private String computeDisplayUrlComponent(final String uid, final File file,
                                               final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

         String fileExt = StringUtils.substringAfterLast(file.getName(), FileUtils.SEPARATOR_DOT);
         return computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, imageSize);
     }
 =======
 >>>>>>> 3b8ed5a6296b31287216cae2a445033de7816a49

     /* public FileData getFromFs(String folderId, String fileId) throws EntityFileStorageException {

          try {
              FileData fData = FileSystemFactory.INSTANCE.getFS().get(folderId, fileId);
              Logger.info("sucessfully loaded fileId: " + fileId);
              return fData;
          } catch (FileStoreException e) {
              throw new EntityFileStorageException(e);
          }
      }

      private String computeFileId(final String uid, final File file, final MediaType mediaType,
                                   final FileCategory fileCategory, final ImageSize imageSize) {

          final String fileExt = StringUtils.substringAfterLast(file.getName(),
                  FileUtils.SEPARATOR_DOT);
          return computeFileId(uid, this.getEntityType(), fileExt, mediaType, fileCategory, imageSize);
      }

      public static String computeFileId(final String uid, final EntityType type,
                                         final String fileExt, final MediaType mediaType, final FileCategory fileCategory,
                                         final ImageSize imageSize) {

          List<String> fileIdComponents = new ArrayList<String>();
          fileIdComponents.add(uid);
          fileIdComponents.add(type.getAcronym());
          fileIdComponents.add(mediaType.getAcronym());
          if (FileCategory.UNSPECIFIED != fileCategory) {
              fileIdComponents.add(fileCategory.getAcronym());
          }
          if (MediaType.IMAGE == mediaType && null != imageSize) {
              fileIdComponents.add(imageSize.getAcronym());
          }
          fileIdComponents.add(fileExt);
          return StringUtils.join(fileIdComponents, FileUtils.SEPARATOR_DOT);
      }

      private String computeDisplayUrlComponent(final String uid, final File file,
                                                final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

          String fileExt = StringUtils.substringAfterLast(file.getName(), FileUtils.SEPARATOR_DOT);
          return computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, imageSize);
      }

      /**
       * This will compute url like following /[document video diagram ]/[vid img doc]/uid.[img vid
       * doc dia ].[thumb orig conv ].[ xsmall small medium large ].{file extension}
       *
       * @param uid
       * @param fileExt
       * @param mediaType
       * @param fileCategory
       * @param imageSize
       * @return
       */
    private String computeDisplayUrlComponent(final String uid, final File file,
                                              final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

        String fileExt = file.getName().substring(file.getName().indexOf(".")+1,file.getName().length()-1);
        //StringUtils.substringAfterLast(file.getName(), FileUtils.SEPARATOR_DOT);

        return computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, imageSize);
    }
@Override
   public String computeDisplayUrlComponent(final String uid, final String fileExt,final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

        List<String> pathComponents = new ArrayList<String>();
        pathComponents.add(entityType.name().toLowerCase());
        pathComponents.add(mediaType.getAcronym());

        List<String> fileIdComponents = new ArrayList<String>();
        fileIdComponents.add(uid);
        fileIdComponents.add(entityType.getAcronym());
        fileIdComponents.add(mediaType.getAcronym());
        if (FileCategory.UNSPECIFIED != fileCategory) {
            fileIdComponents.add(fileCategory.getAcronym());
        }

        if (MediaType.IMAGE == mediaType && null != imageSize) {
            fileIdComponents.add(imageSize.getAcronym());
        }
        fileIdComponents.add(fileExt);

        pathComponents.add(fileIdComponents.stream().collect(Collectors.joining(FileUtils.SEPARATOR_DOT)));

        return pathComponents.stream().collect(Collectors.joining(FileUtils.SEPARATOR_FWDSLASH));
    }

  /*  public String computeDisplayS3UrlComponent(final String uid, final String fileExt,
                                               final MediaType mediaType, final FileCategory fileCategory) {

        List<String> fileIdComponents = new ArrayList<String>();
        fileIdComponents.add(uid);
        fileIdComponents.add(entityType.getAcronym());
        fileIdComponents.add(mediaType.getAcronym());
        if (FileCategory.UNSPECIFIED != fileCategory) {
            fileIdComponents.add(fileCategory.getAcronym());
        }
        fileIdComponents.add(fileExt);
        return StringUtils.join(fileIdComponents, FileUtils.SEPARATOR_DOT);
    }

    // public static String getFileName(String uid,EntityType entityType, MediaType mediaType,
    // FileCategory fileCategory,
    // ImageSize imageSize, String fileExt) {
    //
    // List<String> fileIdComponents = new ArrayList<String>();
    // fileIdComponents.add(uid);
    // fileIdComponents.add(entityType.getAcronym());
    // fileIdComponents.add(mediaType.getAcronym());
    // if (FileCategory.UNSPECIFIED != fileCategory) {
    // fileIdComponents.add(fileCategory.getAcronym());
    // }
    //
    // if (MediaType.IMAGE == mediaType && null != imageSize) {
    // fileIdComponents.add(imageSize.getAcronym());
    // }
    // fileIdComponents.add(fileExt);
    //
    // return StringUtils.join(fileIdComponents, FileUtils.SEPARATOR_DOT);
    // }




    public StorageResult storeVideo(final String uid, final File file,
                                    final FileCategory fileCategory, final Map<String, String> tags, MediaType mediaType)
            throws EntityFileStorageException {

        if (StringUtils.isEmpty(uid)) {
            throw new EntityFileStorageException("cannot store file with null uid");
        }
        if (null == file) {
            throw new EntityFileStorageException("cannot store null file");
        }
        if (null == fileCategory) {
            throw new EntityFileStorageException("cannot store file for null fileCategory");
        }

        final String fileId = computeFileId(uid, file, mediaType, fileCategory, null);
        boolean isStored = storeInFS(file, fileId, tags);
        String displayUrlComponent = isStored ? computeDisplayUrlComponent(uid, file, mediaType,
                fileCategory, null) : null;
        return new StorageResult(uid, folderId, fileId, isStored, displayUrlComponent);
    }

    protected String getDisplayUrlComponent(final String uid, final String fileExt,
                                            final String mediaTypeAcronym, final String fileCategoryAcronym,
                                            final String imageSizeAcronym) throws EntityFileStorageException {

        if (StringUtils.isEmpty(uid)) {
            throw new EntityFileStorageException("cannot get display-url for null uid");
        }
        if (StringUtils.isEmpty(fileExt)) {
            throw new EntityFileStorageException("cannot get display-url for null/empty fileExt");
        }
        MediaType mediaType = MediaType.getByAcronym(mediaTypeAcronym);
        if (null == mediaType) {
            throw new EntityFileStorageException(
                    "cannot get display-url for null mediaType for mediaTypeAcronym : "
                            + mediaTypeAcronym);
        }
        FileCategory fileCategory = FileCategory.getByAcronym(fileCategoryAcronym);
        if (null == fileCategory) {
            throw new EntityFileStorageException(
                    "cannot get display-url for null fileCategory for fileCategoryAcronym : "
                            + fileCategoryAcronym);
        }
        ImageSize imageSize = ImageSize.getByAcronym(imageSizeAcronym);
        if (MediaType.IMAGE == mediaType && null == imageSize) {
            throw new EntityFileStorageException(
                    "cannot get display-url for null imageSize for imageSizeAcronym : "
                            + imageSizeAcronym);
        }
        return computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, imageSize);
    }

    private static final int MIN_FILENAME_TOKENS = 3;
    private static final int MAX_FILENAME_TOKENS = 6;

    public FileData getData(final String entityTypeName, final String mediaTypeAcronym,
                            final String fileName) throws EntityFileStorageException {

        return getData(entityTypeName, mediaTypeAcronym, fileName, null);
    }

    public FileData getData(final String entityTypeName, final String mediaTypeAcronym,
                            final String fileName, Range range) throws EntityFileStorageException {

        LOGGER.info("fetching entittype: " + entityTypeName + "mediaTypeAcronym "
                + mediaTypeAcronym + " fileName " + fileName + " Range " + range);
        if (StringUtils.isEmpty(entityTypeName)) {
            LOGGER.error("Empty entityType Name ");
            throw new EntityFileStorageException("cannot get file-data for null entityTypeName");
        }
        EntityType entityType = EntityType.valueOfKey(entityTypeName.toUpperCase());
        if (EntityType.UNKNOWN == entityType) {
            LOGGER.error("Unknown entityType ");
            throw new EntityFileStorageException(
                    "cannot get file-data for unknown entityType with entityTypeName : "
                            + entityTypeName);
        }
        if (this.entityType != entityType) {
            LOGGER.error("Incorrect storage as requested entity time is  " + entityTypeName
                    + " current storage entityType" + this.entityType);
            throw new EntityFileStorageException(
                    "cannot get file-data for entityType mismatch, expected[" + this.entityType
                            + "] found [" + entityType + "]");
        }
        MediaType mediaType = MediaType.getByAcronym(mediaTypeAcronym);
        if (null == mediaType) {
            LOGGER.error("cannot get file-data for null mediaType for mediaTypeAcronym : "
                    + mediaTypeAcronym);
            throw new EntityFileStorageException(
                    "cannot get file-data for null mediaType for mediaTypeAcronym : "
                            + mediaTypeAcronym);
        }
        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("cannot get file-data for null/empty fileName");
            throw new EntityFileStorageException("cannot get file-data for null/empty fileName");
        }

        String[] tokens = StringUtils.split(fileName, FileUtils.SEPARATOR_DOT);
        if (tokens.length < MIN_FILENAME_TOKENS || tokens.length > MAX_FILENAME_TOKENS) {
            throw new EntityFileStorageException(
                    "cannot get file-data with unexpected number of tokens [" + tokens.length
                            + "] in fileName : " + fileName);
        }
        int index = 0;
        String uid = tokens[index++];
        String entityTypeAcronym = tokens[index++];
        // entityType = EntityType.getByAcronym(entityTypeAcronym);
        // if (null == entityType) {
        // throw new EntityFileStorageException(
        // "cannot get file-data for null entityType for entityTypeAcronym : "
        // + entityTypeAcronym);
        // }
        // TODO we dont need this acronym check as two entities can belong to same acronym
        // if (this.entityType != entityType) {
        // throw new EntityFileStorageException(
        // "cannot get file-data for entityType mismatch, expected["
        // + this.entityType + "] found [" + entityType
        // + "] with entityTypeAcronym : " + entityTypeAcronym);
        // }
        String urlMediaTypeAcronym = tokens[index++];
        if (!mediaType.getAcronym().equals(urlMediaTypeAcronym)) {
            throw new EntityFileStorageException(
                    "cannot get file-data for media type mismatch, expected["
                            + mediaType.getAcronym() + "] found ["
                            + MediaType.getByAcronym(urlMediaTypeAcronym)
                            + "] with entityTypeAcronym : " + urlMediaTypeAcronym);
        }
        FileCategory fileCategory = FileCategory.UNSPECIFIED;
        ImageSize imageSize = null;
        if (tokens.length > MIN_FILENAME_TOKENS) {

            FileCategory tFileCategory = FileCategory.getByAcronym(tokens[index]);
            if (FileCategory.UNSPECIFIED != tFileCategory) {
                fileCategory = tFileCategory;
                index++;
            }
            if (MediaType.IMAGE == mediaType && index + 1 < MAX_FILENAME_TOKENS) {
                imageSize = ImageSize.getByAcronym(tokens[index++]);
            }
        }
        String fileExt = tokens[index];
        LOGGER.info("entityTypeName: " + entityTypeName + ", mediaTypeAcronym: " + mediaTypeAcronym
                + ", fileName: " + fileName + " ==> entityType: " + entityType + ", mediaType: "
                + mediaType + ", uid: " + uid + ", fileCategory: " + fileCategory + ", imageSize: "
                + imageSize + ", fileExt: " + fileExt);
        final String fileId = computeFileId(uid, this.getEntityType(), fileExt, mediaType,
                fileCategory, imageSize);
        LOGGER.info("loading fileId: " + fileId);
        return getFromFs(folderId, fileId, range);
    }

    protected FileData getFromFs(String folderId, String fileId, Range ranges)
            throws EntityFileStorageException {

        try {
            FileData fData = null;

            if (ranges == null) {
                fData = FileSystemFactory.INSTANCE.getFS().get(folderId, fileId);
            } else {
                long index = ranges.getIndex();
                long size = ranges.getSize();
                LOGGER.info("index===: " + index + "size:" + size);

                fData = FileSystemFactory.INSTANCE.getFS().get(folderId, fileId, index, size);
                if (ranges != null && size > fData.getFileSize()) {
                    ranges.setSize(fData.getFileSize());
                }
            }

            LOGGER.info("sucessfully loaded fileId: " + fileId + " content size available "
                    + fData.getIn().available());
            return fData;
        } catch (FileStoreException e) {
            throw new EntityFileStorageException(e);
        } catch (IOException e) {
            throw new EntityFileStorageException(e);
        }
    }

    public static String getUUIDFromFileName(String fileId) {

        return StringUtils.substringBefore(fileId, FileUtils.SEPARATOR_DOT);
    }



    public StorageResult copy(AbstractEntityFileStorage srcEntityStorage, String srcFileName,
                              String newFileName) throws EntityFileStorageException {

        if (StringUtils.isEmpty(newFileName) || StringUtils.isEmpty(srcFileName)) {
            throw new EntityFileStorageException("invalid fileNames src : " + srcFileName
                    + " dest " + newFileName);
        }
        try {
            if (srcEntityStorage.getFS() instanceof LocalFileSystemHandler
                    && this.getFS() instanceof ObjectStorageHandler) {
                LocalFileSystemHandler srcHandler = (LocalFileSystemHandler) srcEntityStorage
                        .getFS();
                srcHandler.getDirectory();
                String filepath = srcHandler.getFilePath(srcEntityStorage.folderId, srcFileName);
                File dataFile = new File(filepath);
                LOGGER.debug("Local file path" + dataFile.getAbsolutePath()
                        + "  new FileId willbe " + newFileName);
                FileData data = srcEntityStorage.getFromFs(srcEntityStorage.folderId, srcFileName);
                this.storeInFS(dataFile, newFileName, data.getFileMetaInfo());
            } else if (srcEntityStorage.getFS() instanceof S3Handler
                    && this.getFS() instanceof S3Handler) {
                this.getFS().copy(srcEntityStorage.folderId, this.folderId, srcFileName,
                        newFileName);

            } else if (srcEntityStorage.getFS() instanceof LocalFileSystemHandler
                    && this.getFS() instanceof LocalFileSystemHandler) {
                this.getFS().copy(srcEntityStorage.folderId, this.folderId, srcFileName,
                        newFileName);
            } else if (srcEntityStorage.getFS() instanceof ObjectStorageHandler
                    && this.getFS() instanceof ObjectStorageHandler) {
                this.getFS().copy(srcEntityStorage.folderId, this.folderId, srcFileName,
                        newFileName);
            }
            return new StorageResult(getUUIDFromFileName(newFileName), this.folderId, newFileName,
                    true, null);
        } catch (Exception exception) {
            LOGGER.debug("Can not move files");
            throw new EntityFileStorageException(exception);
        }
    }

    protected IFileSystemHandler getFS() throws EntityFileStorageException {

        return FileSystemFactory.INSTANCE.getFS();
    }

    @Override
    public String getStorageId() {

        return folderId;
    }

    public StorageIdentification getStorageIdentification() throws FileStoreException {

        return FileSystemFactory.INSTANCE.getFS().getIdentification();
    }

    @Override
    public boolean doesFileExist(String entityType, String mediaType, String fileName) {

        try {
            return FileSystemFactory.INSTANCE.getFS().exists(this.folderId, fileName);
        } catch (FileStoreException e) {
            return false;
        }
    }

    @Override
    public boolean remove(String entityType, String mediaType, String fileName) {

        try {
            return FileSystemFactory.INSTANCE.getFS().delete(this.folderId, fileName);
        } catch (FileStoreException e) {
            return false;
        }
    }

    @Override
    public long size(final String uid, final EntityType type, final String fileExt,
                     final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

        try {
            return FileSystemFactory.INSTANCE.getFS().size(this.folderId,
                    computeFileId(uid, type, fileExt, mediaType, fileCategory, imageSize));
        } catch (FileStoreException e) {
            return -1;
        }

    }*/

    @Override
    public FileData getSecuredURL(final String uid, final EntityType type, final String fileExt,
                                  final MediaType mediaType, final FileCategory fileCategory, final ImageSize imageSize) {

        try {
            if (fsClasss.equalsIgnoreCase(FILE_CLASS)) {
                return s3Handler.getSecureURL(folderId, type, mediaType,
                        computeFileId(uid, type, fileExt, mediaType, fileCategory, imageSize));
            } else {
                return localFileSystemHandler.getSecureURL(folderId, type, mediaType,
                        computeFileId(uid, type, fileExt, mediaType, fileCategory, imageSize));
            }


        } catch (VedantuException | FileStoreException e) {
            logger.error("Can not find files", e);
            return null;
        }
    }

    private String computeFileId(final String uid, final File file, final MediaType mediaType,
                                 final FileCategory fileCategory, final ImageSize imageSize) {

        final String[] fileExt1 = file.getPath().split("\\.");
        String fileExt = fileExt1[fileExt1.length - 1];
        return computeFileId(uid, this.getEntityType(), fileExt, mediaType, fileCategory, imageSize);
    }

    public static String computeFileId(final String uid, final EntityType type,
                                       final String fileExt, final MediaType mediaType, final FileCategory fileCategory,
                                       final ImageSize imageSize) {

        List<String> fileIdComponents = new ArrayList<String>();
        fileIdComponents.add(uid);
        fileIdComponents.add(type.getAcronym());
        fileIdComponents.add(mediaType.getAcronym());
        if (FileCategory.UNSPECIFIED != fileCategory) {
            fileIdComponents.add(fileCategory.getAcronym());
        }
        if (MediaType.IMAGE == mediaType && null != imageSize) {
            fileIdComponents.add(imageSize.getAcronym());
        }
        fileIdComponents.add(fileExt);
        return fileIdComponents.stream().collect(Collectors.joining(FileUtils.SEPARATOR_DOT));
    }
    public EntityType getEntityType() {

        return entityType;
    }

    public StorageResult storeVideo(final String uid, final File file,
                                    final FileCategory fileCategory, final Map<String, String> tags, MediaType mediaType)
            throws EntityFileStorageException {

        if (StringUtils.isEmpty(uid)) {
            throw new EntityFileStorageException("cannot store file with null uid");
        }
        if (null == file) {
            throw new EntityFileStorageException("cannot store null file");
        }
        if (null == fileCategory) {
            throw new EntityFileStorageException("cannot store file for null fileCategory");
        }

        final String fileId = computeFileId(uid, file, mediaType, fileCategory, null);
        boolean isStored = storeInFS(file, fileId, tags);
        String displayUrlComponent = isStored ? computeDisplayUrlComponent(uid, file, mediaType,
                fileCategory, null) : null;
        return new StorageResult(uid, folderId, fileId, isStored, displayUrlComponent);
    }

	@Override
	public FileData getSecureURL(EntityType eType, MediaType mediaType, String fileName) throws Exception {
		 try {
	            if (fsClasss.equalsIgnoreCase(FILE_CLASS)) {
	                return s3Handler.getSecureURL(folderId, eType, mediaType,fileName);
	            } else {
	                return localFileSystemHandler.getSecureURL(folderId, eType, mediaType,
	                		fileName);
	            }


	        } catch (VedantuException | FileStoreException e) {
	            logger.error("Can not find files", e);
	            throw e;
	        }
	}

}

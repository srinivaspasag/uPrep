package com.vedantu.commons.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.ning.http.util.Base64;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;

public class ImageDisplayURLUtil {

    private static final ALogger LOGGER                   = Logger.of(ImageDisplayURLUtil.class);
    public static final String   JPG_EXTENTION            = ".jpg";
 // Set fileSecurityEnabled to true if you want to enable security, I am disabling it because I want to stream directly from S3. If fileSecurity is enabled make sure viewer service is UP and Running
    private static boolean       videoFileSecurityEnabled      = false;
    private static boolean       docFileSecurityEnabled      = true;
    private static String        host                     = null;
    private static boolean       directFileServingEnabled = false;
    static {
        try {

            host = Play.application().configuration().getString("img.host");
//            fileSecurityEnabled = Play.application().configuration()
//                    .getBoolean("environment.file.security.enabled");
            directFileServingEnabled = Play.application().configuration()
                    .getBoolean("environment.file.direct.serving.enabled",Boolean.FALSE);
            LOGGER.info("Host loaded" + host);
            // IMG_HOST = IMG_HOST_PREFIX + host;

            // IMG_HOST = host;

        } catch (Exception e) {
            LOGGER.error("can not load property app.host for application run mode : "
                    + Play.application().configuration().getString("application.mode"));
        }
    }

    public static String getImgHost() {

        return Play.application().configuration().getString("img.host");
    }

    public static final String DEFAULT_FILE_SERVING_HOST_URL                  = getImgHost()
                                                                                      + "/viewer/view/";

    public static final String DEFAULT_FILE_STREAMING_HOST_URL                = getImgHost()
                                                                                      + "/viewer/stream/";

    public static final String DEFAULT_FILE_ESTREAMING_HOST_URL               = getImgHost()
                                                                                      + "/viewer/estream/";

    public static final String DEFAULT_FILE_DOWNLOAD_HOST_URL                 = getImgHost()
                                                                                      + "/viewer/download/";

    public static final String DEFAULT_FILE_SERVING_STATIC_URL                = getImgHost()
                                                                                      + "/static";

    public static final String DEFAULT_HOST_URL_PROFILE_PIC                   = getImgHost()
                                                                                      + "/profiles/pic/";
    public static final String DEFAULT_HOST_URL_PLAYLIST_IMAGES               = getImgHost()
                                                                                      + "/playlists/";
    public static final String DEFAULT_HOST_URL_QUESTION_EMBED_IMAGES         = getImgHost()
                                                                                      + "/questions/";
    public static final String DEFAULT_HOST_URL_QUESTION_EMBED_TEMP_IMAGES    = getImgHost()
                                                                                      + "/temp/questions/";

    public static final String DEFAULT_HOST_URL_SOLUTION_EMBED_IMAGES         = getImgHost()
                                                                                      + "/solutions/";
    public static final String DEFAULT_HOST_URL_SOLUTION_EMBED_TEMP_IMAGES    = getImgHost()
                                                                                      + "/temp/solutions/";

    public static final String DEFAULT_HOST_URL_STATUS_FEED_EMBED_TEMP_IMAGES = getImgHost()
                                                                                      + "/temp/statusfeed/";

    public static final String DEFAULT_HOST_URL_UPLOADED_FILE                 = getImgHost()
                                                                                      + "/Diagrams/get/";

    public static String getStatuFeedOrginalImageURL(String uuid) {

        return getEntityImageURL(EntityType.STATUSFEED, uuid, ImageSize.ORIGINAL);

    }

    public static File getEmbededFileName(String directory, String url) {

        Logger.debug("URL:" + url);
        Logger.debug("Directory:" + directory);

        int indexOfEmbededPart = url.indexOf(DEFAULT_HOST_URL_STATUS_FEED_EMBED_TEMP_IMAGES);
        if (indexOfEmbededPart != -1) {
            String fileName = url.substring(indexOfEmbededPart
                    + DEFAULT_HOST_URL_STATUS_FEED_EMBED_TEMP_IMAGES.length());
            return new File(directory + "/" + fileName);
        }
        return null;

    }

    public static String getOrganizationThumbnail(String thumbnailId) {

        return getEntityImageURL(EntityType.ORGANIZATION, thumbnailId, ImageSize.EXTRA_SMALL);

    }

    public static String getUserPicThumbnail(String profilePic) {

        return getEntityImageURL(EntityType.USER, profilePic, ImageSize.SMALL);
    }

    public static String getEntityThumbnail(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.SMALL);
    }

    public static String getEntityPoster(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.MEDIUM);
    }

    public static String getEntityStaticThumbnail(EntityType entityType,
            List<String> suffixComponents) {

        final String fileName = StringUtils.lowerCase(StringUtils.join(suffixComponents, "-")
                + JPG_EXTENTION);
        return StringUtils.join(Arrays.asList(DEFAULT_FILE_SERVING_STATIC_URL, entityType.name()
                .toLowerCase(), fileName), "/");
    }

    public static String getEntityImageURL(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.ORIGINAL);
    }

    public static String getEntityImageURL(EntityType entityType, String uid, ImageSize size) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        if( directFileServingEnabled){
            return fileEntityStorage.getSecuredURL(uid,entityType,
                        FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                        FileCategory.CONVERTED, size).getSecuredURL();
        }


        return DEFAULT_FILE_SERVING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid,
                        FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                        FileCategory.CONVERTED, size);
    }

    public static String getEntityVideoURL(EntityType entityType, String uid) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return DEFAULT_FILE_STREAMING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid,
                        FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                        FileCategory.CONVERTED, null);
    }

    public static String getEntityVideoSecureURL(EntityType entityType, String uid,
            Map<String, String> sessionParamsMap, boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);

        String componentUrl = fileEntityStorage
                .computeDisplayUrlComponent(uid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                        MediaType.VIDEO, FileCategory.CONVERTED, null);
        return isWebReq && videoFileSecurityEnabled ? (DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(
                componentUrl, sessionParamsMap))
                : (DEFAULT_FILE_STREAMING_HOST_URL + componentUrl);
    }

    public static String getEntityVideoURL(EntityType entityType, String uid, String fileExt,
            FileCategory fileCategory) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);

        return DEFAULT_FILE_STREAMING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, MediaType.VIDEO,
                        fileCategory, null);
    }

    public static String getEntityVideoSecureURL(EntityType entityType, String uid, String fileExt,
            FileCategory fileCategory, Map<String, String> sessionParamsMap, boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        String componentUrl = fileEntityStorage.computeDisplayUrlComponent(uid, fileExt,
                MediaType.VIDEO, fileCategory, null);

        return isWebReq && videoFileSecurityEnabled ? (DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(
                componentUrl, sessionParamsMap)) : (DEFAULT_FILE_STREAMING_HOST_URL + componentUrl);
    }

    public static String getEntityVideoS3URL(EntityType entityType, String uid, String fileExt,
            FileCategory fileCategory) {

        IEntityFileStorage storage = EntityStorageFactory.INSTANCE.get(entityType);
        String componentUrl = storage.computeDisplayS3UrlComponent(uid, fileExt,
                MediaType.VIDEO, fileCategory);

        return ("https://"+storage.getStorageId()+".s3.amazonaws.com/"+ componentUrl);
    }

    public static String getEntityDocumentURL(EntityType entityType, String uid) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return DEFAULT_FILE_STREAMING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid,
                        FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.CONVERTED, null);
    }

    public static String getEntityDocumentSecureURL(EntityType entityType, String uid,
            Map<String, String> sessionParamsMap, boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);

        String componentUrl = fileEntityStorage.computeDisplayUrlComponent(uid,
                FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC, FileCategory.CONVERTED, null);

        return isWebReq && docFileSecurityEnabled ? (DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(
                componentUrl, sessionParamsMap))
                : (DEFAULT_FILE_STREAMING_HOST_URL + fileEntityStorage.computeDisplayUrlComponent(
                        uid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                        FileCategory.CONVERTED, null));

    }

    public static String getEntityDocumentURL(EntityType entityType, String uid, String fileExt,
            FileCategory fileCategory) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return DEFAULT_FILE_STREAMING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, MediaType.DOC,
                        fileCategory, null);
    }

    public static String getEntityDocumentSecureURL(EntityType entityType, String uid,
            String fileExt, FileCategory fileCategory, Map<String, String> sessionParamsMap,
            boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);

        String componentUrl = fileEntityStorage.computeDisplayUrlComponent(uid, fileExt,
                MediaType.DOC, fileCategory, null);

        return isWebReq && docFileSecurityEnabled ? (DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(
                componentUrl, sessionParamsMap)) : (DEFAULT_FILE_STREAMING_HOST_URL + componentUrl);
    }

    public static String getEntityFileURL(EntityType entityType, String uid, String fileExt,
            FileCategory fileCategory) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return DEFAULT_FILE_SERVING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, MediaType.FILE,
                        fileCategory, null);
    }

    public static String getEntityDownloadURL(EntityType entityType, String uid, String fileExt,
            MediaType mediaType, FileCategory fileCategory) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return (entityType == EntityType.VIDEO ? DEFAULT_FILE_DOWNLOAD_HOST_URL:DEFAULT_FILE_STREAMING_HOST_URL)
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, mediaType,
                        fileCategory, null);
    }

    public static String getEntityDownloadURL(EntityType entityType, String uid, String fileExt,
            MediaType mediaType, FileCategory fileCategory, String id) {

        IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        return DEFAULT_FILE_DOWNLOAD_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, mediaType,
                        fileCategory, null) + "/" + id;
    }

    public static String getPlaylistImageURL(String uuid) {

        return getEntityImageURL(EntityType.PLAYLIST, uuid, ImageSize.ORIGINAL);
    }

    public static String getPlaylistImageThumbnailURL(String uuid) {

        return getEntityThumbnail(EntityType.PLAYLIST, uuid);

    }

    public static String getQRQuestionEmbedImage(String imageName, String questionSetId) {

        return DEFAULT_HOST_URL_QUESTION_EMBED_IMAGES + questionSetId + File.separator + "img"
                + File.separator + imageName;
    }

    public static String getQuestionEmbedImageURL(String imageName) {

        return DEFAULT_HOST_URL_QUESTION_EMBED_IMAGES + imageName;
    }

    public static String getQuestionEmbedTempImageURL(String imageName) {

        return DEFAULT_HOST_URL_QUESTION_EMBED_TEMP_IMAGES + imageName;
    }

    public static String getSolutionEmbedImageURL(String imageName) {

        return DEFAULT_HOST_URL_SOLUTION_EMBED_IMAGES + imageName;
    }

    //
    public static String getSolutionEmbedTempImageURL(String imageName) {

        return DEFAULT_HOST_URL_SOLUTION_EMBED_TEMP_IMAGES + imageName;
    }

    public static String getStatusFeedTempImageURL(String imageName) {

        return DEFAULT_HOST_URL_STATUS_FEED_EMBED_TEMP_IMAGES + imageName;
    }

    public static String getTempImageURL(String image) {

        return getImgHost() + "/temp/images/" + image;
    }

    public static String getGlobalTypeImageFormat(String content, String uuid) {

        // chekcking it already processed or content is empty
        if (StringUtils.isEmpty(content) || content.contains("v-uid=\"" + uuid)) {
            return content;
        }
        // replacing all uuids with embeded html

        return content.replaceAll(uuid, "<img v-uid=\"" + uuid
                + "\" src=\"\" class=\"vUrl\" v-perm=\"true\"/>");
    }

    public static String getEmbededHtml(String url, String uuid) {

        return "<img  src=\"" + url + "\" v-uid=\"" + uuid + "\"/>";
    }

    private static String getEncryptedEntityUrl(String urlComponent,
            Map<String, String> sessionParamsMap) {

        if (sessionParamsMap == null) {
            LOGGER.error("null sessionParamsMap from webRequest");
            return StringUtils.EMPTY;
        }
        String userId = sessionParamsMap.get("userId");
        String sessionId = sessionParamsMap.get("___ID");

        if (StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(userId)) {
            return StringUtils.EMPTY;
        }
        String passphrase = sessionId + userId;
        FileMaskProcessor processor = new FileMaskProcessor(passphrase,
                passphrase.getBytes().length);
        byte[] actual = urlComponent.getBytes();
        byte[] result = new byte[actual.length];
        processor.process(actual, 0, actual.length, result);

        String encryptedUrl = Base64.encode(result);

        return encryptedUrl;
    }
}

package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Range;
import org.restlet.engine.http.header.RangeReader;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Result;
import play.mvc.Results;

import com.ning.http.util.Base64;
import com.vedantu.commons.content.interfaces.IDownloadable;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CompoundMediaStorage;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.http.HTTPHeaderFormatter;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.utils.ContentTypeMapper;
import com.vedantu.commons.utils.FileMaskProcessor;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.SessionExtractorUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.viewers.utils.DefaultPicUtil;

public class Viewer extends AbstractVedantuController {

    private static final ALogger LOGGER            = Logger.of(Viewer.class);
    private static final int     RANGE_VIDEO_INDEX = 0;
    private static String        HALF_DATA         = "HALF_DATA";

    public static class ContentDownloader implements Callable<FileData> {

        public String entityType;
        public String mediaType;
        public String fileName;
        public String eTag;
        public String lastModified;

        public ContentDownloader(String entityType2, String mediaType2, String fileName2,
                String eTag, String lastModified) {

            this.entityType = entityType2;
            this.mediaType = mediaType2;
            this.fileName = fileName2;
            this.eTag = eTag;
            this.lastModified = lastModified;
        }

        @Override
        public FileData call() throws Exception {

            FileData data = null;
            EntityType providedEntityType = null;
            try {

                providedEntityType = EntityType.valueOfKey(entityType);
                LOGGER.debug(" EntityType " + entityType);

                AbstractEntityFileStorage defs = EntityStorageFactory.INSTANCE
                        .get(providedEntityType);
                LOGGER.debug("File name" + fileName);
                data = defs.getData(entityType, mediaType, fileName);

                LOGGER.error(" Fetched data ");
                LOGGER.debug("Found tags " + data.getFileMetaInfo() + " size available"
                        + data.getIn().available());

            } catch (Exception exp) {
                LOGGER.error(exp.getMessage(), exp);
                if (data != null) {
                    IOUtils.closeQuietly(data.getIn());
                }
                return data;
            } catch (EntityFileStorageException e) {
                LOGGER.error(e.getMessage(), e);
                if (data != null) {
                    IOUtils.closeQuietly(data.getIn());
                }
                return data;

            }
            return data;
        }

    }

    public static Result view(String entityType, String mediaType, final String fileName) {

        final String requestedETAG = request().getHeader(IF_NONE_MATCH);

        ContentDownloader contentDownloader = new ContentDownloader(entityType, mediaType,
                fileName, request().getHeader(IF_NONE_MATCH), request()
                        .getHeader(IF_MODIFIED_SINCE));
        Promise<FileData> result = Akka.future(contentDownloader);
        return async(result.map(new play.libs.F.Function<FileData, Result>() {

            public Result apply(FileData fileData) {

                try {
                    Date expiryTiming = new Date(System.currentTimeMillis() + (3600 * 1000));
                    String expiryTime = HTTPHeaderFormatter.formatter.format(expiryTiming);
                    if (fileData == null || fileData.getIn() == null) {
                        LOGGER.error("No stream of data found");
                        return notFound();
                    }
                    if (StringUtils.isNotEmpty(requestedETAG)
                            && requestedETAG.equals(fileData.getFileMetaInfo().get(ETAG))) {
                        fileData.getIn().close();
                        return Results.status(NOT_MODIFIED);
                    }
                    if (fileData.getFileMetaInfo().get(ETAG) != null) {
                        response().setHeader(ETAG, fileData.getFileMetaInfo().get(ETAG));
                    }

                    if (fileData.getFileMetaInfo().get(LAST_MODIFIED) != null) {
                        response().setHeader(LAST_MODIFIED,
                                fileData.getFileMetaInfo().get(LAST_MODIFIED));
                    }

                    if (fileData.getFileMetaInfo() != null) {
                        String fetchedExpiryTime = fileData.getFileMetaInfo().get(EXPIRES);
                        if (StringUtils.isNotEmpty(fetchedExpiryTime)) {
                            expiryTime = fetchedExpiryTime;
                        }
                    }

                    LOGGER.debug(" File system provided Expiry time " + expiryTime);

                    response().setHeader(CONTENT_LENGTH, "" + fileData.getContentLength());
                    response().setHeader(EXPIRES, expiryTime);

                    LOGGER.debug("Content being served " + fileData.getContentLength());
                    return ok(fileData.getIn())
                            .as(ContentTypeMapper.get().getContentType(fileName));

                } catch (Exception e) {

                    LOGGER.error("Failed crashed ", e);
                    return notFound();
                }

            }
        }

        ));
    }

    public static Result download(String entityType, String mediaType, final String fileName,
            final String id) {

        response().getHeaders().put(CONTENT_TRANSFER_ENCODING, "binary");
        response().getHeaders().put(CONTENT_TYPE, "application/force-download");
        String originalFileName = "download" + FileUtils.getExtension(fileName);

        EntityType type = EntityType.valueOfKey(entityType);
        if (type == EntityType.UNKNOWN) {
            return notFound();
        }
        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(type);

        if (dao == null) {
            return notFound();
        }

        VedantuBaseMongoModel mongoModel = dao.getById(id);
        LOGGER.debug(" model found for id " + id + mongoModel);
        if (mongoModel == null) {
            return notFound();
        }
        if (!(dao instanceof IDownloadable)) {
            return notFound();
        }

        IDownloadable downloadableDAO = (IDownloadable) dao;
        originalFileName = StringUtils.isNotEmpty(downloadableDAO.getDownloadName(id, mongoModel)) ? downloadableDAO
                .getDownloadName(id, mongoModel) + FileUtils.getExtension(fileName)
                : originalFileName;
        LOGGER.debug("file original name " + originalFileName);
        response().getHeaders().put("Content-Disposition",
                "attachment; filename=\"" + originalFileName + "\"");
        ContentDownloader downloader = new ContentDownloader(entityType, mediaType, fileName, null,
                null);
        Promise<FileData> result = Akka.future(downloader);
        return async(result.map(new play.libs.F.Function<FileData, Result>() {

            public Result apply(FileData fileData) {

                try {

                    if (fileData == null || fileData.getIn() == null) {
                        LOGGER.error("No stream of data found");
                        return notFound();
                    }

                    response().setHeader(CONTENT_LENGTH, "" + fileData.getContentLength());

                    LOGGER.debug("Content being served " + fileData.getContentLength());
                    return ok(fileData.getIn())
                            .as(ContentTypeMapper.get().getContentType(fileName));

                } catch (Exception e) {

                    LOGGER.error("Failed crashed ", e);
                    return notFound();
                }

            }
        }

        ));

    }

    private static String checkUserAgent(String userAgent) {

        LOGGER.debug("UserAgent " + userAgent);
        // // if( userAgent.contains("Chrome")){
        // // return "true";
        // // }
        // if( userAgent.contains("Firefox")){
        // return "false";
        // }
        return "false";
    }

    // fixed application mandated chunk size testing
    public static Result stream(String entityType, String mediaType, String fileName) {

        // generic header
        response().getHeaders().put(ACCEPT_RANGES, "bytes");

        FileData data = null;
        EntityType providedEntityType = null;
        LOGGER.info(" HTTP_REQUEST_HEADER" + request().headers() + " \n Range "
                + request().getHeader(RANGE));

        // TODO check later
        // response() = HttpConstants.HTTP_STATUS_PARTIAL_CONTENT;

        long defaultStreamSize = Play.application().configuration()
                .getLong("default.video.chunk.size", 1024 * 1024L);

        String rangeHeader = request().getHeader(RANGE);
        LOGGER.info("===== Range Header string : " + rangeHeader);
        LOGGER.info("===== Range Half data string : " + request().getHeader(HALF_DATA));
        LOGGER.info("===== Range user agent : " + request().getHeader(USER_AGENT));
        String halfData = checkUserAgent(request().getHeader(USER_AGENT));
        Range range = new Range();
        if (rangeHeader != null) {
            List<Range> ranges = RangeReader.read(rangeHeader);

            range = ranges.get(RANGE_VIDEO_INDEX); // we will read only first
                                                   // range if its a list
            if ("true".equals(halfData)) {

                LOGGER.info(" Range" + range.getIndex() + "  " + range.getSize());

                if (range.getSize() == Range.SIZE_MAX || range.getSize() > defaultStreamSize
                        || range.getSize() < 1) {
                    range.setSize(defaultStreamSize);
                }
            }
        } else if ("true".equals(halfData)) {

            range.setSize(defaultStreamSize);
        }

        try {
            LOGGER.debug("Getting FileName " + fileName + " RANGE " + range + " time "
                    + System.currentTimeMillis());
            providedEntityType = EntityType.valueOfKey(entityType);
            IEntityFileStorage defs = EntityStorageFactory.INSTANCE.get(providedEntityType);
            data = defs.getData(entityType, mediaType, fileName, range);
            LOGGER.debug("Fetched FileName " + fileName + " RANGE " + range + " time "
                    + System.currentTimeMillis());

        } catch (Exception exp) {
            LOGGER.error(exp.getMessage(), exp);

            try {
                return ok(DefaultPicUtil.get(providedEntityType)).as(
                        ContentTypeMapper.get().getContentType(fileName));
            } catch (FileNotFoundException e) {
                return notFound();
            }
        } catch (EntityFileStorageException e) {

            LOGGER.error(e.getMessage(), e);

            try {
                return ok(DefaultPicUtil.get(providedEntityType)).as(
                        ContentTypeMapper.get().getContentType(fileName));
            } catch (FileNotFoundException ex) {
                return notFound();
            }
        
        
        }

        if (data.getIn() == null) {

            return notFound();
        }
        try {
            LOGGER.debug("Retrieved file type with name" + fileName + " Content-Type"
                    + data.getFileMetaInfo() + " part size of file:" + data.getContentLength()
                    + data.getIn().available());
        } catch (IOException e) {
            LOGGER.debug("Failed to see data inputstream");
        }

        LOGGER.debug("Mime type for the content file type : "
                + ContentTypeMapper.get().getContentType(fileName));

        HTTPHeaderFormatter headerFormatter = new HTTPHeaderFormatter();
        headerFormatter.addAcceptRangesHeader();

        data.getFileMetaInfo().putAll(headerFormatter);

        // by Shankar --> as we don't need these values

        // for (String key : data.getFileMetaInfo().keySet()) {
        // LOGGER.debug("Header :" + key + " " + data.fileMetaInfo.get(key));
        // response().setHeader(key, data.fileMetaInfo.get(key));
        // }

        // TODO we need to explore how to set header connection:close as it was
        // not working before
        LOGGER.debug(" Range: index " + range.getIndex() + "  size " + range.getSize());
        long startSize = range.getIndex();
        long size = range.getSize();

        if (size <= 0) {
            size = data.getContentLength();
        }

        response().setHeader(CONNECTION, "close");

        response().setHeader(CONTENT_LENGTH, "" + data.getContentLength());

        response().getHeaders().put(CONTENT_RANGE,
                headerFormatter.addContentRangeHeader(startSize, size, data.getFileSize()));
        response().setContentType(ContentTypeMapper.get().getContentType(fileName));
        LOGGER.debug(response().getHeaders().toString());
        LOGGER.debug("Serving FileName " + fileName + " RANGE " + range + " time "
                + System.currentTimeMillis());

        return Results.status(PARTIAL_CONTENT, data.getIn(), (int) data.getContentLength()).as(
                ContentTypeMapper.get().getContentType(fileName));
    }

    public static Result estream(String encrypted) {

        Map<String, String> sessionMap = SessionExtractorUtils.getSessionParams(request().cookie(
                Play.application().configuration().getString("application.session.cookie")));

        LOGGER.debug("session params: " + sessionMap);

        if (!CollectionUtils.isNotEmpty(sessionMap.keySet())) {
            return notFound();
        }

        String userId = sessionMap.get("userId");
        String sessionId = sessionMap.get("___ID");

        if (StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(userId)) {
            return notFound();
        }

        String passphrase = sessionId + userId;
        FileMaskProcessor processor = new FileMaskProcessor(passphrase,
                passphrase.getBytes().length);
        byte[] decode = Base64.decode(encrypted);
        byte[] result = new byte[decode.length];
        processor.process(decode, 0, decode.length, result);

        String fileId = new String(result);
        LOGGER.debug("file id: " + fileId);
        String[] fileIdComponenets = fileId.split(FileUtils.SEPARATOR_FWDSLASH);

        if (fileIdComponenets.length == 3) {
            return stream(fileIdComponenets[0], fileIdComponenets[1], fileIdComponenets[2]);

        }
        return notFound();

    }

    public static Result storeFolder(String filePath) {

        LOGGER.debug("folder path: " + filePath);

        if (StringUtils.isEmpty(filePath)) {
            return notFound("missing path");
        }
        File folderPath = new File(filePath);
        if (!folderPath.isDirectory()) {
            LOGGER.error("path: " + filePath + " not a directory");
            return noContent();
        }
        CompoundMediaStorage mediaStorage = new CompoundMediaStorage();
        String uuid = UUID.randomUUID().toString();
        LOGGER.debug("dest folder name: " + uuid);
        try {
            mediaStorage.storeInFS(folderPath, uuid, null);
        } catch (EntityFileStorageException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ok("uploaded : " + filePath);
    }
}

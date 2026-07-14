package com.vedantu.ext.cmds.db.models;

public class FileDownloadInfo extends AbstractDBModel {

    /**
     * Schema { name ( CHAR ), entityId( CHAR ),entityType ( CHAR ),size( UNSIGNED BIG INT),
     * downloadUrl ( TEXT ),downloaded (BOOLEAN), downloadedSize( UNSIGNED BIG
     * INT),downloadStartTime (UNSIGNED BIG INT), downloadEndTime (UNSIGNED BIG INT), location
     * STRING
     */
    private static final long  serialVersionUID          = 1L;

    public static final String FIELD_NAME                = "name";
    public static final String FIELD_ENTITY_ID           = "entityId";
    public static final String FIELD_ENTITY_TYPE         = "entityType";
    public static final String FIELD_SIZE                = "size";
    public static final String FIELD_LOCATION            = "location";

    public static final String FIELD_TARGET_ID           = "targetId";
    public static final String FIELD_TARGET_TYPE         = "targetType";

    public static final String FIELD_DOWNLOAD_URL        = "downloadUrl";
    public static final String FIELD_DOWNLOADED          = "downloaded";
    public static final String FIELD_DOWNLOADED_SIZE     = "downloadedSize";
    public static final String FIELD_DOWNLOAD_START_TIME = "downloadStartTime";
    public static final String FIELD_DOWNLOAD_END_TIME   = "downloadEndTime";

    public static final String FIELD_MEDIA_TYPE          = "mediaType";

    public String              name;
    public String              entityId;
    public String              entityType;
    public String              mediaType;
    public long                size;
    public String              location;                                       // relative location
    public String              downloadUrl;                                    // latest
                                                                                // download
                                                                                // url

    public String              targetId;
    public String              targetType;

    public boolean             downloaded;
    public long                downloadedSize;
    public long                downloadStartTime;
    public long                downloadEndTime;

    public FileDownloadInfo() {

        super();
    }

    public FileDownloadInfo(int orgKeyId, String entityId, String entityType, String name,
            String targetId, String targetType, String downloadUrl, boolean downloaded, long size,
            long downloadedSize, long downloadStartTime, long downloadEndTime, String mediaType) {

        super(orgKeyId);
        this.entityId = entityId;
        this.entityType = entityType;
        this.name = name;
        this.downloadUrl = downloadUrl;
        this.downloaded = downloaded;
        this.size = size;
        this.downloadedSize = downloadedSize;
        this.downloadStartTime = downloadStartTime;
        this.downloadEndTime = downloadEndTime;
        this.mediaType = mediaType;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", name:").append(name).append(", downloadUrl:").append(downloadUrl)
                .append(", downloaded:").append(downloaded).append(", size:").append(size)
                .append(", downloadedSize:").append(downloadedSize).append(", downloadStartTime:")
                .append(downloadStartTime).append(", downloadEndTime:").append(downloadEndTime)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}

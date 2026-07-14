package com.vedantu.ext.cmds.db.models;

import com.vedantu.ext.cmds.enums.DownloadProcessState;
import com.vedantu.ext.cmds.utils.commons.StringUtils;

public class SDCard extends AbstractDBModel {

    /**
     * 
     */
    private static final long  serialVersionUID      = 1L;

    public static final String FIELD_GROUP_ID        = "groupId";
    public static final String FIELD_SIZE            = "size";
    public static final String FIELD_CONTENT_SIZE    = "contentSize";
    public static final String FIELD_ID              = "id";

    public final static String FIELD_DOWNLOADED_SIZE = "downloadedSize";
    public final static String FIELD_DOWNLOADED      = "downloaded";
    public final static String FIELD_STATE           = "state";

    public static final String FIELD_COUNT           = "count";

    public String              name;
    public String              id;
    public String              groupId;
    public long                size;
    public long                contentSize;
    public long                count;

    public String              state                 = DownloadProcessState.INITIALIZED.name();

    public long                downloadedSize;
    public boolean             downloaded;

    public SDCard() {

        super();
    }

    public SDCard(int orgKeyId, String name, String id, String groupId, long size,
            long contentSize, long timeCreated, long count) {

        super(orgKeyId);
        this.name = name;
        if (this.name == null) {
            this.name = StringUtils.EMPTY;
        }
        this.id = id;
        this.groupId = groupId;
        this.size = size;
        this.contentSize = contentSize;
        this.timeCreated = timeCreated;
        this.count = count;
    }

    public String getName() {

        return name;
    }

    public String getId() {

        return id;
    }

    public String getGroupId() {

        return groupId;
    }

    public long getSize() {

        return size;
    }

    public long getContentSize() {

        return contentSize;
    }

    public long getCount() {

        return count;
    }

    public void setCount(long count) {

        this.count = count;
    }

    public String getState() {

        return state;
    }

    public void setState(String state) {

        this.state = state;
    }

    public long getDownloadedSize() {

        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {

        this.downloadedSize = downloadedSize;
    }

    public boolean isDownloaded() {

        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {

        this.downloaded = downloaded;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setSize(long size) {

        this.size = size;
    }

    public void setContentSize(long contentSize) {

        this.contentSize = contentSize;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", id:").append(id).append(", groupId:")
                .append(groupId).append(", size:").append(size).append(", contentSize:")
                .append(contentSize).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}

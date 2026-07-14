package com.vedantu.commons.entity.storage;

public class StorageResult {
    public final String folderId;
    public final String fileId;
    public final boolean isStored;
    public String displayUrlComponent;
    public final String uuid;

    public StorageResult(final String uuid, final String folderId, final String fileId,
            final boolean isStored, final String displayUrlComponent) {
        this.folderId = folderId;
        this.fileId = fileId;
        this.isStored = isStored;
        this.displayUrlComponent = displayUrlComponent;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("uuid:").append(uuid);
        sb.append("folderId:").append(folderId);
        sb.append(", ").append("fileId:").append(fileId);
        sb.append(", ").append("isStored:").append(isStored);
        sb.append(", ").append("displayUrlComponent:")
                .append(displayUrlComponent);
        sb.append("}");
        return sb.toString();
    }

}

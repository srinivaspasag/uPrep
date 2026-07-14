package com.vedantu.ext.cmds.db.models;

public class SyncInfo extends AbstractDBModel {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    public static final String FIELD_KEY        = "key";
    public static final String SYNC_TIME        = "syncTime";

    public String              key;
    public long                syncTime;

    public SyncInfo() {

        super();
    }

    public SyncInfo(int orgKeyId, String key, long syncTime) {

        super(orgKeyId);
        this.key = key;
        this.syncTime = syncTime;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{key:").append(key).append(", syncTime:").append(syncTime).append(", _id:")
                .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}

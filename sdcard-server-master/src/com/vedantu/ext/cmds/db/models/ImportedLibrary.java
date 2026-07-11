package com.vedantu.ext.cmds.db.models;

public class ImportedLibrary extends AbstractDBModel {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;

    public final static String ID               = "id";
    public final static String TYPE             = "type";
    public final static String NAME             = "name";
    public final static String DOWNLOADED_SIZE  = "downloadedSize";
    public final static String DOWNLOADED       = "downloaded";
    public final static String STATE            = "state";

    public String              id;
    public String              type;
    public String              name;
    public long                size;
    public long                downloadedSize;
    public boolean             downloaded;
    public String              state;

    public ImportedLibrary() {

        super();
    }

    public ImportedLibrary(int orgKeyId, String id, String type, String name, long size) {

        super(orgKeyId);
        this.id = id;
        this.type = type;
        this.name = name;
        this.size = size;
    }

    public String getId() {

        return id;
    }

    public String getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public long getSize() {

        return size;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", type:").append(type).append(", name:")
                .append(name).append(", size:").append(size).append(", _id:").append(_id)
                .append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}

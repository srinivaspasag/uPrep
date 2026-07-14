package com.vedantu.commons.pojos;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class DownloadableFileInfo implements IListResponseObj {

    public String     name;
    public EntityType entityType;
    public String     entityId;
    public MediaType  mediaType;
    public long       size;
    public String     downloadUrl;

}

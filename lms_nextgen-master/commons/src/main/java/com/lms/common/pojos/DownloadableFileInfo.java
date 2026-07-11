package com.lms.common.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DownloadableFileInfo implements IListResponseObj {

    public String     name;
    public EntityType entityType;
    public String     entityId;
    public MediaType  mediaType;
    public long       size;
    public String     downloadUrl;

}

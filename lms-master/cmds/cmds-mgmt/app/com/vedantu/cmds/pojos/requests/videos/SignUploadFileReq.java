package com.vedantu.cmds.pojos.requests.videos;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class SignUploadFileReq extends AbstractAuthCheckReq {

    @Required
    public String     orgId;
    @Required
    public String     fileName;
    @Required
    public EntityType type;
    @Required
    public MediaType  mediaType;

    public String     url;

}

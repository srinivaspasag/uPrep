package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetContentSecuredDownloadLinkReq extends AbstractOrgScopeReq {

    @Required
    public EntityType entityType;
    @Required
    public MediaType mediaType;

    @Required
    public String     entityId;

    @Required
    public String     fileName;

}

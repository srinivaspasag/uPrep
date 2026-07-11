package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.OrganizationEntity;

public class GetLibraryResourcesReq extends AbstractGetResourcesReq {

    @Required
    public OrganizationEntity orgEntity;

}

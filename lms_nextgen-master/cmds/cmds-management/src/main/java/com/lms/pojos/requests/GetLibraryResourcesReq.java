package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.OrganizationEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetLibraryResourcesReq extends AbstractGetResourcesReq {

    public OrganizationEntity orgEntity;

}

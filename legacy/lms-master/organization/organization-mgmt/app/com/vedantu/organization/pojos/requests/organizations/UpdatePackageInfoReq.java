package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.pojos.PackageInfo;

public class UpdatePackageInfoReq extends AbstractAuthCheckReq{

    @Required
    public String orgId;

    @Required
    public String sectionId;

    @Required
    public List<PackageInfo> packagesList;

}

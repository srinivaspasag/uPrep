package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;

public class UpdateOrgSectionReq extends AbstractAddOrgStructureReq {

    @Required
    public String       programId;
    @Required
    public String       sectionId;

    public AccessScope  accessScope;

    public RevenueModel revenueModel;

    public Boolean      sdOnly;

    public List<String> descriptionPoints;

    public String       thumbnail;

    public String       imageNameWithExtension;

}

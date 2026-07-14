package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrganizationSharedSubjects {
    @Required
    public boolean showSharedSubjects;
    @Required
    public String orgId;

}

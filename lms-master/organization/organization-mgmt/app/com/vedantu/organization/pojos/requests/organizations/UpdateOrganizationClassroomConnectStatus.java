package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrganizationClassroomConnectStatus {
    @Required
    public boolean showClassroomConnect;
    @Required
    public String orgId;
}

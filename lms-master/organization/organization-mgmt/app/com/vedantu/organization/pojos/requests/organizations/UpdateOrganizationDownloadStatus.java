package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class UpdateOrganizationDownloadStatus {
    @Required
    public boolean disableDownload;
    @Required
    public String orgId;
}

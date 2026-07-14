package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

public class UpdateDigitalLibraryFieldsReq {

    @Required
    public String userId;
    public String orgId;
    public List<String> fields;

}

package com.vedantu.organization.pojos.requests.organizations;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.vedantu.organization.enums.AccessScope;

public class GetCategorySectionsReq extends AbstractOrgListReq {

    // @Required
    public String             name;

    public Boolean            openOnly;         // need to keep it till mobile version releases
                                                 // which can
                                                 // use updated code

    public String id;
    public AccessScope scope;
    public boolean            excludeSubscribed;

    public GetCategorySectionsReq() {

        userId = "PUBLIC"; // this api can be used with both userId specify or not specified
        callingUserId = "PUBLIC"; // this api can be used with both userId specify or not specified

    }

}

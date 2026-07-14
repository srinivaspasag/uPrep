package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;
import com.vedantu.organization.pojos.responses.organizations.EditCategoryInfo;

public class EditCategoriesReq extends AbstractOrgScopeReq {

    public List<EditCategoryInfo> categoryList;

}

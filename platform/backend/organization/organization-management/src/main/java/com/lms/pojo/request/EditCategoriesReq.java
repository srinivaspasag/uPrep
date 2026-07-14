package com.lms.pojo.request;

import com.lms.pojo.EditCategoryInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class EditCategoriesReq extends AbstractOrgScopeReq {

    public List<EditCategoryInfo> categoryList;

}

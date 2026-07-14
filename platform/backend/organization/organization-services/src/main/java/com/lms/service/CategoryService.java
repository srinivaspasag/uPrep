package com.lms.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojo.request.*;


public interface CategoryService {

	VedantuResponse addCategory(AddCategoryReq addCategoryReq);

	VedantuResponse editCategory(EditCategoryReq editCategoryReq);

	VedantuResponse removeCategory(RemoveCategoryReq removeCategoryReq);

	VedantuResponse editCategories(EditCategoriesReq editCategoriesReq);

	VedantuResponse getCategories(AbstractOrgScopeReq abstractOrgScopeReq);

	VedantuResponse getCategory(GetCategoryReq getCategoryReq);

	VedantuResponse customizeCategory(CustomizeCategoryReq customizeCategoryReq);

	VedantuResponse getCategorySections(GetCategorySectionsReq getCategorySectionsReq);

	VedantuResponse getCategorySection(GetCategorySectionReq getCategorySectionReq);

	VedantuResponse getMemberCategorySections(GetSelfCategorySectionsReq getSelfCategorySectionsReq);

}

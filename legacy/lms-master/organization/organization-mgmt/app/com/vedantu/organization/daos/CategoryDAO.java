package com.vedantu.organization.daos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.models.Category;
import com.vedantu.organization.pojos.requests.organizations.EditCategoriesReq;
import com.vedantu.organization.pojos.responses.organizations.EditCategoryInfo;

public class CategoryDAO extends VedantuBasicDAO<Category, ObjectId> {

    private static final ALogger    LOGGER   = Logger.of(OrgSectionDAO.class);

    public static final CategoryDAO INSTANCE = new CategoryDAO();

    private CategoryDAO() {

        super(Category.class);
    }

    public String addCategory(String orgId, String name, Set<String> sectionIds)
            throws VedantuException {

        LOGGER.debug("......entering addCategory DAO function......");

        Category category = new Category(orgId, name, sectionIds);
        category.priority = 3;

        LOGGER.debug("......about to save category ......");
        try {
            save(category);
        } catch (DuplicateKey e) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        return category._getStringId();
    }

    public List<Category> getCategories(String orgId) {

        LOGGER.debug("......entering addCategory DAO function......");
        List<Category> categories = getQuery().filter("orgId", orgId).order("priority").asList();
        return categories;
    }

    public boolean editCategory(String id, String name, Set<String> sectionIds)
            throws VedantuException {

        LOGGER.debug("......entering addCategory DAO function......");
        Category category = getById(id);
        if (null == category) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS);
        }
        if (!StringUtils.isEmpty(name)) {
            category.setName(name);
        }

        // if(!CollectionUtils.isEmpty(sectionIds))
        // {
        category.sectionIds = sectionIds;
        // }
        save(category);
        return true;
    }

    public boolean editCategories(EditCategoriesReq editCategoriesReq) throws VedantuException {

        LOGGER.debug("......entering addCategory DAO function......  "
                + editCategoriesReq.categoryList);

        List<EditCategoryInfo> categoryList = editCategoriesReq.categoryList;

        if (categoryList == null) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_LIST_NOT_DEFINED);
        }
        LOGGER.debug("......categoryList is not null." + categoryList.get(0).name + "...");

        for (EditCategoryInfo editCategoryInfo : categoryList) {
            LOGGER.debug("......entered categoryinfo loop......");
            String id = editCategoryInfo.id;
            if (id == null) {
                LOGGER.debug("......category id is null......");
                continue;
            }

            Category category = getById(id);

            if (null == category) {
                LOGGER.debug("......category is null......");
                continue;
            }

            if (StringUtils.isNotEmpty(editCategoryInfo.name)) {
                category.setName(editCategoryInfo.name);
            }

            if (category.sectionIds == null) {
                category.sectionIds = new HashSet<String>();
            }

            if (editCategoryInfo.addedSectionIds != null) {
                LOGGER.debug("......adding sectionIds: ......" + editCategoryInfo.addedSectionIds);
                category.sectionIds.addAll(editCategoryInfo.addedSectionIds);
            }

            if (editCategoryInfo.removedSectionIds != null) {
                LOGGER.debug("......removing sectionIds: ......"
                        + editCategoryInfo.removedSectionIds);
                category.sectionIds.removeAll(editCategoryInfo.removedSectionIds);
            }
            try {
                LOGGER.debug("saving category: " + category);
                save(category);
            } catch (DuplicateKey e) {
                LOGGER.error("a category already exist with " + e.getMessage(), e);
            }

        }
        return true;
    }

    public boolean removeCategory(String id) throws VedantuException {

        LOGGER.debug("......entering addCategory DAO function......");
        Category category = getById(id);
        if (null == category) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS);
        } else {
            delete(category);
        }
        return true;
    }

    public Category getCategory(String orgId, String name) throws VedantuException {

        Query<Category> categoryQuery = getQuery();

        categoryQuery.filter(ConstantsGlobal.ORG_ID, orgId);

        categoryQuery.filter(ConstantsGlobal.NAME, name);

        Category category = categoryQuery.get();
        if (category == null) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS,
                    "no category found with name: " + name + ", for orgId:" + orgId);
        }
        return category;
    }

    public Category getCategoryById(String id) throws VedantuException {

        return getById(id);
    }

    public boolean checkPriority(int priority) {
        Query<Category> categoryQuery = getQuery();
        categoryQuery.filter("priority", priority);
        List<Category> categories = categoryQuery.asList();
        if (priority == 1 || priority == 2) {
            if (categories.size() > 0) {
                return false;
            }
                return true;
        }
        return true;
    }

}

package com.vedantu.organization.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Play;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.fs.handlers.S3Handler;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.CategoryDAO;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.Category;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.organization.pojos.OrgProgramSectionBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;
import com.vedantu.organization.pojos.requests.CustomizeCategoryReq;
import com.vedantu.organization.pojos.requests.GetCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.AddCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.EditCategoriesReq;
import com.vedantu.organization.pojos.requests.organizations.EditCategoryReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategoriesReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategorySectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetCategorySectionsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.GetSelfCategorySectionsReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveCategoryReq;
import com.vedantu.organization.pojos.responses.CustomizeCategoryRes;
import com.vedantu.organization.pojos.responses.GetCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.AddCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.CategoryInfo;
import com.vedantu.organization.pojos.responses.organizations.EditCategoriesRes;
import com.vedantu.organization.pojos.responses.organizations.EditCategoryRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategoriesRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategorySectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetCategorySectionsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.OrgSectionInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveCategoryRes;

public class CategoryManager {

	private static final ALogger LOGGER = Logger.of(CategoryManager.class);

	public static AddCategoryRes addCategory(AddCategoryReq addCategoryReq)
			throws VedantuException {

		AddCategoryRes res = new AddCategoryRes();
		res.id = CategoryDAO.INSTANCE.addCategory(addCategoryReq.orgId,
				addCategoryReq.name, addCategoryReq.sectionIds);
		return res;
	}

	public static GetCategoriesRes getCategories(
			GetCategoriesReq getCategoriesReq) throws VedantuException {

		LOGGER.debug("......entering getCategories function......");
		GetCategoriesRes res = new GetCategoriesRes();

		//Get all the orgIds that gave access to the current organization
//        MutableLong totalProgramHits = new MutableLong(0L);
//   	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getCategoriesReq.orgId, null, totalProgramHits);
//        List<String> grantedOrgs = new ArrayList<String>();
//        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
//			grantedOrgs.add(granteeOrgProgram.providerOrgId);
//		}
//		grantedOrgs.add(getCategoriesReq.orgId);
//		HashSet uniqueGrantedOrgs = new HashSet(grantedOrgs);
//		grantedOrgs.clear();
//		grantedOrgs.addAll(uniqueGrantedOrgs);
//		List<Category> categories = new ArrayList<Category>();
//		for (String orgId : grantedOrgs) {
//			LOGGER.debug("......ogIds......" + orgId);
//			categories.addAll(CategoryDAO.INSTANCE.getCategories(orgId));
//		}
        List<Category> categories = CategoryDAO.INSTANCE.getCategories(getCategoriesReq.orgId);
        List<CategoryInfo> categoryInfos = OrgSectionManager.toCategoryInfo(categories);
        res.list.addAll(categoryInfos);
        return res;
    }

	public static GetCategoryRes getCategory(GetCategoryReq getCategoryReq)
            throws VedantuException {

	    GetCategoryRes res = new GetCategoryRes();
	    res.category = CategoryDAO.INSTANCE.getCategory(getCategoryReq.orgId,getCategoryReq.name);
	    LOGGER.debug("Category manager  : " + res);
        return res;
    }

	public static EditCategoryRes editCategory(EditCategoryReq editCategoryReq)
			throws VedantuException {

		EditCategoryRes res = new EditCategoryRes();
		res.edited = CategoryDAO.INSTANCE.editCategory(editCategoryReq.id,
				editCategoryReq.name, editCategoryReq.sectionIds);
		return res;
	}

	public static EditCategoriesRes editCategories(
			EditCategoriesReq editCategoriesReq) throws VedantuException {

		LOGGER.debug("......entering editCategories function......"
				+ editCategoriesReq.categoryList.get(0).id);
		EditCategoriesRes res = new EditCategoriesRes();
		res.edited = CategoryDAO.INSTANCE.editCategories(editCategoriesReq);
		return res;
	}

	public static RemoveCategoryRes removeCategory(
			RemoveCategoryReq removeCategoryReq) throws VedantuException {

		RemoveCategoryRes res = new RemoveCategoryRes();
		res.deleted = CategoryDAO.INSTANCE.removeCategory(removeCategoryReq.id);
		return res;
	}

	public static GetCategorySectionsRes getCategorySections(
			GetCategorySectionsReq req) throws VedantuException {

		GetCategorySectionsRes res = new GetCategorySectionsRes();
		Set<String> sectionIds = new HashSet<String>();
		LOGGER.debug("......Request......" + req.id);
		LOGGER.debug("......Request......" + req.name+"+");

		//Get all the orgIds that gave access to the current organization
        MutableLong totalProgramHits = new MutableLong(0L);
   	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(req.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
			grantedOrgs.add(granteeOrgProgram.providerOrgId);
			Logger.debug("ProviderOrgId:"+granteeOrgProgram.providerOrgId);
		}

		if (StringUtils.isEmpty(req.id)) {

			List<String> orgIds = new ArrayList<String>();
			orgIds.add(req.orgId);
			if (grantedOrgs != null)
				orgIds.addAll(grantedOrgs);
			List<Category> categories = new ArrayList<Category>();
			for (String orgId : orgIds) {
				categories.addAll(CategoryDAO.INSTANCE.getCategories(orgId));
			}
			if (CollectionUtils.isNotEmpty(categories)) {
				for (Category category : categories) {
					if (category.name.equals(req.name) ){
						if (CollectionUtils.isNotEmpty(category.sectionIds)) {
							sectionIds.addAll(category.sectionIds);
							LOGGER.info("in if loop");
						}
					}
					if (req.name.equals("")) {
						if (CollectionUtils.isNotEmpty(category.sectionIds)) {
							sectionIds.addAll(category.sectionIds);
							LOGGER.info("in else loop");

						}
					}

				}
			}

		} else {
			// Get by Id
			Category category = CategoryDAO.INSTANCE.getCategoryById(req.id);
			// Category category = CategoryDAO.INSTANCE.getCategory(req.orgId,
			// req.name);
			if (category.orgId.equals(req.orgId)
					|| (grantedOrgs != null && grantedOrgs
							.contains(category.orgId))) {
				if (CollectionUtils.isNotEmpty(category.sectionIds)) {
					sectionIds.addAll(category.sectionIds);
				}
			}
		}

		if (req.excludeSubscribed) {
			OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(
					req.orgId, req.userId);
			if (orgMember == null) {
				throw new VedantuException(
						VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
			}

			if (CollectionUtils.isNotEmpty(orgMember.mappings)) {
				Set<String> excludeSections = new HashSet<String>();

				for (OrgMemberMappingInfo exclude : orgMember.mappings) {
				    excludeSections.add(exclude.sectionId);
				}
				sectionIds.removeAll(excludeSections);
			}
		}

		if (CollectionUtils.isEmpty(sectionIds)) {
			return res;
		}

		AccessScope scope = null;
		if (req.openOnly != null && req.openOnly == Boolean.TRUE) {
			scope = AccessScope.OPEN;
		} else {
			scope = req.scope;
		}
		MutableLong totalHits = new MutableLong();
		List<String> orgIds = new ArrayList<String>();
		orgIds.add(req.orgId);
		if (grantedOrgs != null)
			orgIds.addAll(grantedOrgs);

		List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionsByIds(
				orgIds, null,
				ObjectIdUtils.toObjectIds(new ArrayList<String>(sectionIds)),
				scope, null, VedantuRecordState.ACTIVE, req.start, req.size,
				totalHits);

		Set<String> programIds = new HashSet<String>();
		Set<String> centerIds = new HashSet<String>();
		res.totalHits = totalHits.longValue();

		List<OrgSection> filterdSections=new ArrayList<OrgSection>();
		for (OrgSection section : sections) {
			Logger.debug("SectionLoop:"+ section.orgId+","+section.programId+","+section.centerId);
			// only for current organization sections
			if(section.orgId.equals(req.orgId)){
				filterdSections.add(section);
				programIds.add(section.programId);
				centerIds.add(section.centerId);
			}else{
				for(GranteeOrgProgram grantedOrg : granteeOrgPrograms){
					Logger.debug("GranteeProgId:"+grantedOrg.programId);
					if(grantedOrg.programId.equals(section.programId))
					{
						filterdSections.add(section);
						programIds.add(section.programId);
						centerIds.add(section.centerId);
						break;
					}
				}
			}

			/*
			 * If the org id of the section matches the org id, just add
			 * section to the filter sections and add its programids and cetids
			 *
			 * else
			 *
			 * checkthe orgid and programid comb is part of thelist
			 * if yes , add sectionId and programid,ccenterid.
			 */
		}
		sections = filterdSections;
		Logger.debug("ProgrammIds.size:"+programIds.size());
		Logger.debug("Sections:"+sections.size());

		Map<String, OrgStructureBasicInfo> programInfoMap = OrgProgramDAO.INSTANCE
				.toBasicInfosMap(OrgProgramDAO.INSTANCE.getByIds(ObjectIdUtils
						.toObjectIds(new ArrayList<String>(programIds)),
						VedantuRecordState.ACTIVE));
		Map<String, OrgStructureBasicInfo> centerInfoMap = OrgCenterDAO.INSTANCE
				.toBasicInfosMap(OrgCenterDAO.INSTANCE.getByIds(ObjectIdUtils
						.toObjectIds(new ArrayList<String>(centerIds)),
						VedantuRecordState.ACTIVE));

		for (OrgSection section : sections) {
			GetCategorySectionRes catSection = new GetCategorySectionRes();
			catSection.programInfo = programInfoMap.get(section.programId);
			catSection.centerInfo = centerInfoMap.get(section.centerId);
			totalHits.setValue(0);
			catSection.sectionInfo = OrgSectionManager
					.toOrgSectionInfo(section);

			OrgMemberDAO.INSTANCE.getOrgMembers(req.orgId,
					OrgMemberProfile.STUDENT, catSection.programInfo.id,
					catSection.centerInfo.id, catSection.sectionInfo.id, null,
					null, 0, 1, totalHits);
			catSection.sectionInfo.memberCount = totalHits.longValue();

			res.list.add(catSection);
		}
		return res;

	}

	public static GetCategorySectionRes getCategorySection(
			GetCategorySectionReq req) throws VedantuException {

		GetCategorySectionRes response = new GetCategorySectionRes();
		response.isPartOf = false;

		OrgSection section = OrgSectionDAO.INSTANCE.getById(req.sectionId);
		if (section == null) {
			throw new VedantuException(VedantuErrorCode.INVALID_SECTION_ID);
		}
		OrgMemberDAO.INSTANCE.updateOrgMemberExpiredMappings(req.orgId, req.userId);
		OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(req.orgId,
				req.userId);

		if (member != null && CollectionUtils.isNotEmpty(member.mappings)) {
			OrgMemberMappingInfo memberMapping = new OrgMemberMappingInfo(
					section.programId, section.centerId,
					section._getStringId(), null);
			response.isPartOf = member.mappings.contains(memberMapping);
		}

		// TODO change this by making simple call to db

		response.sectionInfo = OrgSectionManager.toOrgSectionInfo(section);
		response.programInfo = OrgProgramDAO.INSTANCE
				.getBasicInfo(section.programId);
		response.centerInfo = OrgCenterDAO.INSTANCE
				.getBasicInfo(section.centerId);

		GetOrgProgramCoursesReq courseRequest = new GetOrgProgramCoursesReq();
		courseRequest.orgId = req.orgId;
		courseRequest.programId = section.programId;
		//courseRequest.friendOrgIds = req.friendOrgIds;

		GetOrgProgramCoursesRes courseResponse = OrgProgramManager
				.getProgramCourses(courseRequest);
		if (courseResponse != null
				&& CollectionUtils.isNotEmpty(courseResponse.list)) {
			response.courseInfo.addAll(courseResponse.list);
		}
		return response;

	}

	public static GetCategorySectionsRes getMemberCategorySections(
			GetSelfCategorySectionsReq req) throws VedantuException {

		GetCategorySectionsRes response = new GetCategorySectionsRes();
		MutableLong totalHits = new MutableLong();
		OrgMemberDAO.INSTANCE.updateOrgMemberExpiredMappings(req.orgId, req.userId);
		OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(req.orgId,
				req.userId);
		// TODO change this by making simple call to db
		Set<String> programIds = new HashSet<String>();
		Set<String> centerIds = new HashSet<String>();
		Set<String> sectionIds = new HashSet<String>();
		if (member != null && CollectionUtils.isNotEmpty(member.mappings)) {

			for (OrgMemberMappingInfo mapping : member.mappings) {
			    programIds.add(mapping.programId);
			    centerIds.add(mapping.centerId);
			    sectionIds.add(mapping.sectionId);
			}
			response.totalHits = member.mappings.size();
		}

		Map<String, OrgStructureBasicInfo> programInfoMap = OrgProgramDAO.INSTANCE
				.toBasicInfosMap(OrgProgramDAO.INSTANCE.getByIds(ObjectIdUtils
						.toObjectIds(new ArrayList<String>(programIds)),
						VedantuRecordState.ACTIVE));
		Map<String, OrgStructureBasicInfo> centerInfoMap = OrgCenterDAO.INSTANCE
				.toBasicInfosMap(OrgCenterDAO.INSTANCE.getByIds(ObjectIdUtils
						.toObjectIds(new ArrayList<String>(centerIds)),
						VedantuRecordState.ACTIVE));
		Map<String, OrgStructureBasicInfo> sectionInfoMap = OrgSectionDAO.INSTANCE
				.toBasicInfosMap(OrgSectionDAO.INSTANCE.getByIds(ObjectIdUtils
						.toObjectIds(new ArrayList<String>(sectionIds)),
						VedantuRecordState.ACTIVE));

        if (member != null && CollectionUtils.isNotEmpty(member.mappings)) {

            for (OrgMemberMappingInfo mapping : member.mappings) {
                GetCategorySectionRes catSection = new GetCategorySectionRes();
                catSection.programInfo = programInfoMap.get(mapping.programId);
                catSection.centerInfo = centerInfoMap.get(mapping.centerId);
                totalHits.setValue(0);
                // TODO: optimise this API call
                OrgSection section = OrgSectionDAO.INSTANCE.getById(mapping.sectionId);
                OrgProgramSectionBasicInfo sectionBasicInfo = new OrgProgramSectionBasicInfo(
                        section._getStringId(), section.recordState, section.getName(),
                        section.code, EntityType.SECTION);
                sectionBasicInfo.thumbnail = section.thumbnail;
                sectionBasicInfo.addSectionExtraInfo(section);
                catSection.sectionInfo = new OrgSectionInfo(sectionBasicInfo.id,
                        sectionBasicInfo.name, sectionBasicInfo.code, sectionBasicInfo.recordState,
                        sectionBasicInfo.accessScope, sectionBasicInfo.revenueModel,
                        sectionBasicInfo.desc, sectionBasicInfo.costRate, sectionBasicInfo.size,
                        sectionBasicInfo.sdOnly, sectionBasicInfo.orgId,
                        sectionBasicInfo.startingRates, sectionBasicInfo.packagesMap,
                        sectionBasicInfo.descriptionPoints, sectionBasicInfo.thumbnail);
                OrgMemberDAO.INSTANCE.getOrgMembers(req.orgId, OrgMemberProfile.STUDENT,
                        catSection.programInfo.id, catSection.centerInfo.id,
                        catSection.sectionInfo.id, null, null, 0, 1, totalHits);
                catSection.sectionInfo.memberCount = totalHits.longValue();

                response.list.add(catSection);
            }
        }

		return response;

	}

    public static CustomizeCategoryRes customizeCategory(CustomizeCategoryReq customizeCategoryReq)
            throws VedantuException {
        CustomizeCategoryRes response = new CustomizeCategoryRes();
        Category category = CategoryDAO.INSTANCE.getById(customizeCategoryReq.id);
        if (category == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_CATEGORY_ID);
        }
        if (category.priority == customizeCategoryReq.priority
                || CategoryDAO.INSTANCE.checkPriority(customizeCategoryReq.priority)) {
            if (!customizeCategoryReq.thumbnail.contains("https://s3.amazonaws.com/")) {
                customizeCategoryReq.thumbnail = getAWSFileUrl(customizeCategoryReq.iconUUID);
            }
            if (!customizeCategoryReq.banner.contains("https://s3.amazonaws.com/")) {
                customizeCategoryReq.banner = getAWSFileUrl(customizeCategoryReq.bannerUUID);
            }
            category.description = customizeCategoryReq.description;
            category.shortDescription = customizeCategoryReq.shortDescription;
            category.priority = customizeCategoryReq.priority;
            category.thumbnail = customizeCategoryReq.thumbnail;
            category.banner = customizeCategoryReq.banner;

            CategoryDAO.INSTANCE.save(category);
            response.category = category;
            response.success = true;
        } else {
            response.category = category;
            response.success = false;
        }

        return response;
    }
    public static String getAWSFileUrl(String imageNameWithExtension) {
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
        String filePath = tempFs.getFilePath("organization", imageNameWithExtension);
        File file = new File(filePath);
        // move to s3 public bucket and get s3 url
        S3Handler handler = new S3Handler();
        try {
            handler.store(file, Play.application().configuration().getString("bucket.name"),
                    imageNameWithExtension, new HashMap<String, String>());
        } catch (FileStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileUtils.deleteFile(imageNameWithExtension, file);
        String AWSFileUrl = "https://"+Play.application().configuration().getString("bucket.name")+".s3.amazonaws.com/"
                + imageNameWithExtension;
        return AWSFileUrl;
    }
}

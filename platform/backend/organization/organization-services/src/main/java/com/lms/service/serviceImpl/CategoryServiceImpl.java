package com.lms.service.serviceImpl;

import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.handlers.LocalFileSystemHandler;
import com.lms.common.fs.handlers.S3Handler;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.OrgMemberProfile;
import com.lms.models.*;
import com.lms.pojo.*;
import com.lms.pojo.request.*;
import com.lms.pojo.responce.*;
import com.lms.repository.*;
import com.lms.service.CategoryService;
import com.mongodb.DuplicateKeyException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private OrganizationsImpl organizationsImpl;
    @Autowired
    private ProgramServiceImpl programServiceImpl;
    @Autowired
    private LocalFileSystemHandler localFileSystemHandler;
    @Autowired
    private S3Handler s3Handler;
    @Value("${amazon.s3.bucket.identifier}")
    private String bucketName;
    @Autowired
    private MemberServiceImpl memberServiceImpl;
    @Value("${learnpedia.id")
    private String learnpediaId;
    @Value("${bucket.name}")
    private String bucketNametemp;
    @Override
    public VedantuResponse addCategory(AddCategoryReq addCategoryReq) {
        if (ObjectIdUtils.hasInvalidId(addCategoryReq.getOrgId())) {
            throw new VedantuException(VedantuErrorCode.INVALID_ID);

        }
        AddCategoryRes res = null;

        try {
            res = new AddCategoryRes();
			res.id = addCategory(addCategoryReq.getOrgId(),
					addCategoryReq.getName(), addCategoryReq.getSectionIds());
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(res);
	}

	public String addCategory(String orgId, String name, Set<String> sectionIds) throws VedantuException {

		Category category = new Category(orgId, name, sectionIds);
		category.priority = 3;
		try {
			categoryRepo.save(category);
		} catch (Exception e) {
			throw new VedantuException(VedantuErrorCode.CATEGORY_ALREADY_EXISTS);
		}

		return category._getStringId();
	}

	@Override
	public VedantuResponse editCategory(EditCategoryReq editCategoryReq) {
		if (ObjectIdUtils.hasInvalidId(editCategoryReq.id)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		EditCategoryRes res = null;

		try {
			res = new EditCategoryRes();
			res.edited = editCategory(editCategoryReq.id,
					editCategoryReq.name, editCategoryReq.sectionIds);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(res);
	}

	public boolean editCategory(String id, String name, Set<String> sectionIds) throws VedantuException {

		Optional<Category> categoryOptional = categoryRepo.findById(id);
		if (!categoryOptional.isPresent()) {
			throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS);
		}
		Category category = categoryOptional.get();
		if (!StringUtils.isEmpty(name)) {
			category.setName(name);
		}
		category.sectionIds = sectionIds;
		categoryRepo.save(category);
		return true;
	}

	@Override
	public VedantuResponse removeCategory(RemoveCategoryReq removeCategoryReq) {

		if (ObjectIdUtils.hasInvalidId(removeCategoryReq.id)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		RemoveCategoryRes removeCategoryRes = null;

		try {
			removeCategoryRes = new RemoveCategoryRes();
			removeCategoryRes.deleted = removeCategory(removeCategoryReq.id);

		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(removeCategoryRes);

	}

	public boolean removeCategory(String id) throws VedantuException {
		Optional<Category> categoryOptional = categoryRepo.findById(id);
		if (!categoryOptional.isPresent()) {
			throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS);
		} else {
			Category category = categoryOptional.get();
			category.setRecordState(VedantuRecordState.DELETED);
			categoryRepo.delete(category);
		}
		return true;
	}

	@Override
	public VedantuResponse editCategories(@Valid EditCategoriesReq editCategoriesReq) {
		EditCategoryRes editCategoriesRes = null;

		try {
			editCategoriesRes = new EditCategoryRes();
			editCategoriesRes.edited = editCategoriesInfo(editCategoriesReq);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(editCategoriesRes);
	}

	public boolean editCategoriesInfo(EditCategoriesReq editCategoriesReq) throws VedantuException {

		List<EditCategoryInfo> categoryList = editCategoriesReq.categoryList;

		if (categoryList == null) {
			throw new VedantuException(VedantuErrorCode.CATEGORY_LIST_NOT_DEFINED);
		}

		for (EditCategoryInfo editCategoryInfo : categoryList) {
			String id = editCategoryInfo.id;
			if (id == null) {
				continue;
			}
			Optional<Category> categoryOptional = categoryRepo.findById(id);

			if (categoryOptional.isPresent()) {
				Category category = categoryOptional.get();
				if (!StringUtils.isEmpty(editCategoryInfo.name)) {
					category.setName(editCategoryInfo.name);
				}

				if (category.sectionIds == null) {
					category.sectionIds = new HashSet<String>();
				}

				if (editCategoryInfo.addedSectionIds != null) {
					category.sectionIds.addAll(editCategoryInfo.addedSectionIds);
				}

				if (editCategoryInfo.removedSectionIds != null) {

					category.sectionIds.removeAll(editCategoryInfo.removedSectionIds);
				}
				try {
					categoryRepo.save(category);
				} catch (DuplicateKeyException e) {
				}
			}

		}
		return true;
	}

	@Override
	public VedantuResponse getCategories(AbstractOrgScopeReq abstractOrgScopeReq) {
		if (ObjectIdUtils.hasInvalidId(abstractOrgScopeReq.orgId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);
		}
		GetCategoriesRes res = null;

		try {

			res = new GetCategoriesRes();
			List<Category> categories = getCategories(abstractOrgScopeReq.orgId);
			List<CategoryInfo> categoryInfos = toCategoryInfo(categories);
			res.list.addAll(categoryInfos);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(res);

	}

	public List<Category> getCategories(String orgId) {
		List<Category> categories = categoryRepo.findByOrgIdOrderByPriorityAsc(orgId);
		return categories;
	}

	public static List<CategoryInfo> toCategoryInfo(List<Category> categories) {

		List<CategoryInfo> categoryInfos = new ArrayList<CategoryInfo>();
		if (!CollectionUtils.isEmpty(categories)) {
			for (Category category : categories) {
				if (null == category) {
					continue;
				}
				CategoryInfo categoryInfo = new CategoryInfo(category._getStringId(), category.getName(),
						category.sectionIds, category.description, category.shortDescription, category.priority,
						category.banner, category.thumbnail);
				categoryInfos.add(categoryInfo);
			}
		}
		return categoryInfos;
	}

	@Override
	public VedantuResponse getCategory(GetCategoryReq getCategoryReq) {
		if (ObjectIdUtils.hasInvalidId(getCategoryReq.orgId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		GetCategoryRes res = null;

		try {
			res = new GetCategoryRes();
			res.category = getCategory(getCategoryReq.orgId, getCategoryReq.name);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(res);
	}

    public Category getCategory(String orgId, String name) throws VedantuException {

        Category category = categoryRepo.findByOrgIdAndName(orgId, name);
        if (category == null) {
            throw new VedantuException(VedantuErrorCode.CATEGORY_DOES_NOT_EXISTS,
                    "no category found with name: " + name + ", for orgId:" + orgId);
        }
        return category;
    }

    @Override
    public VedantuResponse customizeCategory(CustomizeCategoryReq customizeCategoryReq) {
        CustomizeCategoryRes response = null;

        try {
             response = new CustomizeCategoryRes();
            Optional<Category> categoryOptional = categoryRepo.findById(customizeCategoryReq.id);
            if (!categoryOptional.isPresent()) {
                throw new VedantuException(VedantuErrorCode.INVALID_CATEGORY_ID);
            }
            Category category = categoryOptional.get();
            if (category.priority == customizeCategoryReq.priority
                    || checkPriority(customizeCategoryReq.priority)) {
                if (customizeCategoryReq.thumbnail!=null && !customizeCategoryReq.thumbnail.contains("https://s3.amazonaws.com/")) {
                    customizeCategoryReq.thumbnail = getAWSFileUrl(customizeCategoryReq.iconUUID);
                }
                if (customizeCategoryReq.banner!=null && !customizeCategoryReq.banner.contains("https://s3.amazonaws.com/")) {
                    customizeCategoryReq.banner = getAWSFileUrl(customizeCategoryReq.bannerUUID);
                    // customizeCategoryReq.thumbnail = getAWSFileUrl(customizeCategoryReq.iconUUID);
                }

                category.description = customizeCategoryReq.description;
                category.shortDescription = customizeCategoryReq.shortDescription;
                category.priority = customizeCategoryReq.priority;
                category.thumbnail = customizeCategoryReq.thumbnail;
                category.banner = customizeCategoryReq.banner;

                categoryRepo.save(category);
                response.category = category;
                response.success = true;
            } else {
                response.category = category;
                response.success = false;
            }

        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(response);
    }

    public boolean checkPriority(int priority) {
        List<Category> categories = categoryRepo.findByPriority(priority);
        if (priority == 1 || priority == 2) {
            return categories.size() <= 0;
        }
        return true;
    }

    @Override
    public VedantuResponse getCategorySections(GetCategorySectionsReq req) {
        GetCategorySectionsRes getCategorySectionsRes = null;
        GetCategorySectionsRes res = new GetCategorySectionsRes();

        try {
            Set<String> sectionIds = new HashSet<String>();

            // Get all the orgIds that gave access to the current organization
            List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(req.orgId);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgs.add(granteeOrgProgram.providerOrgId);
            }

            if (StringUtils.isEmpty(req.id)) {

                List<String> orgIds = new ArrayList<String>();
                orgIds.add(req.orgId);
                if (grantedOrgs != null)
                    orgIds.addAll(grantedOrgs);
                List<Category> categories = new ArrayList<Category>();
                for (String orgId : orgIds) {
                    categories.addAll(categoryRepo.findByOrgIdOrderByPriorityAsc(orgId));
                }
                if (!CollectionUtils.isEmpty(categories)) {
                    for (Category category : categories) {
                        if (category.name.equals(req.name)) {
                            if (!CollectionUtils.isEmpty(category.sectionIds)) {
                                sectionIds.addAll(category.sectionIds);
                            }
                        }
                        if (req.name.equals("")) {
                            if (!CollectionUtils.isEmpty(category.sectionIds)) {
                                sectionIds.addAll(category.sectionIds);

                            }
                        }

                    }
                }

            } else {
                // Get by Id
                Category category = categoryRepo.findById(req.id).get();

                if (category.orgId.equals(req.orgId) || (grantedOrgs != null && grantedOrgs.contains(category.orgId))) {
                    if (!CollectionUtils.isEmpty(category.sectionIds)) {
                        sectionIds.addAll(category.sectionIds);
                    }
                }
            }

            if (req.excludeSubscribed) {
                OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(req.orgId, req.userId);
                if (orgMember == null) {
                    throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
                }

                if (!CollectionUtils.isEmpty(orgMember.mappings)) {
                    Set<String> excludeSections = new HashSet<String>();

                    for (OrgMemberMappingInfo exclude : orgMember.mappings) {
                        excludeSections.add(exclude.sectionId);
                    }
                    sectionIds.removeAll(excludeSections);
                }
            }

            if (CollectionUtils.isEmpty(sectionIds)) {
                return new VedantuResponse(res);
            }

            AccessScope scope = null;
            if (req.openOnly != null && req.openOnly == Boolean.TRUE) {
                scope = AccessScope.OPEN;
            } else {
                scope = req.scope;
            }

            List<String> orgIds = new ArrayList<String>();
            orgIds.add(req.orgId);
            if (grantedOrgs != null)
                orgIds.addAll(grantedOrgs);
            // private List<OrgSection> getAllSectionsByIds(List<String> asList, String
            // programId, List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel
            // revenueModel, VedantuRecordState recordState, int start, int size, AtomicLong
            // totalHits) {

            List<OrgSection> sections = organizationsImpl.getAllSectionsByIds(orgIds, null,
                    ObjectIdUtils.toObjectIds(new ArrayList<String>(sectionIds)), scope, null,
                    VedantuRecordState.ACTIVE, req.start, req.size, new AtomicLong());
            Set<String> programIds = new HashSet<String>();
            Set<String> centerIds = new HashSet<String>();
            res.totalHits = sections.size();

            List<OrgSection> filterdSections = new ArrayList<OrgSection>();
            for (OrgSection section : sections) {
                // only for current organization sections
                if (section.orgId.equals(req.orgId)) {
                    filterdSections.add(section);
                    programIds.add(section.programId);
                    centerIds.add(section.centerId);
                } else {
                    for (GranteeOrgProgram grantedOrg : granteeOrgPrograms) {
                        if (grantedOrg.programId.equals(section.programId)) {
                            filterdSections.add(section);
                            programIds.add(section.programId);
                            centerIds.add(section.centerId);
                            break;
                        }
                    }
                }

                /*
                 * If the org id of the section matches the org id, just add section to the
                 * filter sections and add its programids and cetids
                 *
                 * else
                 *
                 * checkthe orgid and programid comb is part of thelist if yes , add sectionId
                 * and programid,ccenterid.
                 */
            }
            sections = filterdSections;
            Map<String, OrgStructureBasicInfo> programInfoMap = getProgramInfoMap(programIds);

            Map<String, OrgStructureBasicInfo> centerInfoMap = getCenterInfoMap(centerIds);


            List<OrgProgram> programList = orgProgramRepo.findByIdInAndRecordState(programIds,
                    VedantuRecordState.ACTIVE);
            for (OrgProgram program : programList) {
                programInfoMap.put(program._getStringId(), toOrgStructureBasicInfo(program._getStringId(),
                        program.recordState, program.getcName(), program.code, EntityType.PROGRAM));
            }

            List<OrgCenter> orgCenterList = orgCenterRepo.findByIdInAndRecordState(centerIds,
                    VedantuRecordState.ACTIVE);
            for (OrgCenter orgCenter : orgCenterList) {
                centerInfoMap.put(orgCenter._getStringId(), toOrgStructureBasicInfo(orgCenter._getStringId(),
                        orgCenter.recordState, orgCenter.getcName(), orgCenter.code, EntityType.CENTER));
            }

            for (OrgSection section : sections) {
                GetCategorySectionRes catSection = new GetCategorySectionRes();
                catSection.programInfo = programInfoMap.get(section.programId);
                catSection.centerInfo = centerInfoMap.get(section.centerId);
                catSection.sectionInfo = toOrgSectionInfo(section);

                List<OrgMember> members = memberServiceImpl.getOrgMembers(req.orgId, OrgMemberProfile.STUDENT, null, catSection.programInfo.id, catSection.centerInfo.id,
                        catSection.sectionInfo.id, null, null, 0, 1, null, null, null, new AtomicLong());

                catSection.sectionInfo.memberCount = members.size();

                res.list.add(catSection);
            }
        } catch (VedantuException e) {
            throw e;
        }
        
          if(req.orgId.equals(learnpediaId)){ try { return new VedantuResponse( addCustomDescription(new
          VedantuResponse(getCategorySectionsRes).toString())); } catch (JSONException
          e) { e.printStackTrace(); } }
         
        return new VedantuResponse(res);
    }

    private Map<String, OrgStructureBasicInfo> getProgramInfoMap(Set<String> programIds) {
        List<OrgProgram> programList = orgProgramRepo.findByIdInAndRecordState(programIds,
                VedantuRecordState.ACTIVE);
        Map<String, OrgStructureBasicInfo> programInfoMap = new LinkedHashMap<>();
        for (OrgProgram program : programList) {
            programInfoMap.put(program._getStringId(), toOrgStructureBasicInfo(program._getStringId(),
                    program.recordState, program.getcName(), program.code, EntityType.PROGRAM));
        }
        return programInfoMap;
    }

    private Map<String, OrgStructureBasicInfo> getCenterInfoMap(Set<String> centerIds) {
        List<OrgCenter> orgCenterList = orgCenterRepo.findByIdInAndRecordState(centerIds,
                VedantuRecordState.ACTIVE);
        Map<String, OrgStructureBasicInfo> centerInfoMap = new LinkedHashMap<String, OrgStructureBasicInfo>();
        for (OrgCenter orgCenter : orgCenterList) {
            centerInfoMap.put(orgCenter._getStringId(), toOrgStructureBasicInfo(orgCenter._getStringId(),
                    orgCenter.recordState, orgCenter.getcName(), orgCenter.code, EntityType.CENTER));
        }
        return centerInfoMap;
    }

    private OrgSectionInfo toOrgSectionInfo(OrgSection section) {
        return new OrgSectionInfo(section._getStringId(), section.getName(), section.code,
                section.getRecordState(), section.getAccessScope(), section.getRevenueModel(), section.getDesc(),
                section.getCostRate(), section.getSize(), section.extSupported, section.getOrgId(),
                section.getStartingRates(), section.getPackagesMap(), section.getDescriptionPoints(), section.getThumbnail());
    }

    public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId) {

        List<GranteeOrgProgram> programs = granteeOrgProgramRepo.findBySubscriberOrgIdAndRecordState(providerOrgId,
                VedantuRecordState.ACTIVE);

        return programs;
    }

    private String addCustomDescription(String resultResponse) throws JSONException {
        JSONObject response = new JSONObject(resultResponse);
        JSONObject result = new JSONObject(resultResponse);
        result = result.getJSONObject("result");
        JSONArray list = new JSONArray();
        list = result.getJSONArray("list");
       /* for(int i = 0; i < list.length(); i++){
            String secId = list.getJSONObject(i).getJSONObject("sectionInfo").getString("id");
            JSONObject description = new JSONObject();
            description.put("desc", Play.application().configuration().getString("\""+secId+".desc"+"\""));
            description.put("firstPoint", Play.application().configuration().getString("\""+secId+".firstPoint"+"\""));
            description.put("secondPoint", Play.application().configuration().getString("\""+secId+".secondPoint"+"\""));
            description.put("thirdPoint", Play.application().configuration().getString("\""+secId+".thirdPoint"+"\""));
            description.put("fourthPoint", Play.application().configuration().getString("\""+secId+".fourthPoint"+"\""));
            description.put("imageUrl", Play.application().configuration().getString("\""+secId+".imageUrl"+"\""));
            response.getJSONObject("result").getJSONArray("list").getJSONObject(i).put("description", description);
        }*/
        return response.toString();
    }

    @Override
    public VedantuResponse getCategorySection(GetCategorySectionReq req) {
        GetCategorySectionRes response = null;

        try {
            response = new GetCategorySectionRes();
            response.isPartOf = false;

            Optional<OrgSection> sectionOptional = orgSectionRepo.findById(req.sectionId);
            if (!sectionOptional.isPresent()) {
                throw new VedantuException(VedantuErrorCode.INVALID_SECTION_ID);
            }
            OrgSection section = sectionOptional.get();
            updateOrgMemberExpiredMappings(req.orgId, req.userId);
            OrgMember member = orgMemberRepo.findByOrgIdAndUserId(req.orgId,
                    req.userId);

            if (member != null && !CollectionUtils.isEmpty(member.mappings)) {
                OrgMemberMappingInfo memberMapping = new OrgMemberMappingInfo(
                        section.programId, section.centerId,
                        section._getStringId(), null);
                response.isPartOf = member.mappings.contains(memberMapping);
            }

            // TODO change this by making simple call to db

            response.sectionInfo = new OrgSectionInfo(section._getStringId(), section.getName(), section.code,
                    section.recordState, section.accessScope, section.revenueModel, section.desc,
                    section.costRate, section.size, section.extSupported, section.orgId,
                    section.startingRates, section.packagesMap, section.descriptionPoints, section.thumbnail);
            OrgProgram program = orgProgramRepo.findById(section.programId).get();
            response.programInfo = toOrgStructureBasicInfo(program._getStringId(), program.recordState, program.getcName(), program.code, EntityType.PROGRAM);
            OrgCenter orgCenter = orgCenterRepo.findById(section.centerId).get();
            response.centerInfo = toOrgStructureBasicInfo(orgCenter._getStringId(), orgCenter.recordState, orgCenter.getcName(), orgCenter.code, EntityType.CENTER);


            GetOrgProgramCoursesReq courseRequest = new GetOrgProgramCoursesReq();
            courseRequest.orgId = req.orgId;
            courseRequest.programId = section.programId;
            //courseRequest.friendOrgIds = req.friendOrgIds;

            GetOrgProgramCoursesRes courseResponse = programServiceImpl.getProgramCoursesList(courseRequest);
            if (courseResponse != null
                    && !CollectionUtils.isEmpty(courseResponse.list)) {
                response.courseInfo.addAll(courseResponse.list);
            }

            String secId = req.sectionId;
            String temp = ""; //Play.application().configuration().getString("\""+secId+".desc"+"\"");
            response.isB2C = null != temp;
        } catch (VedantuException e) {
            throw e;
        }

        return new VedantuResponse(response);

    }

    private OrgStructureBasicInfo toOrgStructureBasicInfo(String id, VedantuRecordState recordState, String name, String code, EntityType type) {
        return new OrgStructureBasicInfo(id, recordState, name, code, type);

    }

    public void updateOrgMemberExpiredMappings(String orgId, String userId) {
        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);
        if (null == member) {
            return;
        }
        boolean modified = false;
        if (member.expiredMappings == null) {
            member.expiredMappings = new ArrayList<OrgMemberMappingInfo>();
            modified = true;
        }
        if (member.mappings != null && !member.mappings.isEmpty()) {
            Iterator<OrgMemberMappingInfo> mappingIterator = member.mappings.iterator();
            while (mappingIterator.hasNext()) {
                OrgMemberMappingInfo mapping = mappingIterator.next();
                if (mapping.endTime > 0 && mapping.endTime < System.currentTimeMillis()) {
                    // This mapping has expired. Add to expiredMappingsList
                    member.expiredMappings.add(mapping);
                    mappingIterator.remove();
                    modified = true;
                }
            }
        }
        if (modified) {
            orgMemberRepo.save(member);
        }
    }

    @Override
    public VedantuResponse getMemberCategorySections(GetSelfCategorySectionsReq getSelfCategorySectionsReq) {
        GetCategorySectionsRes getCategorySectionsRes = null;

        try {
            getCategorySectionsRes = getMemberCategorySectionsRes(getSelfCategorySectionsReq);
        } catch (VedantuException e) {
            throw e;
        }

	       /* if(getCategorySectionsReq.orgId.equals(Play.application().configuration().getString("learnpedia.id"))){
	            try {
	                return ok(addCustomDescription(getResultResponse(getCategorySectionsRes).toObjectNode().toString()));
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	        }*/
        return new VedantuResponse(getCategorySectionsRes);
    }

    public GetCategorySectionsRes getMemberCategorySectionsRes(
            GetSelfCategorySectionsReq req) {

        GetCategorySectionsRes response = new GetCategorySectionsRes();
        updateOrgMemberExpiredMappings(req.orgId, req.userId);
        OrgMember member = orgMemberRepo.findByOrgIdAndUserId(req.orgId, req.userId);
        // TODO change this by making simple call to db
        Set<String> programIds = new HashSet<String>();
        Set<String> centerIds = new HashSet<String>();
        Set<String> sectionIds = new HashSet<String>();
        if (member != null && !CollectionUtils.isEmpty(member.mappings)) {

            for (OrgMemberMappingInfo mapping : member.mappings) {
                programIds.add(mapping.programId);
                centerIds.add(mapping.centerId);
                sectionIds.add(mapping.sectionId);
            }
            response.totalHits = member.mappings.size();
        }

        Map<String, OrgStructureBasicInfo> programInfoMap = getProgramInfoMap(programIds);
        Map<String, OrgStructureBasicInfo> centerInfoMap = getCenterInfoMap(centerIds);
        //Map<String, OrgStructureBasicInfo> sectionInfoMap = getSectionInfoMap(sectionIds);

        if (member != null && !CollectionUtils.isEmpty(member.mappings)) {

            for (OrgMemberMappingInfo mapping : member.mappings) {
                GetCategorySectionRes catSection = new GetCategorySectionRes();
                catSection.programInfo = programInfoMap.get(mapping.programId);
                catSection.centerInfo = centerInfoMap.get(mapping.centerId);
                // TODO: optimise this API call
                Optional<OrgSection> sectionOptional = orgSectionRepo.findById(mapping.sectionId);
                if (sectionOptional.isPresent()) {
                    OrgSection section = sectionOptional.get();
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
               /* OrgMemberDAO.INSTANCE.getOrgMembers(req.orgId, OrgMemberProfile.STUDENT,
                        catSection.programInfo.id, catSection.centerInfo.id,
                        catSection.sectionInfo.id, null, null, 0, 1, totalHits);*/
                    catSection.sectionInfo.memberCount = 0;//totalHits.longValue();

                    response.list.add(catSection);
                }
            }
        }

        return response;

    }

    private Map<String, OrgStructureBasicInfo> getSectionInfoMap(Set<String> sectionIds) {
        List<OrgSection> orgSectionList = orgSectionRepo.findByIdInAndRecordState(sectionIds,
                VedantuRecordState.ACTIVE);
        Map<String, OrgStructureBasicInfo> orgSectionInfoMap = new LinkedHashMap<>();
        for (OrgSection orgSection : orgSectionList) {
            orgSectionInfoMap.put(orgSection._getStringId(), toOrgStructureBasicInfo(orgSection._getStringId(),
                    orgSection.recordState, orgSection.getcName(), orgSection.code, EntityType.SECTION));
        }
        return orgSectionInfoMap;
    }

    public String getAWSFileUrl(String imageNameWithExtension) {
        localFileSystemHandler.localFileSystemHandlerTempDirectory(true);
        String filePath = localFileSystemHandler.getFilePath("organization", imageNameWithExtension);
        File file = new File(filePath);
        // move to s3 public bucket and get s3 url

        try {
            s3Handler.store(file, bucketNametemp,

                    imageNameWithExtension, new HashMap<String, String>());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileUtils.deleteFile(imageNameWithExtension, file);
        String AWSFileUrl = "https://" + bucketNametemp + ".s3.amazonaws.com/"
                + imageNameWithExtension;
        return AWSFileUrl;
    }

}

package com.lms.service.serviceImpl;

import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.repo.BoardRepo;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.ProgramCategory;
import com.lms.models.OrgCenter;
import com.lms.models.OrgProgram;
import com.lms.pojo.*;
import com.lms.pojo.request.*;
import com.lms.pojo.responce.*;
import com.lms.repository.OrgCenterRepo;
import com.lms.repository.OrgProgramRepo;
import com.lms.service.ProgramService;
import com.mongodb.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import java.util.*;


@Service
public class ProgramServiceImpl implements ProgramService {
	@Autowired
	private OrgProgramRepo orgProgramRepo;
	@Autowired
	private GranteeOrgProgramRepo granteeOrgProgramRepo;
	@Autowired
	private OrgCenterRepo orgCenterRepo;
	@Autowired
	private BoardRepo boardRepo;

	@Override
	public VedantuResponse getPrograms(@Valid GetOrgProgramsReq getOrgProgramsReq) {
		if (ObjectIdUtils.hasInvalidId(getOrgProgramsReq.orgId, getOrgProgramsReq.departmentId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		GetOrgProgramsRes getOrgProgramsRes = null;

		try {
			List<OrgProgram> programs = getPrograms(getOrgProgramsReq.orgId,
					getOrgProgramsReq.departmentId);
			// totalHits.s    = programs.size();
			Long totalProgramHits = new Long(0L);
			// List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE
			//        .getGranteeOrgPrograms(getOrgProgramsReq.orgId, null, totalProgramHits);

			List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getOrgProgramsReq.orgId);
			for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
				programs.add(getProgramById(granteeOrgProgram.providerOrgId,
						granteeOrgProgram.programId)); // sending an empty string as
				// orgId
			}
			getOrgProgramsRes = new GetOrgProgramsRes();
			if (!CollectionUtils.isEmpty(programs)) {
				getOrgProgramsRes.totalHits = programs.size() + granteeOrgPrograms.size();
				for (OrgProgram program : programs) {
					if (null == program) {
						continue;
					}
					OrgProgramInfo programInfo = new OrgProgramInfo(program._getStringId(),
							program.getName(), program.code, program.recordState, program.periodEnd,
							program.isOffline, program.category, program.sharedProgramAccess);
					getOrgProgramsRes.list.add(programInfo);
				}
			}
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(getOrgProgramsRes);

	}

	public List<OrgProgram> getPrograms(String orgId, String departmentId) {

		List<OrgProgram> programs = orgProgramRepo.findByOrgIdOrDepartmentIdOrderByCName(orgId, departmentId);
		return programs;
	}

	public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId) {


		List<GranteeOrgProgram> programs = granteeOrgProgramRepo.findBySubscriberOrgIdAndRecordState(providerOrgId, VedantuRecordState.ACTIVE);


		return programs;
	}

	public OrgProgram getProgramById(String orgId, String programId)
			throws VedantuException {
		Optional<OrgProgram> orgProgram = orgProgramRepo.findById(programId);
		if (!(orgProgram.isPresent())) {
			String errorMsg = "cannot find orgProgram for _id: " + programId;
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND, errorMsg);
		}
		return orgProgram.get();
	}

	@Override
	public VedantuResponse getProgramInfo(GetProgramInfoReq getProgramInfoReq) {
		OrgProgramBasicInfo res = null;
		try {
			res = getOrgProgramBasicInfo(getProgramInfoReq.programId, true, true, null);
		} catch (VedantuException e) {
			throw e;
		}
		return new VedantuResponse(res);
	}

	public OrgProgramBasicInfo getOrgProgramBasicInfo(String programId,
													  boolean includeAllCenters, boolean addSectionInfo,
													  Collection<String> forSectionIds) throws VedantuException {

		OrgProgram program = getProgramById(null, programId);

		OrgProgramBasicInfo programBasicInfo = getOrgProgramBasicInfo(program,
				includeAllCenters, true, null);
		return programBasicInfo;
	}

	public OrgProgramBasicInfo getOrgProgramBasicInfo(
			OrgProgram orgProgram, boolean includeAllCenters,
			boolean addSectionInfo, Collection<String> forSectionIds) {

		//OrgProgramBasicInfo programInfo = (OrgProgramBasicInfo) orgProgram.toBasicInfo();
		OrgProgramBasicInfo programInfo = new OrgProgramBasicInfo(orgProgram.getId().toString(), orgProgram.recordState, orgProgram.getcName(), orgProgram.getCode(), EntityType.PROGRAM, orgProgram.getDepartmentId(), "", "", orgProgram.getCourseIds(), orgProgram.isOffline);
		if (!addSectionInfo) {
			return programInfo;
		}
		if (forSectionIds == null) {
			forSectionIds = new HashSet<String>();
		}

		/*Set<String> centerIds = new HashSet<String>(orgProgram._getCenterIds(
				forSectionIds, includeAllCenters));
		Map<String, OrgStructureBasicInfo> centerMap = OrgCenterDAO.INSTANCE
				.getBasicInfosByIds(centerIds);

		Map<String, OrgStructureBasicInfo> sectionMap = OrgSectionDAO.INSTANCE
				.getBasicInfosByIds(forSectionIds, true);
		for (OrgProgramCenterSections center : orgProgram.centersSections) {
			OrgStructureBasicInfo orgCenter = centerMap.get(center.centerId);
			if (orgCenter == null) {
				continue;
			}

			OrgProgramCenterBasicInfo centerInfo = programInfo
					._getOrAddProgramCenter(orgCenter);
			for (String sectionId : center.sectionIds) {
				OrgStructureBasicInfo orgSection = sectionMap.get(sectionId);
				if (orgSection == null) {
					continue;
				}
				centerInfo._getOrAddProgramSection(orgSection);
			}
		}*/
		return programInfo;
	}

	@Override
	public VedantuResponse addProgram(AddOrgProgramReq addOrgProgramReq) {
		if (ObjectIdUtils.hasInvalidId(addOrgProgramReq.orgId, addOrgProgramReq.departmentId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		AddOrgProgramRes addOrgProgramRes = null;

		try {
			OrgProgram orgProgram = addProgram(
					addOrgProgramReq.orgId, addOrgProgramReq.code,
					addOrgProgramReq.name, addOrgProgramReq.departmentId,
					addOrgProgramReq.description, addOrgProgramReq.periodStart,
					addOrgProgramReq.periodEnd, addOrgProgramReq.isOffline, addOrgProgramReq.sharedProgramAccess, addOrgProgramReq.category);

			addOrgProgramRes = new AddOrgProgramRes();
			addOrgProgramRes.id = orgProgram._getStringId();
			addOrgProgramRes.recordState = orgProgram.recordState;
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(addOrgProgramRes);

	}

	public OrgProgram addProgram(String orgId, String code, String name,
								 String departmentId, String description, long periodStart,
								 long periodEnd, boolean isOffline, boolean sharedProgramAccess, ProgramCategory category) throws VedantuException {

		OrgProgram orgProgram = orgProgramRepo.findByOrgIdAndDepartmentIdAndCodeOrderByCName(orgId, departmentId, code);
		try {
			if (null != orgProgram) {
				if (VedantuRecordState.ACTIVE == orgProgram.recordState) {

					throw new VedantuException(
							VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
				} else {

					orgProgram.setName(name);
					orgProgram.setRecordState(VedantuRecordState.ACTIVE);
					orgProgramRepo.save(orgProgram);
					return orgProgram;
				}
			}

			orgProgram = new OrgProgram(orgId, code, name, departmentId,
					description, periodStart, periodEnd, isOffline, sharedProgramAccess, category);
			orgProgramRepo.save(orgProgram);

		} catch (Exception exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}

		return orgProgram;
	}

	@Override
	public VedantuResponse updateProgram(UpdateOrgProgramReq updateOrgProgramReq) {
		if (ObjectIdUtils.hasInvalidId(updateOrgProgramReq.getOrgId(), updateOrgProgramReq.getDepartmentId(),
				updateOrgProgramReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		UpdateOrgProgramRes updateOrgProgramRes = null;

		try {
			OrgProgram orgProgram = updateProgram(
					updateOrgProgramReq.getOrgId(), updateOrgProgramReq.getProgramId(),
					updateOrgProgramReq.getCode(), updateOrgProgramReq.getName(),
					updateOrgProgramReq.getDepartmentId(),
					updateOrgProgramReq.getDescription(),
					updateOrgProgramReq.getPeriodStart(), updateOrgProgramReq.getPeriodEnd(),
					updateOrgProgramReq.isOffline, updateOrgProgramReq.category);

			updateOrgProgramRes = new UpdateOrgProgramRes();
			updateOrgProgramRes.id = orgProgram._getStringId();
			updateOrgProgramRes.recordState = orgProgram.recordState;

		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(updateOrgProgramRes);
	}

	public OrgProgram updateProgram(String orgId, String programId,
									String code, String name, String departmentId, String description,
									long periodStart, long periodEnd, boolean isOffline,
									ProgramCategory category) throws VedantuException {


		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			if (!(orgProgram.departmentId.equals(departmentId))) {

				throw new VedantuException(VedantuErrorCode.INVALID_ID);
			}

			orgProgram.code = code;
			orgProgram.setName(name);
			orgProgram.description = departmentId;
			orgProgram.periodStart = periodStart;
			orgProgram.periodEnd = periodEnd;
			orgProgram.isOffline = isOffline;
			orgProgram.category = category;
			orgProgramRepo.save(orgProgram);


		} catch (DuplicateKeyException exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	@Override
	public VedantuResponse removeProgram(StateChangeOrgProgramReq stateChangeOrgProgramReq) {

		if (ObjectIdUtils.hasInvalidId(stateChangeOrgProgramReq.orgId, stateChangeOrgProgramReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		AddOrgProgramRes addOrgProgramRes = null;

		try {
			OrgProgram orgProgram = stateChangeProgram(
					stateChangeOrgProgramReq.orgId, stateChangeOrgProgramReq.programId, VedantuRecordState.DELETED);
			addOrgProgramRes = new AddOrgProgramRes();
			addOrgProgramRes.id = orgProgram._getStringId();
			addOrgProgramRes.recordState = orgProgram.recordState;

		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(addOrgProgramRes);
	}

	@Override
	public VedantuResponse activateProgram(StateChangeOrgProgramReq stateChangeOrgProgramReq) {
		if (ObjectIdUtils
				.hasInvalidId(stateChangeOrgProgramReq.orgId, stateChangeOrgProgramReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		AddOrgProgramRes addOrgProgramRes = null;

		try {
			OrgProgram orgProgram = stateChangeProgram(
					stateChangeOrgProgramReq.orgId, stateChangeOrgProgramReq.programId, VedantuRecordState.ACTIVE);
			addOrgProgramRes = new AddOrgProgramRes();
			addOrgProgramRes.id = orgProgram._getStringId();
			addOrgProgramRes.recordState = orgProgram.recordState;
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(addOrgProgramRes);
	}

	public OrgProgram stateChangeProgram(String orgId, String programId, VedantuRecordState vedantuRecordState)
			throws VedantuException {
		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			orgProgram.setRecordState(vedantuRecordState);
			orgProgramRepo.save(orgProgram);


		} catch (DuplicateKeyException exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	@Override
	public VedantuResponse getProgramCenters(GetOrgProgramCentersReq getOrgProgramCentersReq) {
		if (ObjectIdUtils.hasInvalidId(getOrgProgramCentersReq.getOrgId(),
				getOrgProgramCentersReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		GetOrgProgramCentersRes getOrgProgramCentersRes = null;

		try {
			//Long totalProgramHits = new Long(0L);
			List<GranteeOrgProgram> granteeOrgPrograms = granteeOrgProgramRepo.findBySubscriberOrgIdAndRecordState(getOrgProgramCentersReq.getOrgId(), VedantuRecordState.ACTIVE);
			List<String> allOrgIds = new ArrayList<String>();

			for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
				allOrgIds.add(granteeOrgProgram.providerOrgId);
			}
			allOrgIds.add(getOrgProgramCentersReq.orgId);

			OrgProgram program = getProgramById(
					null,
					getOrgProgramCentersReq.getProgramId());

			getOrgProgramCentersRes = new GetOrgProgramCentersRes();
			if (null != program
					&& !CollectionUtils.isEmpty(program.centersSections)) {
				//List<ObjectId> centerIds = program._getCentersAsObjectIds();
				List<String> centerIds = program._getCenterIds(new ArrayList<>(), false);
				//List<OrgCenter> orgCenters = OrgCenterDAO.INSTANCE
				//	.getCentersByIds(centerIds);
				List<OrgCenter> orgCenters = orgCenterRepo.findByIdIn(centerIds);
				List<OrgCenterInfo> centerInfos = toOrgCenterInfo(orgCenters);

				getOrgProgramCentersRes.list.addAll(centerInfos);
				getOrgProgramCentersRes.totalHits = centerInfos.size();
			}
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(getOrgProgramCentersRes);
	}

	public static List<OrgCenterInfo> toOrgCenterInfo(List<OrgCenter> centers) {
		List<OrgCenterInfo> centerInfos = new ArrayList<OrgCenterInfo>();
		if (!CollectionUtils.isEmpty(centers)) {
			for (OrgCenter center : centers) {
				if (null == center) {
					continue;
				}
				OrgCenterInfo centerInfo = new OrgCenterInfo(
						center._getStringId(), center.getName(), center.code,
						center.recordState);
				centerInfos.add(centerInfo);
			}
			//Collections.sort(centerInfos,OrgStructureInfoNameComparator.INSTANCE);
			centerInfos.sort((OrgCenterInfo orgCenterInfo1, OrgCenterInfo orgCenterInfo2) -> orgCenterInfo1.getName().compareTo(orgCenterInfo2.getName()));

		}
		return centerInfos;
	}

	@Override
	public VedantuResponse addProgramCenters(AddOrgProgramCentersReq addOrgProgramCentersReq) {

		if (ObjectIdUtils.hasInvalidId(addOrgProgramCentersReq.orgId,
				addOrgProgramCentersReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		AddOrgProgramCentersRes addOrgProgramCentersRes = null;

		try {
			Set<String> tCenterIds = null == addOrgProgramCentersReq.centerIds ? null
					: new HashSet<String>(addOrgProgramCentersReq.centerIds);
			OrgProgramCenterSectionsDetails details =
					addProgramCenter(addOrgProgramCentersReq.orgId,
							addOrgProgramCentersReq.programId, tCenterIds);

			addOrgProgramCentersRes = new AddOrgProgramCentersRes();
			addOrgProgramCentersRes.id = details.programId;
			addOrgProgramCentersRes.recordState = details.recordState;
			addOrgProgramCentersRes.centerSections = details.centerSections;
			addOrgProgramCentersRes.isAdded = details.isAdded;
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(addOrgProgramCentersRes);

	}

	public OrgProgramCenterSectionsDetails addProgramCenter(String orgId,
															String programId, Set<String> centerIds) throws VedantuException {


		OrgProgram orgProgram = getProgramById(orgId, programId);
		OrgProgramCenterSectionsDetails result = new OrgProgramCenterSectionsDetails();
		result.programId = programId;
		result.recordState = orgProgram.recordState;
		try {

			for (String centerId : centerIds) {
				OrgProgramCenterSections center = new OrgProgramCenterSections(
						centerId);
				for (OrgProgramCenterSections tCenter : orgProgram.centersSections) {
					if (center.equals(tCenter)) {
						throw new VedantuException(
								VedantuErrorCode.ORGANIZATION_PROGRAM_CENTER_ALREADY_EXISTS);
					}
				}
				orgProgram.centersSections.add(center);
				result.centerSections.add(center);


			}

			orgProgramRepo.save(orgProgram);
			result.isAdded = true;


		} catch (DuplicateKeyException exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return result;
	}

	@Override
	public VedantuResponse removeProgramCenters(AddOrgProgramCentersReq addOrgProgramCentersReq) {
		if (ObjectIdUtils.hasInvalidId(addOrgProgramCentersReq.orgId,
				addOrgProgramCentersReq.programId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}
		RemoveOrgProgramCentersRes removeOrgProgramCentersRes = null;

		try {
			Set<String> tCenterIds = null == addOrgProgramCentersReq.centerIds ? null
					: new HashSet<String>(addOrgProgramCentersReq.centerIds);
			OrgProgramCenterSectionsDetails details =
					removeProgramCenter(addOrgProgramCentersReq.orgId,
							addOrgProgramCentersReq.programId, tCenterIds);

			removeOrgProgramCentersRes = new RemoveOrgProgramCentersRes();
			removeOrgProgramCentersRes.id = details.programId;
			removeOrgProgramCentersRes.recordState = details.recordState;
			removeOrgProgramCentersRes.centerSections = details.centerSections;
			removeOrgProgramCentersRes.isRemoved = !details.isAdded;
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(removeOrgProgramCentersRes);

	}

	public OrgProgramCenterSectionsDetails removeProgramCenter(String orgId,
															   String programId, Set<String> centerIds) throws VedantuException {

		OrgProgram orgProgram = getProgramById(orgId, programId);

		OrgProgramCenterSectionsDetails result = new OrgProgramCenterSectionsDetails();
		result.programId = programId;
		result.recordState = orgProgram.recordState;
		try {
			for (String centerId : centerIds) {

				OrgProgramCenterSections center = new OrgProgramCenterSections(
						centerId);
				boolean found = false;
				for (OrgProgramCenterSections tCenter : orgProgram.centersSections) {
					if (center.equals(tCenter)) {
						center = tCenter;
						found = true;
						break;
					}
				}

				if (!found) {
					throw new VedantuException(
							VedantuErrorCode.ORGANIZATION_PROGRAM_CENTER_NOT_FOUND);
				}

				orgProgram.centersSections.remove(center);
				result.centerSections.add(center);


			}

			orgProgramRepo.save(orgProgram);
			result.isAdded = false;


		} catch (DuplicateKeyException exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return result;
	}

	@Override
	public VedantuResponse getProgramCourses(GetOrgProgramCoursesReq getOrgProgramCoursesReq) {
		GetOrgProgramCoursesRes getOrgProgramCoursesRes = null;

		try {
			getOrgProgramCoursesRes = getProgramCoursesList(getOrgProgramCoursesReq);
			getOrgProgramCoursesRes.totalHits = getOrgProgramCoursesRes.list.size();

		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(getOrgProgramCoursesRes);

	}

	public GetOrgProgramCoursesRes getProgramCoursesList(GetOrgProgramCoursesReq getOrgProgramCoursesReq) {
		//Get all the orgIds that gave access to the current organizations programs
		List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getOrgProgramCoursesReq.orgId);
		List<String> grantedOrgs = new ArrayList<String>();

		for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
			grantedOrgs.add(granteeOrgProgram.providerOrgId);
		}


		grantedOrgs.add(getOrgProgramCoursesReq.orgId);
		Set<BoardBasicInfo> courseBoardInfos =
				getProgramCourses(grantedOrgs, getOrgProgramCoursesReq.programId);
		GetOrgProgramCoursesRes getOrgProgramCoursesRes = new GetOrgProgramCoursesRes();
		getOrgProgramCoursesRes.list = new ArrayList<BoardBasicInfo>(
				courseBoardInfos);
		Collections.sort(getOrgProgramCoursesRes.list, BoardBasicInfo.COMPARATOR);
		return getOrgProgramCoursesRes;
	}

	public Set<BoardBasicInfo> getProgramCourses(List<String> orgIds,
												 String programId) throws VedantuException {

		OrgProgram orgProgram = getProgramById(null, programId);

		Map<String, BoardBasicInfo> courseInfos =
				getBasicInfosByIds(orgProgram.courseIds);


		Set<BoardBasicInfo> courseBoardInfos = new HashSet<BoardBasicInfo>();
		if (null != courseInfos) {
			courseBoardInfos.addAll(courseInfos.values());
		}


		return courseBoardInfos;
	}

	public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {

		List<Board> results = boardRepo.findByIdIn(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids), true));
		Map<String, BoardBasicInfo> basicInfoMap = toBasicInfosMap(results);

		return basicInfoMap;
	}

	private Map<String, BoardBasicInfo> toBasicInfosMap(List<Board> results) {
		Map<String, BoardBasicInfo> infosMap = new LinkedHashMap<String, BoardBasicInfo>();
		if (!CollectionUtils.isEmpty(results)) {
			results.forEach(board -> {
				infosMap.put(board._getStringId(), new BoardBasicInfo(board._getStringId(), board.recordState, board.name, board.code, board.type, board.treeName, board.grades, board.parentBrdIds, board.year));
			});
		}
		return infosMap;
	}

	@Override
	public VedantuResponse addProgramCourses(OrgProgramCoursesReq addOrgProgramCoursesReq) {
		String[] tCourseIds1 = addOrgProgramCoursesReq.courseIds.toArray(new String[0]);
		if (ObjectIdUtils.hasInvalidId(addOrgProgramCoursesReq.orgId,
				addOrgProgramCoursesReq.programId) || ObjectIdUtils.hasInvalidId(tCourseIds1)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}

		AddOrgProgramCoursesRes addOrgProgramCoursesRes = null;

		try {
			Set<String> tCourseIds = null == addOrgProgramCoursesReq.courseIds ? null
					: new HashSet<String>(addOrgProgramCoursesReq.courseIds);

			OrgProgram program = addProgramCourses(
					addOrgProgramCoursesReq.orgId,
					addOrgProgramCoursesReq.programId, tCourseIds);

			addOrgProgramCoursesRes = new AddOrgProgramCoursesRes();
			addOrgProgramCoursesRes.id = program._getStringId();
			addOrgProgramCoursesRes.recordState = program.recordState;
			addOrgProgramCoursesRes.courseIds = program.courseIds;
			addOrgProgramCoursesRes.isAdded = true;

		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(addOrgProgramCoursesRes);

	}

	public OrgProgram addProgramCourses(String orgId, String programId, Collection<String> courseIds)
			throws VedantuException {

		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			boolean added = orgProgram.addCourses(courseIds);
			if (added) {
				orgProgramRepo.save(orgProgram);
			}

		} catch (DuplicateKeyException exception) {

			throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	@Override
	public VedantuResponse removeProgramCourses(OrgProgramCoursesReq removeOrgProgramCoursesReq) {
		String[] tCourseIds1 = removeOrgProgramCoursesReq.courseIds.toArray(new String[0]);
		if (ObjectIdUtils.hasInvalidId(removeOrgProgramCoursesReq.orgId, removeOrgProgramCoursesReq.programId)
				|| ObjectIdUtils.hasInvalidId(tCourseIds1)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}

		RemoveOrgProgramCoursesRes removeOrgProgramCoursesRes = null;

		try {
			Set<String> tCourseIds = null == removeOrgProgramCoursesReq.courseIds ? null
					: new HashSet<String>(removeOrgProgramCoursesReq.courseIds);

			OrgProgram program = removeProgramCourses(removeOrgProgramCoursesReq.orgId,
					removeOrgProgramCoursesReq.programId, tCourseIds);

			removeOrgProgramCoursesRes = new RemoveOrgProgramCoursesRes();
			removeOrgProgramCoursesRes.id = program._getStringId();
			removeOrgProgramCoursesRes.recordState = program.recordState;
			removeOrgProgramCoursesRes.courseIds = program.courseIds;
			removeOrgProgramCoursesRes.isRemoved = true;

		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(removeOrgProgramCoursesRes);

	}

	public OrgProgram removeProgramCourses(String orgId, String programId, Collection<String> courseIds)
			throws VedantuException {

		OrgProgram orgProgram = getProgramById(orgId, programId);

		boolean removed = orgProgram.removeCourses(courseIds);

		if (removed) {
			orgProgramRepo.save(orgProgram);
		}

		return orgProgram;
	}

	@Override
	public VedantuResponse getCoursePrograms(GetOrgCourseProgramsReq getOrgCourseProgramsReq) {
		if (ObjectIdUtils.hasInvalidId(getOrgCourseProgramsReq.orgId, getOrgCourseProgramsReq.courseId)) {
			throw new VedantuException(VedantuErrorCode.INVALID_ID);

		}

		GetOrgCourseProgramsRes getOrgCourseProgramsRes = null;

		try {
			List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getOrgCourseProgramsReq.orgId);
			List<String> grantedOrgs = new ArrayList<String>();
			for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
				grantedOrgs.add(granteeOrgProgram.providerOrgId);
			}

			grantedOrgs.add(getOrgCourseProgramsReq.orgId);

			List<OrgStructureBasicInfo> programBasicInfos = getCoursePrograms(grantedOrgs,
					getOrgCourseProgramsReq.courseId);

			getOrgCourseProgramsRes = new GetOrgCourseProgramsRes();
			getOrgCourseProgramsRes.list = programBasicInfos;
			getOrgCourseProgramsRes.totalHits = programBasicInfos.size();
		} catch (VedantuException e) {
			throw e;
		}

		return new VedantuResponse(getOrgCourseProgramsRes);

	}

	public List<OrgStructureBasicInfo> getCoursePrograms(List<String> orgIds, String courseId) throws VedantuException {
		List<OrgProgram> orgPrograms = orgProgramRepo.findByOrgIdInAndCourseIdsOrderByCName(orgIds, courseId);
		List<OrgStructureBasicInfo> orgProBasicInfos = toBasicInfos(orgPrograms);
		return orgProBasicInfos;
	}

	private List<OrgStructureBasicInfo> toBasicInfos(List<OrgProgram> orgPrograms) {
		List<OrgStructureBasicInfo> basicInfos = new ArrayList<>();
		if (orgPrograms != null && !orgPrograms.isEmpty()) {
			orgPrograms.forEach(orgProgram -> {
				basicInfos.add(new OrgStructureBasicInfo(orgProgram._getStringId(), orgProgram.getRecordState(),
						orgProgram.getcName(), orgProgram.getCode(), EntityType.PROGRAM));
			});
		}
		return basicInfos;
	}


}

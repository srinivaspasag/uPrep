package com.vedantu.organization.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.ProgramCategory;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.pojos.OrgProgramCenterSections;
import com.vedantu.organization.pojos.OrgProgramCenterSectionsDetails;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class OrgProgramDAO extends VedantuBasicDAO<OrgProgram, ObjectId> {

	private static final ALogger LOGGER = Logger.of(OrgProgramDAO.class);

	public static final OrgProgramDAO INSTANCE = new OrgProgramDAO();

	private OrgProgramDAO() {
		super(OrgProgram.class);
	}

	public List<OrgProgram> getPrograms(String orgId, String departmentId,
			MutableLong totalHits) {
		LOGGER.debug("getPrograms orgId: " + orgId + ", departmentId: "
				+ departmentId);

		Query<OrgProgram> query = getQuery().filter("orgId", orgId);
		query.order("cName");
		if (null != departmentId) {
			query.filter("departmentId", departmentId);
		}
		LOGGER.debug("getPrograms query: " + query);

		List<OrgProgram> programs = query.asList();
		totalHits.setValue(query.countAll());

		LOGGER.info("getPrograms totalHits: " + totalHits.getValue());

		return programs;
	}
	// TODO : Added a method

	public List<OrgProgram> getFriendOrgPrograms(List<String> orgIds, String departmentId,
			MutableLong totalHits) {
		LOGGER.debug("getPrograms orgId: " + orgIds + ", departmentId: "
				+ departmentId);

		// TODO : Also should add a filter to check if proggram has access to the org.

		Query<OrgProgram> query = getQuery().filter("orgId", orgIds);
		query.order("cName");
		if (null != departmentId) {
			query.filter("departmentId", departmentId);
		}
		LOGGER.debug("getFriendPrograms query: " + query);

		List<OrgProgram> programs = query.asList();
		totalHits.setValue(query.countAll());

		LOGGER.info("getFriendOrgPrograms totalHits: " + totalHits.getValue());

		return programs;
	}

	public OrgProgram addProgram(String orgId, String code, String name,
			String departmentId, String description, long periodStart,
			long periodEnd, boolean isOffline, boolean sharedProgramAccess , ProgramCategory category) throws VedantuException {

		LOGGER.debug("addProgram orgId: " + orgId + ", code: " + code
				+ ", name: " + name + ", departmentId: " + departmentId
				+ ", description: " + description + ", periodStart: "
				+ periodStart + ", periodEnd: " + periodEnd);

		OrgProgram orgProgram = getQuery().filter("orgId", orgId)
				.filter("departmentId", departmentId).filter("code", code)
				.order("cName").get();
		try {
			if (null != orgProgram) {
				if (VedantuRecordState.ACTIVE == orgProgram.recordState) {
					LOGGER.error("cannot add orgProgram as orgProgram already exists for orgId: "
							+ orgId
							+ ", departmentId: "
							+ departmentId
							+ ", code: " + code);
					throw new VedantuException(
							VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
				} else {
					LOGGER.error("changing orgProgram recordState for orgId: "
							+ orgId + ", departmentId: " + departmentId
							+ ", code: " + code + ", _id: "
							+ orgProgram._getStringId() + ", from: "
							+ orgProgram.recordState + ", to: "
							+ VedantuRecordState.ACTIVE);
					orgProgram.setName(name);
					markActive(orgProgram);
					save(orgProgram);
					return orgProgram;
				}
			}

			orgProgram = new OrgProgram(orgId, code, name, departmentId,
					description, periodStart, periodEnd, isOffline, sharedProgramAccess , category);
			save(orgProgram);
			LOGGER.info("addProgram orgProgram: " + orgProgram);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}

		return orgProgram;
	}

	public OrgProgram getProgramById(String orgId, String programId)
			throws VedantuException {
		LOGGER.debug("getProgramById orgId: " + orgId + ", programId: "
				+ programId);
		return getProgramById(Arrays.asList(orgId), programId);
	}
		public OrgProgram getProgramById(List<String> orgIds, String programId)
				throws VedantuException {

		OrgProgram orgProgram = getById(programId);
		if (null == orgProgram) {
			String errorMsg = "cannot find orgProgram for _id: " + programId;
			LOGGER.error(errorMsg);
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND, errorMsg);
		}

		//if (!StringUtils.equals(orgProgram.orgId, orgId)) {
		// if(!orgIds.contains(orgProgram.orgId))	{
		// String errorMsg = "mismatch in orgId for program _id: " + programId
		// 			+ ", expected orgId: " + orgProgram.orgId
		// 			+ ", found orgIds: " + orgIds;
		// 	LOGGER.error(errorMsg);
		// 	throw new VedantuException(VedantuErrorCode.INVALID_ID, errorMsg);
		// }

		LOGGER.info("getProgramById orgProgram: " + orgProgram);

		return orgProgram;
	}

	public OrgProgram updateProgram(String orgId, String programId,
			String code, String name, String departmentId, String description,
			long periodStart, long periodEnd, boolean isOffline,
			ProgramCategory category) throws VedantuException {
		LOGGER.debug("updateProgram orgId: " + orgId + ", programId: "
				+ programId + ", code: " + code + ", name: " + name
				+ ", departmentId: " + departmentId + ", description: "
				+ description + ", periodStart: " + periodStart
				+ ", periodEnd: " + periodEnd);

		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			if (!StringUtils.equals(orgProgram.departmentId, departmentId)) {
				LOGGER.error("mismatch in departmentId for program _id: "
						+ programId + ", expected departmentId: "
						+ orgProgram.departmentId + ", found departmentId: "
						+ departmentId);
				throw new VedantuException(VedantuErrorCode.INVALID_ID);
			}

			orgProgram.code = code;
			orgProgram.setName(name);
			orgProgram.description = departmentId;
			orgProgram.periodStart = periodStart;
			orgProgram.periodEnd = periodEnd;
			orgProgram.isOffline = isOffline;
			orgProgram.category = category;
			save(orgProgram);

			LOGGER.info("updateProgram orgProgram: " + orgProgram);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	public OrgProgram removeProgram(String orgId, String programId)
			throws VedantuException {
		LOGGER.debug("removeProgram orgId: " + orgId + ", programId: "
				+ programId);

		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			markDeleted(orgProgram);
			save(orgProgram);

			LOGGER.info("removeProgram orgProgram: " + orgProgram);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	public OrgProgram activateProgram(String orgId, String programId)
			throws VedantuException {
		LOGGER.debug("activateProgram orgId: " + orgId + ", programId: "
				+ programId);

		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			markActive(orgProgram);
			save(orgProgram);

			LOGGER.info("activateProgram orgProgram: " + orgProgram);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	public OrgProgramCenterSectionsDetails addProgramCenter(String orgId,
			String programId, Set<String> centerIds) throws VedantuException {

		LOGGER.debug("addProgramCenter orgId: " + orgId + ", programId: "
				+ programId + ", centerIds: {"
				+ StringUtils.join(centerIds, ", ") + "}");

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

				LOGGER.info("addProgramCenter added center: " + center);
			}

			save(orgProgram);
			result.isAdded = true;

			LOGGER.info("addProgramCenter result: " + result);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return result;
	}

	public OrgProgramCenterSectionsDetails removeProgramCenter(String orgId,
			String programId, Set<String> centerIds) throws VedantuException {

		LOGGER.debug("removeProgramCenter orgId: " + orgId + ", programId: "
				+ programId + ", centerIds: {"
				+ StringUtils.join(centerIds, ", ") + "}");

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

				LOGGER.info("removeProgramCenter removed center: " + center);
			}

			save(orgProgram);
			result.isAdded = false;

			LOGGER.info("removeProgramCenter result: " + result);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return result;
	}

	public boolean addCenterSection(String orgId, String programId,
			String centerId, String sectionId) throws VedantuException {

		LOGGER.debug("addCenterSection orgId: " + orgId + ", programId: "
				+ programId + ", centerId: " + centerId + ", sectionId: "
				+ sectionId);

		OrgProgram orgProgram = getProgramById(orgId, programId);
		OrgProgramCenterSections centerSections = orgProgram
				._getOrgProgramCenterSections(centerId);
		try {
			if (null == centerSections) {
				centerSections = new OrgProgramCenterSections(centerId);
				orgProgram.centersSections.add(centerSections);
				LOGGER.debug("addCenterSection added center to programId: "
						+ programId + "for centerId: " + centerId);
			}
			if (centerSections.sectionIds.contains(sectionId)) {
				LOGGER.debug("addCenterSection program already contains sectionId: "
						+ sectionId);
				return false;
			}
			centerSections.sectionIds.add(sectionId);
			save(orgProgram);

			LOGGER.info("addCenterSection added sectionId: " + sectionId);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return true;
	}

	public boolean removeCenterSection(String orgId, String programId,
			String centerId, String sectionId) throws VedantuException {

		LOGGER.debug("removeCenterSection orgId: " + orgId + ", programId: "
				+ programId + ", centerId: " + centerId + ", sectionId: "
				+ sectionId);

		OrgProgram orgProgram = getProgramById(orgId, programId);
		OrgProgramCenterSections centerSections = orgProgram
				._getOrgProgramCenterSections(centerId);
		if (null == centerSections) {
			LOGGER.debug("removeCenterSection no center found in programId: "
					+ programId + " for centerId: " + centerId);
			return false;
		}
		if (centerSections.sectionIds.contains(sectionId)) {
			centerSections.sectionIds.remove(sectionId);
			save(orgProgram);
			LOGGER.info("removeCenterSection removed section from programId: "
					+ programId + " for sectionId: " + sectionId);
			return true;
		}

		LOGGER.info("removeCenterSection section not found in programId: "
				+ programId + " for sectionId: " + sectionId);
		return false;
	}

	public Map<String, OrgStructureBasicInfo> getBasicInfosByIds(Set<String> ids) {
		List<OrgProgram> results = getByIds(ObjectIdUtils
				.toObjectIds(new ArrayList<String>(ids)));
		Map<String, OrgStructureBasicInfo> basicInfoMap = toBasicInfosMap(results);
		return basicInfoMap;
	}

    public Map<String, OrgProgram> getProgramsMapByIds(Set<String> programIds) {
        List<OrgProgram> results = getByIds(ObjectIdUtils.toObjectIds(new ArrayList<String>(
                programIds)));
        Map<String, OrgProgram> programsMap = new HashMap<String, OrgProgram>();
        for(OrgProgram result : results){
            programsMap.put(result._getStringId(), result);
        }
        return programsMap;
    }

	public OrgProgram addProgramCourses(String orgId, String programId,
			Collection<String> courseIds, MutableBoolean isAdded)
			throws VedantuException {

		LOGGER.debug("addProgramCourse orgId: " + orgId + ", programId: "
				+ programId + ", courseIds: {"
				+ StringUtils.join(courseIds, ", ") + "}");

		OrgProgram orgProgram = getProgramById(orgId, programId);
		try {
			boolean added = orgProgram.addCourses(courseIds);
			isAdded.setValue(added);

			LOGGER.debug("isAdded: " + isAdded.getValue());
			if (isAdded.getValue()) {
				save(orgProgram);
			}

			LOGGER.debug("addProgramCourse isAdded: " + isAdded);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_ALREADY_EXISTS);
		}
		return orgProgram;
	}

	public OrgProgram removeProgramCourses(String orgId, String programId,
			Collection<String> courseIds, MutableBoolean isRemoved)
			throws VedantuException {

		LOGGER.debug("removeProgramCourse orgId: " + orgId + ", programId: "
				+ programId + ", courseIds: {"
				+ StringUtils.join(courseIds, ", ") + "}");

		OrgProgram orgProgram = getProgramById(orgId, programId);

		boolean removed = orgProgram.removeCourses(courseIds);
		isRemoved.setValue(removed);

		LOGGER.debug("isRemoved: " + isRemoved.getValue());
		if (isRemoved.getValue()) {
			save(orgProgram);
		}

		LOGGER.debug("removeProgramCourse isRemoved: " + isRemoved);

		return orgProgram;
	}

	public Set<BoardBasicInfo> getProgramCourses(String orgId, String programId)
			throws VedantuException {

		LOGGER.debug("getProgramCourses orgId: " + orgId + ", programId: "
				+ programId);
		return getProgramCourses(Arrays.asList(orgId), programId);
	}

	public Set<BoardBasicInfo> getProgramCourses(List<String> orgIds,
			String programId) throws VedantuException {

		OrgProgram orgProgram = getProgramById(orgIds, programId);

		Map<String, BoardBasicInfo> courseInfos = BoardDAO.INSTANCE
				.getBasicInfosByIds(orgProgram.courseIds);

		LOGGER.debug("getProgramCourses courseInfos: {"
				+ StringUtils.join(courseInfos, ", ") + "}");

		Set<BoardBasicInfo> courseBoardInfos = new HashSet<BoardBasicInfo>();
		if (null != courseInfos) {
			courseBoardInfos.addAll(courseInfos.values());
		}

		LOGGER.info("getProgramCourses courseBoardInfos: {"
				+ StringUtils.join(courseBoardInfos, ",\n") + "}");

		return courseBoardInfos;
	}

	public List<OrgStructureBasicInfo> getCoursePrograms(String orgId,
			String courseId) throws VedantuException {
		LOGGER.debug("getCoursePrograms orgId: " + orgId + ", courseId: "
				+ courseId);
		return getCoursePrograms(orgId, courseId);
	}

	public List<OrgStructureBasicInfo> getCoursePrograms(List<String> orgIds,
			String courseId) throws VedantuException {

		LOGGER.debug("getCoursePrograms orgId: " + orgIds + ", courseId: "
				+ courseId);


		Query<OrgProgram> query = getQuery().field("orgId").hasAnyOf(orgIds)
				.filter("courseIds", courseId).order("cName");
		LOGGER.debug("getCoursePrograms query: " + query);

		List<OrgProgram> orgPrograms = query.asList();
		List<OrgStructureBasicInfo> orgProBasicInfos = toBasicInfos(orgPrograms);

		LOGGER.info("getCoursePrograms orgProBasicInfos: {"
				+ StringUtils.join(orgProBasicInfos, ",\n") + "}");

		return orgProBasicInfos;
	}

}

package com.vedantu.organization.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.pojos.InvoiceInfo;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.models.GranteeOrgProgram;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.models.OrgCenter;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.OrgProgramCenterBasicInfo;
import com.vedantu.organization.pojos.OrgProgramCenterSections;
import com.vedantu.organization.pojos.OrgProgramCenterSectionsDetails;
import com.vedantu.organization.pojos.OrgProgramSectionBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgCourseProgramsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgProgramsReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionsReq;
import com.vedantu.organization.pojos.requests.organizations.GetPaymentInfoReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramCentersReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramCoursesReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgProgramReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgProgramReq;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgCourseProgramsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgProgramsRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionsRes;
import com.vedantu.organization.pojos.responses.organizations.GetPaymentInfoRes;
import com.vedantu.organization.pojos.responses.organizations.OrgCenterInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgProgramInfo;
import com.vedantu.organization.pojos.responses.organizations.OrgSectionInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramCentersRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramCoursesRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgProgramRes;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgProgramRes;

public class OrgProgramManager extends AbstractVedantuManager {

	public static final ALogger LOGGER = Logger.of(OrgProgramManager.class);

    public static GetOrgProgramsRes getPrograms(GetOrgProgramsReq getOrgProgramsReq)
            throws VedantuException {

        MutableLong totalHits = new MutableLong(0L);
        List<OrgProgram> programs = OrgProgramDAO.INSTANCE.getPrograms(getOrgProgramsReq.orgId,
                getOrgProgramsReq.departmentId, totalHits);
        // TODO : add friend org programs
        MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE
                .getGranteeOrgPrograms(getOrgProgramsReq.orgId, null, totalProgramHits);
        LOGGER.debug("OrgProgramManager getPrograms" + granteeOrgPrograms + " size="
                + granteeOrgPrograms.size());

        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            programs.add(OrgProgramDAO.INSTANCE.getProgramById(granteeOrgProgram.providerOrgId,
                    granteeOrgProgram.programId)); // sending an empty string as
                                                   // orgId
        }
        GetOrgProgramsRes getOrgProgramsRes = new GetOrgProgramsRes();
        if (CollectionUtils.isNotEmpty(programs)) {
            getOrgProgramsRes.totalHits = totalHits.getValue() + totalProgramHits.getValue();
            for (OrgProgram program : programs) {
                if (null == program) {
                    continue;
                }
                OrgProgramInfo programInfo = new OrgProgramInfo(program._getStringId(),
                        program.getName(), program.code, program.recordState, program.periodEnd,
                        program.isOffline, program.category, program.sharedProgramAccess);
                programInfo.orgId = program.orgId;
                getOrgProgramsRes.list.add(programInfo);
            }
        }
        return getOrgProgramsRes;
    }

	public static AddOrgProgramRes addProgram(AddOrgProgramReq addOrgProgramReq)
			throws VedantuException {

		OrgProgram orgProgram = OrgProgramDAO.INSTANCE.addProgram(
				addOrgProgramReq.orgId, addOrgProgramReq.code,
				addOrgProgramReq.name, addOrgProgramReq.departmentId,
				addOrgProgramReq.description, addOrgProgramReq.periodStart,
				addOrgProgramReq.periodEnd, addOrgProgramReq.isOffline, addOrgProgramReq.sharedProgramAccess, addOrgProgramReq.category);

		AddOrgProgramRes addOrgProgramRes = new AddOrgProgramRes();
		addOrgProgramRes.id = orgProgram._getStringId();
		addOrgProgramRes.recordState = orgProgram.recordState;

		return addOrgProgramRes;
	}

	public static UpdateOrgProgramRes updateProgram(
			UpdateOrgProgramReq updateOrgProgramReq) throws VedantuException {
		OrgProgram orgProgram = OrgProgramDAO.INSTANCE.updateProgram(
				updateOrgProgramReq.orgId, updateOrgProgramReq.programId,
				updateOrgProgramReq.code, updateOrgProgramReq.name,
				updateOrgProgramReq.departmentId,
				updateOrgProgramReq.description,
				updateOrgProgramReq.periodStart, updateOrgProgramReq.periodEnd,
				updateOrgProgramReq.isOffline, updateOrgProgramReq.category);

		UpdateOrgProgramRes updateOrgProgramRes = new UpdateOrgProgramRes();
		updateOrgProgramRes.id = orgProgram._getStringId();
		updateOrgProgramRes.recordState = orgProgram.recordState;

		return updateOrgProgramRes;
	}

	public static RemoveOrgProgramRes removeProgram(
			RemoveOrgProgramReq removeOrgProgramReq) throws VedantuException {

		OrgProgram orgProgram = OrgProgramDAO.INSTANCE.removeProgram(
				removeOrgProgramReq.orgId, removeOrgProgramReq.programId);

		// TODO: Remove from all corresponding programs

		RemoveOrgProgramRes removeOrgProgramRes = new RemoveOrgProgramRes();
		removeOrgProgramRes.id = orgProgram._getStringId();
		removeOrgProgramRes.recordState = orgProgram.recordState;

		return removeOrgProgramRes;
	}

	public static ActivateOrgProgramRes activateProgram(
			ActivateOrgProgramReq activateOrgProgramReq)
			throws VedantuException {

		OrgProgram orgProgram = OrgProgramDAO.INSTANCE.activateProgram(
				activateOrgProgramReq.orgId, activateOrgProgramReq.programId);

		// TODO: Activate from all corresponding programs ??

		ActivateOrgProgramRes activateOrgProgramRes = new ActivateOrgProgramRes();
		activateOrgProgramRes.id = orgProgram._getStringId();
		activateOrgProgramRes.recordState = orgProgram.recordState;

		return activateOrgProgramRes;
	}

	// TODO : this needs to verify the programId and OrgId is valid

	public static GetOrgProgramCentersRes getProgramCenters(
			GetOrgProgramCentersReq getOrgProgramCentersReq)
			throws VedantuException {



		//MutableLong totalHits = new MutableLong(0L);
        // TODO : add friend org programs
        MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getOrgProgramCentersReq.orgId, null, totalProgramHits);
        List<String> allOrgIds = new ArrayList<String>();
        LOGGER.debug("OrgProgramManager getPrograms"+granteeOrgPrograms+" size="+granteeOrgPrograms.size());

        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
			allOrgIds.add(granteeOrgProgram.providerOrgId);
		}
        allOrgIds.add(getOrgProgramCentersReq.orgId);

		OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(
				allOrgIds,
				getOrgProgramCentersReq.programId);

		GetOrgProgramCentersRes getOrgProgramCentersRes = new GetOrgProgramCentersRes();
		if (null != program
				&& CollectionUtils.isNotEmpty(program.centersSections)) {
			List<ObjectId> centerIds = program._getCentersAsObjectIds();
			List<OrgCenter> orgCenters = OrgCenterDAO.INSTANCE
					.getCentersByIds(centerIds);
			List<OrgCenterInfo> centerInfos = OrgCenterManager
					.toOrgCenterInfo(orgCenters);

			getOrgProgramCentersRes.list.addAll(centerInfos);
			getOrgProgramCentersRes.totalHits = centerInfos.size();
		}
		return getOrgProgramCentersRes;
	}

	public static AddOrgProgramCentersRes addProgramCenters(
			AddOrgProgramCentersReq addOrgProgramCentersReq)
			throws VedantuException {

		Set<String> tCenterIds = null == addOrgProgramCentersReq.centerIds ? null
				: new HashSet<String>(addOrgProgramCentersReq.centerIds);
		OrgProgramCenterSectionsDetails details = OrgProgramDAO.INSTANCE
				.addProgramCenter(addOrgProgramCentersReq.orgId,
						addOrgProgramCentersReq.programId, tCenterIds);

		AddOrgProgramCentersRes addOrgProgramCentersRes = new AddOrgProgramCentersRes();
		addOrgProgramCentersRes.id = details.programId;
		addOrgProgramCentersRes.recordState = details.recordState;
		addOrgProgramCentersRes.centerSections = details.centerSections;
		addOrgProgramCentersRes.isAdded = details.isAdded;

		return addOrgProgramCentersRes;
	}

	public static RemoveOrgProgramCentersRes removeProgramCenters(
			RemoveOrgProgramCentersReq removeOrgProgramCentersReq)
			throws VedantuException {

		Set<String> tCenterIds = null == removeOrgProgramCentersReq.centerIds ? null
				: new HashSet<String>(removeOrgProgramCentersReq.centerIds);
		OrgProgramCenterSectionsDetails details = OrgProgramDAO.INSTANCE
				.removeProgramCenter(removeOrgProgramCentersReq.orgId,
						removeOrgProgramCentersReq.programId, tCenterIds);

		RemoveOrgProgramCentersRes removeOrgProgramCentersRes = new RemoveOrgProgramCentersRes();
		removeOrgProgramCentersRes.id = details.programId;
		removeOrgProgramCentersRes.recordState = details.recordState;
		removeOrgProgramCentersRes.centerSections = details.centerSections;
		removeOrgProgramCentersRes.isRemoved = !details.isAdded;

		return removeOrgProgramCentersRes;
	}

	public static GetOrgSectionsRes getProgramSections(
			GetOrgSectionsReq getOrgSectionsReq) throws VedantuException {

		LOGGER.debug("......entering getProgramSections function......");

		 MutableLong totalProgramHits = new MutableLong(0L);
	        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getOrgSectionsReq.orgId, null, totalProgramHits);
	        List<String> allOrgIds = new ArrayList<String>();
	        LOGGER.debug("OrgProgramManager getPrograms"+granteeOrgPrograms+" size="+granteeOrgPrograms.size());

	        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
				allOrgIds.add(granteeOrgProgram.providerOrgId);
			}
	        allOrgIds.add(getOrgSectionsReq.orgId);


		OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(
				allOrgIds, getOrgSectionsReq.programId);
		LOGGER.debug("found program: " + program._getStringId());

		GetOrgSectionsRes getOrgSectionsRes = new GetOrgSectionsRes();

		OrgProgramCenterSections c = program
				._getOrgProgramCenterSections(getOrgSectionsReq.centerId);

		if (null == c) {
			LOGGER.debug("could not find center for orgId: "
					+ getOrgSectionsReq.orgId + ", programId: "
					+ getOrgSectionsReq.programId + ", centerId: "
					+ getOrgSectionsReq.centerId);
			return getOrgSectionsRes;
		}
		if (CollectionUtils.isEmpty(c.sectionIds)) {
			LOGGER.debug("could not find sections for orgId: "
					+ getOrgSectionsReq.orgId + ", programId: "
					+ getOrgSectionsReq.programId + ", centerId: "
					+ getOrgSectionsReq.centerId);
			return getOrgSectionsRes;
		}

		List<ObjectId> sectionIds = ObjectIdUtils.toObjectIds(c.sectionIds);
		List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionsByIds(
				getOrgSectionsReq.orgId, getOrgSectionsReq.programId,
				sectionIds, getOrgSectionsReq.accessScope,
				getOrgSectionsReq.revenueModel, null, MongoManager.NO_START,
				MongoManager.NO_LIMIT, new MutableLong());
		if (CollectionUtils.isEmpty(sections)) {
			LOGGER.debug("could not convert sectionIds to sections for orgId: "
					+ getOrgSectionsReq.orgId + ", programId: "
					+ getOrgSectionsReq.programId + ", centerId: "
					+ getOrgSectionsReq.centerId + ", sectionIds: ["
					+ StringUtils.join(c.sectionIds, ", ") + "]");
			return getOrgSectionsRes;
		}
		List<OrgSectionInfo> sectionInfos = OrgSectionManager
				.toOrgSectionInfo(sections);

		getOrgSectionsRes.list.addAll(sectionInfos);
		getOrgSectionsRes.totalHits = sectionInfos.size();
		return getOrgSectionsRes;
	}

	public static AddOrgSectionRes addProgramSection(
			AddOrgSectionReq addOrgSectionReq) throws VedantuException {

		LOGGER.info("......entering addSection function......");
		OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(
				addOrgSectionReq.orgId, addOrgSectionReq.programId);
		LOGGER.debug("found program: " + program._getStringId());

		OrgCenter center = OrgCenterDAO.INSTANCE.getCenterById(
				addOrgSectionReq.orgId, addOrgSectionReq.centerId);
		LOGGER.debug("found center: " + center._getStringId());

		OrgProgramCenterSections c = program
				._getOrgProgramCenterSections(addOrgSectionReq.centerId);
		if (null == c) {
			LOGGER.debug("could not find center for adding section orgId: "
					+ addOrgSectionReq.orgId + ", programId: "
					+ addOrgSectionReq.programId + ", centerId: "
					+ addOrgSectionReq.centerId);
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_CENTER_NOT_FOUND);
		}

		final boolean returnExisting = false;
		AddOrgSectionRes addOrgSectionRes = OrgSectionManager.addSection(
				addOrgSectionReq, returnExisting);

		boolean result = OrgProgramDAO.INSTANCE.addCenterSection(
				addOrgSectionReq.orgId, addOrgSectionReq.programId,
				addOrgSectionReq.centerId, addOrgSectionRes.id);
		LOGGER.debug("section addition result: " + result);

		return addOrgSectionRes;
	}

	public static RemoveOrgSectionRes removeProgramSection(
			RemoveOrgSectionReq removeOrgSectionReq) throws VedantuException {

		OrgSection orgSection = OrgSectionDAO.INSTANCE.getSectionById(
				removeOrgSectionReq.orgId, removeOrgSectionReq.sectionId);

		OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(
				removeOrgSectionReq.orgId, orgSection.programId);
		LOGGER.debug("found program: " + program._getStringId());

		RemoveOrgSectionRes removeOrgSectionRes = OrgSectionManager
				.removeSection(removeOrgSectionReq);

		boolean result = OrgProgramDAO.INSTANCE.removeCenterSection(
				removeOrgSectionReq.orgId, orgSection.programId,
				orgSection.centerId, removeOrgSectionReq.sectionId);
		LOGGER.debug("section removal result: " + result);

		return removeOrgSectionRes;
	}

	public static AddOrgProgramCoursesRes addProgramCourses(
			AddOrgProgramCoursesReq addOrgProgramCoursesReq)
			throws VedantuException {

		Set<String> tCourseIds = null == addOrgProgramCoursesReq.courseIds ? null
				: new HashSet<String>(addOrgProgramCoursesReq.courseIds);

		MutableBoolean isAdded = new MutableBoolean(false);
		OrgProgram program = OrgProgramDAO.INSTANCE.addProgramCourses(
				addOrgProgramCoursesReq.orgId,
				addOrgProgramCoursesReq.programId, tCourseIds, isAdded);

		AddOrgProgramCoursesRes addOrgProgramCoursesRes = new AddOrgProgramCoursesRes();
		addOrgProgramCoursesRes.id = program._getStringId();
		addOrgProgramCoursesRes.recordState = program.recordState;
		addOrgProgramCoursesRes.courseIds = program.courseIds;
		addOrgProgramCoursesRes.isAdded = isAdded.getValue();

		return addOrgProgramCoursesRes;
	}

	public static RemoveOrgProgramCoursesRes removeProgramCourses(
			RemoveOrgProgramCoursesReq removeOrgProgramCoursesReq)
			throws VedantuException {

		Set<String> tCourseIds = null == removeOrgProgramCoursesReq.courseIds ? null
				: new HashSet<String>(removeOrgProgramCoursesReq.courseIds);

		MutableBoolean isRemoved = new MutableBoolean(false);
		OrgProgram program = OrgProgramDAO.INSTANCE.removeProgramCourses(
				removeOrgProgramCoursesReq.orgId,
				removeOrgProgramCoursesReq.programId, tCourseIds, isRemoved);

		RemoveOrgProgramCoursesRes removeOrgProgramCoursesRes = new RemoveOrgProgramCoursesRes();
		removeOrgProgramCoursesRes.id = program._getStringId();
		removeOrgProgramCoursesRes.recordState = program.recordState;
		removeOrgProgramCoursesRes.courseIds = program.courseIds;
		removeOrgProgramCoursesRes.isRemoved = isRemoved.getValue();

		return removeOrgProgramCoursesRes;
	}

	public static GetOrgProgramCoursesRes getProgramCourses(
			GetOrgProgramCoursesReq getOrgProgramCoursesReq)
			throws VedantuException {

		//Get all the orgIds that gave access to the current organizations programs
        MutableLong totalProgramHits = new MutableLong(0L);
   	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getOrgProgramCoursesReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        /*
         *
         * If programid is null
         * 	get  programcours with currentorg, programid
         * 	for each of grateeorgprogram of this org
         * 		clll getprogramcourses ( granteprogram.properorg, grateeprog.programid)
         *
         * IF NOT NULL{
         * 	BUILD ORGANIATION LIST AND PASS THIS TO
         * }
         */
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
			grantedOrgs.add(granteeOrgProgram.providerOrgId);
			LOGGER.debug("......entering for loop getPrograCourses......"+ grantedOrgs);
		}


		grantedOrgs.add(getOrgProgramCoursesReq.orgId);
		Set<BoardBasicInfo> courseBoardInfos = OrgProgramDAO.INSTANCE
				.getProgramCourses(grantedOrgs, getOrgProgramCoursesReq.programId);
		GetOrgProgramCoursesRes getOrgProgramCoursesRes = new GetOrgProgramCoursesRes();
		getOrgProgramCoursesRes.list = new ArrayList<BoardBasicInfo>(
				courseBoardInfos);
        Collections.sort(getOrgProgramCoursesRes.list, BoardBasicInfo.COMPARATOR);
		getOrgProgramCoursesRes.totalHits = CollectionUtils
				.size(courseBoardInfos);

		return getOrgProgramCoursesRes;
	}

	public static GetOrgCourseProgramsRes getCoursePrograms(
			GetOrgCourseProgramsReq getOrgCourseProgramsReq)
			throws VedantuException {

		//Get all the orgIds that gave access to the current organization
        MutableLong totalProgramHits = new MutableLong(0L);
   	 	List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getOrgCourseProgramsReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
			grantedOrgs.add(granteeOrgProgram.providerOrgId);
			LOGGER.debug("......entering for loop getCoursePrograms......"+ grantedOrgs);
		}


		grantedOrgs.add(getOrgCourseProgramsReq.orgId);

		List<OrgStructureBasicInfo> programBasicInfos = OrgProgramDAO.INSTANCE
				.getCoursePrograms(grantedOrgs, getOrgCourseProgramsReq.courseId);

		GetOrgCourseProgramsRes getOrgCourseProgramsRes = new GetOrgCourseProgramsRes();
		getOrgCourseProgramsRes.list = programBasicInfos;
		getOrgCourseProgramsRes.totalHits = CollectionUtils
				.size(programBasicInfos);

		return getOrgCourseProgramsRes;
	}

	public static Set<String> getProgramSections(String orgId, String programId)
			throws VedantuException {

		return getProgramSections(orgId, programId, null);
	}

	/**
	 * get all sectionids
	 *
	 * @param orgId
	 * @param srcEntity
	 * @return set of sectionIds
	 * @throws VedantuException
	 */
	public static Set<String> getProgramSections(String orgId,
			String programId, List<String> centerIds) throws VedantuException {

		Set<String> sectionIds = new HashSet<String>();
		OrgProgram program = null;

		boolean collectAllSections = false;
		if (CollectionUtils.isEmpty(centerIds)) {
			// if content added for few centres
			collectAllSections = true;
		}
		program = OrgProgramDAO.INSTANCE.getProgramById(orgId, programId);
		if (program == null) {
			return sectionIds;
		}

		if (CollectionUtils.isNotEmpty(program.centersSections)) {
			for (OrgProgramCenterSections centerSection : program.centersSections) {

				if (CollectionUtils.isEmpty(centerSection.sectionIds)) {
					continue;
				}

				if (collectAllSections) {

					sectionIds.addAll(centerSection.sectionIds);
				} else if (centerIds.contains(centerSection.centerId)) {
					sectionIds.addAll(centerSection.sectionIds);

				}
			}
		}

		return sectionIds;
	}

	public static List<OrgProgramBasicInfo> getProgramBySectionIds(
			Collection<String> sectionIds, boolean addSectionInfos) {

		List<OrgProgramBasicInfo> orgPrograms = new ArrayList<OrgProgramBasicInfo>();
		DBObject query = new BasicDBObject("centersSections.sectionIds",
				new BasicDBObject(MongoManager.IN_QUERY, sectionIds.toArray()));
		VedantuDBResult<OrgProgram> results = OrgProgramDAO.INSTANCE.getInfos(
				query,
				null,
				MongoManager.NO_START,
				MongoManager.NO_LIMIT,
				MongoManager.getSortQuery(ConstantsGlobal.NAME,
						SortOrder.ASC.name()));
		for (OrgProgram orgProgram : results.results) {
			OrgProgramBasicInfo info = getOrgProgramBasicInfo(orgProgram,
					false, addSectionInfos, sectionIds);
			orgPrograms.add(info);
		}
		return orgPrograms;
	}

	public static OrgProgramBasicInfo getOrgProgramBasicInfo(String programId,
			boolean includeAllCenters, boolean addSectionInfo,
			Collection<String> forSectionIds) throws VedantuException {

		OrgProgram program = OrgProgramDAO.INSTANCE.getById(programId);
		if (program == null) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND,
					"program not found");
		}
		OrgProgramBasicInfo programBasicInfo = getOrgProgramBasicInfo(program,
				includeAllCenters, true, null);
		return programBasicInfo;
	}

	public static OrgProgramBasicInfo getOrgProgramBasicInfo(
			OrgProgram orgProgram, boolean includeAllCenters,
			boolean addSectionInfo, Collection<String> forSectionIds) {

		OrgProgramBasicInfo programInfo = (OrgProgramBasicInfo) orgProgram
				.toBasicInfo();
		if (!addSectionInfo) {
			return programInfo;
		}
		if (forSectionIds == null) {
			forSectionIds = new HashSet<String>();
			;
		}

		Set<String> centerIds = new HashSet<String>(orgProgram._getCenterIds(
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
		}
		return programInfo;
	}

	public static GetOrgSectionRes getProgramSection(GetOrgSectionReq request)
			throws VedantuException {

		OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(
				request.orgId, request.userId);

		if (member == null) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}

		OrgSection section = OrgSectionDAO.INSTANCE.getById(request.sectionId);
		if (section == null) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
		}
		OrgProgramSectionBasicInfo sectionInfo = new OrgProgramSectionBasicInfo(
				section._getStringId(), section.recordState, section.getName(),
				section.code, EntityType.SECTION);
		sectionInfo.thumbnail = section.thumbnail;
		sectionInfo.addSectionExtraInfo(section);
		boolean found = false;
        for (OrgMemberMappingInfo info : member.mappings) {
            if (info.endTime <= 0 || info.endTime > System.currentTimeMillis()) {
                if (sectionInfo.id.equals(info.sectionId)) {
                    sectionInfo.timeJoined = info.timeJoined;
                    sectionInfo.endTime = info.endTime;
                    sectionInfo.orderId = info.orderId;
                    found = true;
                    break;
                }
            }

		}
		if (!found) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
		}

		GetOrgSectionRes response = new GetOrgSectionRes();
		response.info = sectionInfo;
		return response;
	}

	public static GetPaymentInfoRes getProgramPaymentInfo(GetPaymentInfoReq request)
			throws VedantuException {
		OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(
				request.orgId, request.userId);
		if (member == null) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
		}
		long orderId = 0;
		boolean found = false;
		for (OrgMemberMappingInfo info : member.mappings) {
			if (info.endTime <= 0 || info.endTime > System.currentTimeMillis()) {
				if (request.sectionId.equals(info.sectionId)) {
					if(info.orderId.equals(null) || info.orderId.isEmpty()){
						orderId = 0;
					}else{
						long orderIdLong = Long.parseLong(info.orderId);
						orderId = orderIdLong;
					}
					found = true;
					break;
				}
			}
		}
		if (!found) {
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_MEMBER_MAPPING_NOT_FOUND);
		}
		InvoiceInfo invoiceInfo = new InvoiceInfo();
		GetPaymentInfoRes response = new GetPaymentInfoRes();
		if(orderId !=0){
			Order order = OrderDAO.INSTANCE.getOrderById(orderId);
			LOGGER.debug("siddhardha order: "+order);
			invoiceInfo = order.invoiceInfo;
			response.info = invoiceInfo;
		}else{
			invoiceInfo.total = 0;
			response.info = invoiceInfo;
		}
		return response;
	}

    public static GetOrgSectionRes getSectionPackageInfo(GetOrgSectionReq request)
            throws VedantuException {
        OrgMember member = OrgMemberDAO.INSTANCE.getMemberByUserId(
                request.orgId, request.userId);

        if (member == null) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
        }

        OrgSection section = OrgSectionDAO.INSTANCE.getById(request.sectionId);
        if (section == null) {
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        OrgProgramSectionBasicInfo sectionInfo = new OrgProgramSectionBasicInfo(
                section._getStringId(), section.recordState, section.getName(),
                section.code, EntityType.SECTION);
        sectionInfo.addSectionExtraInfo(section);
        GetOrgSectionRes response = new GetOrgSectionRes();
        response.info = sectionInfo;
        return response;
    }
}

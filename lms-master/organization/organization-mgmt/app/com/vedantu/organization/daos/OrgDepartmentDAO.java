package com.vedantu.organization.daos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.models.OrgDepartment;

public class OrgDepartmentDAO extends VedantuBasicDAO<OrgDepartment, ObjectId> {

	private static final ALogger LOGGER = Logger.of(OrgDepartmentDAO.class);

	public static final OrgDepartmentDAO INSTANCE = new OrgDepartmentDAO();

	private OrgDepartmentDAO() {
		super(OrgDepartment.class);
	}

	public List<OrgDepartment> getDepartments(String orgId,
			MutableLong totalHits) {
		Query<OrgDepartment> query = getQuery().filter("orgId", orgId).order(
				"cName");
		List<OrgDepartment> departments = query.asList();
		totalHits.setValue(query.countAll());
		return departments;
	}

	public OrgDepartment addDepartment(String orgId, String code, String name)
			throws VedantuException {

		OrgDepartment orgDepartment = getQuery().filter("orgId", orgId)
				.filter("code", code).get();
		try {

			if (null != orgDepartment) {
				if (VedantuRecordState.ACTIVE == orgDepartment.recordState) {
					LOGGER.error("cannot add orgDepartment as orgDepartment already exists for orgId: "
							+ orgId + ", code: " + code);
					throw new VedantuException(
							VedantuErrorCode.ORGANIZATION_DEPARTMENT_ALREADY_EXISTS);
				} else {
					LOGGER.error("changing orgDepartment recordState for orgId: "
							+ orgId
							+ ", code: "
							+ code
							+ ", _id: "
							+ orgDepartment._getStringId()
							+ ", from: "
							+ orgDepartment.recordState
							+ ", to: "
							+ VedantuRecordState.ACTIVE);
					orgDepartment.setName(name);
					markActive(orgDepartment);
					save(orgDepartment);
					return orgDepartment;
				}
			}

			orgDepartment = new OrgDepartment(orgId, code, name);
			save(orgDepartment);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_DEPARTMENT_ALREADY_EXISTS);
		}

		return orgDepartment;
	}

	private OrgDepartment getDepartmentById(String orgId, String departmentId)
			throws VedantuException {
		OrgDepartment orgDepartment = getById(departmentId);
		if (null == orgDepartment) {
			LOGGER.error("cannot find orgDepartment for _id: " + departmentId);
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_DEPARTMENT_NOT_FOUND);
		}
		if (!StringUtils.equals(orgDepartment.orgId, orgId)) {
			LOGGER.error("mismatch in orgId for department _id: "
					+ departmentId + ", expected orgId: " + orgDepartment.orgId
					+ ", found orgId: " + orgId);
			throw new VedantuException(VedantuErrorCode.INVALID_ID);
		}
		return orgDepartment;
	}

	public OrgDepartment updateDepartment(String orgId, String departmentId,
			String code, String name) throws VedantuException {

		OrgDepartment orgDepartment = getDepartmentById(orgId, departmentId);
		try {
			orgDepartment.code = code;
			orgDepartment.setName(name);
			save(orgDepartment);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_DEPARTMENT_ALREADY_EXISTS);
		}
		return orgDepartment;
	}

	public OrgDepartment removeDepartment(String orgId, String departmentId)
			throws VedantuException {

		OrgDepartment orgDepartment = getDepartmentById(orgId, departmentId);

		markDeleted(orgDepartment);
		save(orgDepartment);

		return orgDepartment;
	}

	public OrgDepartment activateDepartment(String orgId, String departmentId)
			throws VedantuException {

		OrgDepartment orgDepartment = getDepartmentById(orgId, departmentId);
		try {
			markActive(orgDepartment);
			save(orgDepartment);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_DEPARTMENT_ALREADY_EXISTS);
		}
		return orgDepartment;
	}

}

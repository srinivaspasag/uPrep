package com.vedantu.organization.managers;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.daos.OrgDepartmentDAO;
import com.vedantu.organization.models.OrgDepartment;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgDepartmentsReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgDepartmentReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgDepartmentReq;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgDepartmentsRes;
import com.vedantu.organization.pojos.responses.organizations.OrgDepartmentInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgDepartmentRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgDepartmentRes;

public class OrgDepartmentManager extends AbstractVedantuManager {

	public static GetOrgDepartmentsRes getDepartments(
			GetOrgDepartmentsReq getOrgDepartmentsReq) throws VedantuException {

		MutableLong totalHits = new MutableLong(0L);
		List<OrgDepartment> departments = OrgDepartmentDAO.INSTANCE
				.getDepartments(getOrgDepartmentsReq.orgId, totalHits);
		GetOrgDepartmentsRes getOrgDepartmentsRes = new GetOrgDepartmentsRes();
		if (CollectionUtils.isNotEmpty(departments)) {
			getOrgDepartmentsRes.totalHits = totalHits.getValue();
			for (OrgDepartment department : departments) {
				if (null == department) {
					continue;
				}
				OrgDepartmentInfo departmentInfo = new OrgDepartmentInfo(
						department._getStringId(), department.getName(),
						department.code, department.recordState);
				getOrgDepartmentsRes.list.add(departmentInfo);
			}
		}
		return getOrgDepartmentsRes;
	}

	public static AddOrgDepartmentRes addDepartment(
			AddOrgDepartmentReq addOrgDepartmentReq) throws VedantuException {

		OrgDepartment orgDepartment = OrgDepartmentDAO.INSTANCE.addDepartment(
				addOrgDepartmentReq.orgId, addOrgDepartmentReq.code,
				addOrgDepartmentReq.name);

		AddOrgDepartmentRes addOrgDepartmentRes = new AddOrgDepartmentRes();
		addOrgDepartmentRes.id = orgDepartment._getStringId();
		addOrgDepartmentRes.recordState = orgDepartment.recordState;

		return addOrgDepartmentRes;
	}

	public static UpdateOrgDepartmentRes updateDepartment(
			UpdateOrgDepartmentReq updateOrgDepartmentReq)
			throws VedantuException {

		OrgDepartment orgDepartment = OrgDepartmentDAO.INSTANCE
				.updateDepartment(updateOrgDepartmentReq.orgId,
						updateOrgDepartmentReq.departmentId,
						updateOrgDepartmentReq.code,
						updateOrgDepartmentReq.name);

		UpdateOrgDepartmentRes updateOrgDepartmentRes = new UpdateOrgDepartmentRes();
		updateOrgDepartmentRes.id = orgDepartment._getStringId();
		updateOrgDepartmentRes.recordState = orgDepartment.recordState;

		return updateOrgDepartmentRes;
	}

	public static RemoveOrgDepartmentRes removeDepartment(
			RemoveOrgDepartmentReq removeOrgDepartmentReq)
			throws VedantuException {

		OrgDepartment orgDepartment = OrgDepartmentDAO.INSTANCE
				.removeDepartment(removeOrgDepartmentReq.orgId,
						removeOrgDepartmentReq.departmentId);

		// TODO: Remove from all corresponding programs

		RemoveOrgDepartmentRes removeOrgDepartmentRes = new RemoveOrgDepartmentRes();
		removeOrgDepartmentRes.id = orgDepartment._getStringId();
		removeOrgDepartmentRes.recordState = orgDepartment.recordState;

		return removeOrgDepartmentRes;
	}

	public static ActivateOrgDepartmentRes activateDepartment(
			ActivateOrgDepartmentReq activateOrgDepartmentReq)
			throws VedantuException {

		OrgDepartment orgDepartment = OrgDepartmentDAO.INSTANCE
				.activateDepartment(activateOrgDepartmentReq.orgId,
						activateOrgDepartmentReq.departmentId);

		// TODO: Activate from all corresponding programs ??

		ActivateOrgDepartmentRes activateOrgDepartmentRes = new ActivateOrgDepartmentRes();
		activateOrgDepartmentRes.id = orgDepartment._getStringId();
		activateOrgDepartmentRes.recordState = orgDepartment.recordState;

		return activateOrgDepartmentRes;
	}

}

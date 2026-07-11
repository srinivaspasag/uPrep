package com.vedantu.organization.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.models.OrgCenter;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgCentersReq;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgCenterReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgCenterReq;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.GetOrgCentersRes;
import com.vedantu.organization.pojos.responses.organizations.OrgCenterInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgCenterRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgCenterRes;
import com.vedantu.organization.pojos.utils.OrgStructureInfoNameComparator;

public class OrgCenterManager extends AbstractVedantuManager {

	public static GetOrgCentersRes getCenters(GetOrgCentersReq getOrgCentersReq)
			throws VedantuException {
		MutableLong totalHits = new MutableLong(0L);
		List<OrgCenter> centers = OrgCenterDAO.INSTANCE.getCenters(
				getOrgCentersReq.orgId, totalHits);
		GetOrgCentersRes getOrgCentersRes = new GetOrgCentersRes();
		getOrgCentersRes.totalHits = totalHits.getValue();
		List<OrgCenterInfo> centerInfos = toOrgCenterInfo(centers);
		getOrgCentersRes.list.addAll(centerInfos);
		return getOrgCentersRes;
	}

	public static List<OrgCenterInfo> toOrgCenterInfo(List<OrgCenter> centers) {
		List<OrgCenterInfo> centerInfos = new ArrayList<OrgCenterInfo>();
		if (CollectionUtils.isNotEmpty(centers)) {
			for (OrgCenter center : centers) {
				if (null == center) {
					continue;
				}
				OrgCenterInfo centerInfo = new OrgCenterInfo(
						center._getStringId(), center.getName(), center.code,
						center.recordState);
				centerInfos.add(centerInfo);
			}
			Collections.sort(centerInfos,OrgStructureInfoNameComparator.INSTANCE);
		}
		return centerInfos;
	}

	public static AddOrgCenterRes addCenter(AddOrgCenterReq addOrgCenterReq)
			throws VedantuException {
		OrgCenter orgCenter = OrgCenterDAO.INSTANCE.addCenter(
				addOrgCenterReq.orgId, addOrgCenterReq.code,
				addOrgCenterReq.name);

		AddOrgCenterRes addOrgCenterRes = new AddOrgCenterRes();
		addOrgCenterRes.id = orgCenter._getStringId();
		addOrgCenterRes.recordState = orgCenter.recordState;

		return addOrgCenterRes;
	}

	public static UpdateOrgCenterRes updateCenter(UpdateOrgCenterReq updateOrgCenterReq)
			throws VedantuException {
		OrgCenter orgCenter = OrgCenterDAO.INSTANCE.updateCenter(
				updateOrgCenterReq.orgId, updateOrgCenterReq.centerId,
				updateOrgCenterReq.code, updateOrgCenterReq.name);

		UpdateOrgCenterRes updateOrgCenterRes = new UpdateOrgCenterRes();
		updateOrgCenterRes.id = orgCenter._getStringId();
		updateOrgCenterRes.recordState = orgCenter.recordState;

		return updateOrgCenterRes;
	}

	public static RemoveOrgCenterRes removeCenter(
			RemoveOrgCenterReq removeOrgCenterReq) throws VedantuException {
		OrgCenter orgCenter = OrgCenterDAO.INSTANCE.removeCenter(
				removeOrgCenterReq.orgId, removeOrgCenterReq.centerId);

		// TODO: Remove from all corresponding programs

		RemoveOrgCenterRes removeOrgCenterRes = new RemoveOrgCenterRes();
		removeOrgCenterRes.id = orgCenter._getStringId();
		removeOrgCenterRes.recordState = orgCenter.recordState;

		return removeOrgCenterRes;
	}

	public static ActivateOrgCenterRes activateCenter(
			ActivateOrgCenterReq activateOrgCenterReq) throws VedantuException {
		OrgCenter orgCenter = OrgCenterDAO.INSTANCE.activateCenter(
				activateOrgCenterReq.orgId, activateOrgCenterReq.centerId);

		// TODO: Activate from all corresponding programs ??

		ActivateOrgCenterRes activateOrgCenterRes = new ActivateOrgCenterRes();
		activateOrgCenterRes.id = orgCenter._getStringId();
		activateOrgCenterRes.recordState = orgCenter.recordState;

		return activateOrgCenterRes;
	}

}

package com.vedantu.organization.daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.models.OrgCenter;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class OrgCenterDAO extends VedantuBasicDAO<OrgCenter, ObjectId> {

	private static final ALogger LOGGER = Logger.of(OrgCenterDAO.class);

	public static final OrgCenterDAO INSTANCE = new OrgCenterDAO();

	private OrgCenterDAO() {
		super(OrgCenter.class);
	}

	public List<OrgCenter> getCenters(String orgId, MutableLong totalHits) {
		Query<OrgCenter> query = getQuery().filter("orgId", orgId).order(
				"cName");
		List<OrgCenter> centers = query.asList();
		totalHits.setValue(query.countAll());
		return centers;
	}

	public List<OrgCenter> getCentersByIds(List<ObjectId> centerIds) {
		List<OrgCenter> centers = getByIds(centerIds);
		return centers;
	}
	
	
	public OrgCenter addCenter(String orgId, String code, String name)
			throws VedantuException {

		OrgCenter orgCenter = getQuery().filter("orgId", orgId)
				.filter("code", code).order("cName").get();
		try {
			if (null != orgCenter) {
				if (VedantuRecordState.ACTIVE == orgCenter.recordState) {
					LOGGER.error("cannot add orgCenter as orgCenter already exists for orgId: "
							+ orgId + ", code: " + code);
					throw new VedantuException(
							VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
				} else {
					LOGGER.error("changing orgCenter recordState for orgId: "
							+ orgId + ", code: " + code + ", _id: "
							+ orgCenter._getStringId() + ", from: "
							+ orgCenter.recordState + ", to: "
							+ VedantuRecordState.ACTIVE);
					orgCenter.setName(name);
					markActive(orgCenter);
					save(orgCenter);
					return orgCenter;
				}
			}

			orgCenter = new OrgCenter(orgId, code, name);
			save(orgCenter);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
		}
		return orgCenter;
	}

	public OrgCenter getCenterById(String orgId, String centerId)
			throws VedantuException {
		OrgCenter orgCenter = getById(centerId);
		if (null == orgCenter) {
			LOGGER.error("cannot find orgCenter for _id: " + centerId);
			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
		}
		if (!StringUtils.equals(orgCenter.orgId, orgId)) {
			LOGGER.error("mismatch in orgId for center _id: " + centerId
					+ ", expected orgId: " + orgCenter.orgId
					+ ", found orgId: " + orgId);
			throw new VedantuException(VedantuErrorCode.INVALID_ID);
		}
		return orgCenter;
	}

	public OrgCenter getCenterById(String centerId)
            throws VedantuException {
        OrgCenter orgCenter = getById(centerId);
        if (null == orgCenter) {
            LOGGER.error("cannot find orgCenter for _id: " + centerId);
            throw new VedantuException(
                    VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
        }
        return orgCenter;
    }

	public OrgCenter updateCenter(String orgId, String centerId, String code,
			String name) throws VedantuException {

		OrgCenter orgCenter = getCenterById(orgId, centerId);
		try {
			orgCenter.code = code;
			orgCenter.setName(name);
			save(orgCenter);
		} catch (DuplicateKey exception) {

			throw new VedantuException(
					VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
		}
		return orgCenter;
	}

	public OrgCenter removeCenter(String orgId, String centerId)
			throws VedantuException {

		OrgCenter orgCenter = getCenterById(orgId, centerId);

		markDeleted(orgCenter);
		save(orgCenter);

		return orgCenter;
	}

	public OrgCenter activateCenter(String orgId, String centerId)
			throws VedantuException {

		OrgCenter orgCenter = getCenterById(orgId, centerId);
		try {
			markActive(orgCenter);
			save(orgCenter);
			} catch (DuplicateKey exception) {
	
				throw new VedantuException(
						VedantuErrorCode.ORGANIZATION_CENTER_ALREADY_EXISTS);
			}
		return orgCenter;
	}

	public Map<String, OrgStructureBasicInfo> getBasicInfosByIds(Set<String> ids) {
		List<OrgCenter> results = getByIds(ObjectIdUtils
				.toObjectIds(new ArrayList<String>(ids)));
		Map<String, OrgStructureBasicInfo> basicInfoMap = toBasicInfosMap(results);
		return basicInfoMap;
	}

	public Map<String, OrgStructureBasicInfo> getBasicInfosByCode(String orgId,
			Set<String> codes) {
		List<OrgCenter> centers = getQuery().filter("orgId", orgId)
				.field("code").hasAnyOf(codes).order("cName").asList();
		List<ModelBasicInfo> centerBasicInfos = toBasicInfos(centers);

		Map<String, OrgStructureBasicInfo> codeToCenter = new HashMap<String, OrgStructureBasicInfo>();
		for (ModelBasicInfo centerBasicInfo : centerBasicInfos) {
			OrgStructureBasicInfo oCenterBasicInfo = (OrgStructureBasicInfo) centerBasicInfo;
			codeToCenter.put(oCenterBasicInfo.code, oCenterBasicInfo);
		}

		return codeToCenter;
	}
}

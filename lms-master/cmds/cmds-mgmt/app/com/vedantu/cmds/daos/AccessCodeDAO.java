package com.vedantu.cmds.daos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.cmds.models.AccessCode;
import com.vedantu.cmds.models.SDCardGroup;
import com.vedantu.cmds.pojos.AccessCodeInfo;
import com.vedantu.cmds.pojos.SellableItemInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.UniqueCodeUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.user.pojos.UserInfo;

public class AccessCodeDAO<T extends VedantuBaseMongoModel, K> extends
		VedantuBasicDAO<AccessCode, ObjectId> {

	private static final ALogger LOGGER = Logger.of(CMDSAssignmentDAO.class);

	public static final AccessCodeDAO INSTANCE = new AccessCodeDAO();

	private AccessCodeDAO() {

		super(AccessCode.class);
	}

	public AccessCode generateAccessCode(AccessCode accessCode)
			throws VedantuException {

		accessCode.code = UniqueCodeUtils.generateUniqueCode("AccessCode");
		save(accessCode);
		return accessCode;
	}

	public AccessCode update(AccessCode accessCode) throws VedantuException {

		save(accessCode);
		return accessCode;
	}

	@SuppressWarnings("unchecked")
	public ListResponse<AccessCodeInfo> getAccessCodes(String orgId,
			String buyerEmail, String pointOfSale, String sellerReferenceNo,
			String userId, int start, int size, String orderBy, String sortOrder)
			throws VedantuException {

		DBObject accessCodeQuery = new BasicDBObject();
		DBObject order = MongoManager.getSortQuery(orderBy, sortOrder);
		accessCodeQuery.put("orgId", orgId);
		if (StringUtils.isNotEmpty(userId)) {
			accessCodeQuery.put("userId", userId);
		}
		if (StringUtils.isNotEmpty(buyerEmail)) {
			accessCodeQuery.put("buyerContactDetails.email", buyerEmail.trim());
		}
		if (StringUtils.isNotEmpty(pointOfSale)) {
			accessCodeQuery.put("sellerInfo.pointOfSale", pointOfSale.trim());
		}
		if (StringUtils.isNotEmpty(sellerReferenceNo)) {
			accessCodeQuery.put("sellerInfo.sellerReferenceNo",
					sellerReferenceNo.trim());
		}

		LOGGER.debug("...........mid get access codes.........");

		ListResponse<AccessCodeInfo> codesQueryInfo = new ListResponse<AccessCodeInfo>();
		VedantuDBResult<AccessCode> accessCodesQueryInfo = AccessCodeDAO.INSTANCE
				.getInfos(accessCodeQuery, null, start, size, order);
		codesQueryInfo.totalHits = accessCodesQueryInfo.totalHits;

		Set<String> userIds = new HashSet<String>();

		for (AccessCode accessCode : accessCodesQueryInfo.results) {
			AccessCodeInfo accessCodeInfo = (AccessCodeInfo) accessCode
					.toBasicInfo();
			codesQueryInfo.list.add(accessCodeInfo);
			userIds.add(accessCode.userId);
		}

		Map<String, ModelBasicInfo> users = OrgMemberManager.getUserInfoMap(
				orgId, userIds, true);
		for (AccessCodeInfo accessCode : codesQueryInfo.list) {
			accessCode.userInfo = (UserInfo) users.get(accessCode.userId);
		}

		return codesQueryInfo;
	}

	public ListResponse<SellableItemInfo> getSellableItems(String orgId,
			EntityType type, String name, RevenueModel revenueModel,
			AccessScope accessScope, int start, int size)
			throws VedantuException {

		DBObject sellableItemQuery = new BasicDBObject();



		if (revenueModel != null) {
			sellableItemQuery.put("revenueModel", revenueModel.toString());
		}

		if (accessScope != null) {
			sellableItemQuery.put("accessScope", accessScope.toString());
		}

		if (StringUtils.isNotEmpty(name)) {
			sellableItemQuery.put("name", Pattern.compile(".*" + name + ".*",
					Pattern.CASE_INSENSITIVE));
		}
		ListResponse<SellableItemInfo> itemsQueryInfo = new ListResponse<SellableItemInfo>();

		if (type == EntityType.SECTION) {
			sellableItemQuery.put("orgId", orgId.trim());
			VedantuDBResult<OrgSection> sellableItemsQueryInfo = OrgSectionDAO.INSTANCE
					.getInfos(sellableItemQuery, null, start, size, null);
			itemsQueryInfo.totalHits = sellableItemsQueryInfo.totalHits;
			for (OrgSection orgSection : sellableItemsQueryInfo.results) {
				ModelBasicInfo orgSectionInfo = (ModelBasicInfo) orgSection
						.toBasicInfo();
				SellableItemInfo item = new SellableItemInfo();
				item.info = orgSectionInfo;
				itemsQueryInfo.list.add(item);
			}
		}

		if (type == EntityType.SDCARDGROUP) {
			sellableItemQuery.put("contentSrc.type", EntityType.ORGANIZATION.toString());
			sellableItemQuery.put("contentSrc.id", orgId.trim());
			VedantuDBResult<SDCardGroup> sellableItemsQueryInfo = SDCardGroupDAO.INSTANCE
					.getInfos(sellableItemQuery, null, start, size, null);
			itemsQueryInfo.totalHits = sellableItemsQueryInfo.totalHits;
			for (SDCardGroup sdCardGroup : sellableItemsQueryInfo.results) {
				ModelBasicInfo orgSectionInfo = (ModelBasicInfo) sdCardGroup
						.toBasicInfo();
				SellableItemInfo item = new SellableItemInfo();
				item.info = orgSectionInfo;
				itemsQueryInfo.list.add(item);
			}
		}
		return itemsQueryInfo;
	}

	public AccessCode getAccessCodeById(String accessCodeId)
			throws VedantuException {

		AccessCode accessCode = getById(accessCodeId);
		if (accessCode == null) {
			throw new VedantuException(
					VedantuErrorCode.ACCESS_CODE_DOES_NOT_EXISTS,
					"no access code found with id : " + accessCodeId);
		}
		return accessCode;
	}

	public AccessCode getByCodeAndEmailId(String code, String email)
			throws VedantuException {
                 AccessCode accessCode = getDS().find(AccessCode.class).filter("code",code).get();
                 if (accessCode.buyerContactDetails.email != null && !accessCode.buyerContactDetails.email.equals("") && !accessCode.buyerContactDetails.email.equals(email) ){
			accessCode = null;
                 }

		if (accessCode == null) {
			throw new VedantuException(VedantuErrorCode.INVALID_CODE);
		}
		return accessCode;
	}

}

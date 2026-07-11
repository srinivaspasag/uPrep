package com.lms.managers.news;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.NewsContext;
import com.lms.enums.OrgMemberProfile;
import com.lms.models.OrgMember;
import com.lms.models.Remark;
import com.lms.pojo.OrgMemberMappingInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSecuritySet {

	private static final Logger logger = LoggerFactory.getLogger(UserSecuritySet.class);
	public NewsContext contextType = NewsContext.NEWSFEED;
	private String userId;
	private String orgId;
	private OrgMember memberInfo;
	private boolean needAuthorization;

	//
	// private Set<SrcEntity> allowedEntitySet = new HashSet<SrcEntity>();
	// private Set<SrcEntity> restrictedEntitySet = new HashSet<SrcEntity>();

	public UserSecuritySet(String userId, String orgId) {

		this.userId = userId;

		this.orgId = orgId;
		/*if (StringUtils.isNotEmpty(orgId)) {
			memberInfo = OrgMemberDAO.INSTANCE.getMemberByUserId(orgId, userId);
		}*/

	}

	public String getOrgId() {

		return orgId;
	}

	public void setOrgId(String orgId) {

		this.orgId = orgId;
	}

	public String getUserId() {

		return userId;
	}

	public void setUserId(String userId) {

		this.userId = userId;
	}

	public NewsContext getContextType() {

		return contextType;
	}

	public void setContextType(NewsContext contextType) {

		this.contextType = contextType;
	}

	public boolean validate(NewsActivity info, VedantuBaseMongoModel srcModel) {


		return validateAuthorization(info, srcModel);
	}

	private boolean validateAuthorization(NewsActivity info, VedantuBaseMongoModel srcModel) {

		if (!needAuthorization) {
			return true;
		}
		boolean authorized = !needAuthorization;
		// TODO update it using entity based security validator where share
		// check is common

		// TODO: verify these profile access to ILE
		if (memberInfo.profile == OrgMemberProfile.MANAGER) {

			/**
			 * || memberInfo.profile == OrgMemberProfile.TEACHER || memberInfo.profile ==
			 * OrgMemberProfile.EDITOR
			 */

			return true;
		}

		if (info.src.type == EntityType.REMARK) {
			Remark remark = (Remark) srcModel;
			logger.debug(" ProvideeId check " + remark.provideeId);
			if (memberInfo.profile != OrgMemberProfile.TEACHER
					&& (memberInfo.profile == OrgMemberProfile.STUDENT && !remark.provideeId.equals(userId))) {
				logger.debug("Remark can not be shown ot requester " + userId);
				return false;
			}
			logger.debug("Remark : " + remark + "Remark can be shown to requester " + userId);

			return true;
		}

		if (verifySharing(info, memberInfo, authorized)) {
			return true;
		}

		return authorized;
	}

	public void setNeedAuthorization(boolean needAuthorization) {

		this.needAuthorization = needAuthorization;
	}

	private boolean verifySharing(NewsActivity info, OrgMember memberInfo, boolean authorized) {

		if (authorized) {
			return authorized;
		}

		switch (info.eType) {
			case MADE_VISIBLE:
				SrcEntity sections = info.sharedWith.get(0);
				if (CollectionUtils.isNotEmpty(memberInfo.mappings)) {
					for (OrgMemberMappingInfo orgMemberMappingInfo : memberInfo.mappings) {
						if (sections.id.equals(orgMemberMappingInfo.sectionId)) {
							return true;
						}
					}
				}
				break;
			case SHARE_ENTITY:
				authorized = verifyShares(info.src);
				break;
			default:
				authorized = false;
				break;

		}
		return authorized;

	}

	public VedantuBaseMongoModel checkIfExist(SrcEntity src) {

		/*@SuppressWarnings("unchecked")
		VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> entityDAO = EntityTypeDAOFactory.INSTANCE.get(src.type);

		if (entityDAO == null) {
			return null;
		}

		VedantuBaseMongoModel model = entityDAO.getById(src.id, VedantuRecordState.ACTIVE);
		if (model == null) {
			return null;
		}
		return model;*/
		return null;
	}

	public boolean verifyShares(SrcEntity entity) {

		/*SrcEntity self = new SrcEntity(EntityType.USER, userId);
		Set<SrcEntity> shares = EntityShareDAO.INSTANCE.getAllShares(entity);
		if (CollectionUtils.isEmpty(shares)) {
			return false;
		}
		if (shares.contains(self)) {
			logger.debug("Checking if entity is shared with usres himself" + self);
			return true;
		}
		logger.info("Checking if entity is shared with usres sections ");

		if (CollectionUtils.isEmpty(memberInfo.mappings)) {
			logger.debug(
					"Checking if entity is shared with user doesnt belong to any of the section ie it he has no mapping ");
			return false;
		}

		for (OrgMemberMappingInfo orgMemberMappingInfo : memberInfo.mappings) {
			SrcEntity section = new SrcEntity(EntityType.SECTION, orgMemberMappingInfo.sectionId);
			logger.debug("Checking if entity is shared with user's section" + section);
			if (shares.contains(section)) {
				return true;
			}
		}*/
		return false;
	}
}

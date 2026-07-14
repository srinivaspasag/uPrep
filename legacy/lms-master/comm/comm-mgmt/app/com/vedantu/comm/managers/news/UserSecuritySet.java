package com.vedantu.comm.managers.news;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.util.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.daos.EntityShareDAO;
import com.vedantu.comm.enums.NewsContext;
import com.vedantu.comm.models.mongo.Remark;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;

public class UserSecuritySet {

    private static final ALogger LOGGER      = Logger.of(UserSecuritySet.class);
    private String               userId;
    private String               orgId;
    private OrgMember            memberInfo;
    private boolean              needAuthorization;
    public NewsContext           contextType = NewsContext.NEWSFEED;

    //
    // private Set<SrcEntity> allowedEntitySet = new HashSet<SrcEntity>();
    // private Set<SrcEntity> restrictedEntitySet = new HashSet<SrcEntity>();

    public String getOrgId() {

        return orgId;
    }

    public void setOrgId(String orgId) {

        this.orgId = orgId;
    }

    public UserSecuritySet(String userId, String orgId) {

        this.userId = userId;

        this.orgId = orgId;
        if (StringUtils.isNotEmpty(orgId)) {
            memberInfo = OrgMemberDAO.INSTANCE.getMemberByUserId(orgId, userId);
        }

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

        // LOGGER.debug("Checking for validation: ");
        // if (allowedEntitySet.contains(info.src)) {
        // LOGGER.debug("Allowed Security :  " + info.src);
        // return true;
        // } else if (restrictedEntitySet.contains(info.src)) {
        // LOGGER.debug("Restricted Security :  " + info.src);
        // return false;
        // } else if (validateAuthorization(info)) {
        // LOGGER.debug("Succeeded Authorization :  " + info.src);
        // return allowedEntitySet.add(info.src);
        // }
        // LOGGER.debug("Failed Authorization :  " + info.src);
        // restrictedEntitySet.add(info.src);
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
            LOGGER.debug(" ProvideeId check " + remark.provideeId);
            if (memberInfo.profile != OrgMemberProfile.TEACHER
                    && (memberInfo.profile == OrgMemberProfile.STUDENT && !remark.provideeId
                            .equals(userId))) {
                LOGGER.debug("Remark can not be shown ot requester " + userId);
                return false;
            }
            LOGGER.debug("Remark : " + remark + "Remark can be shown to requester " + userId);

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
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(memberInfo.mappings)) {
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

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> entityDAO = EntityTypeDAOFactory.INSTANCE
                .get(src.type);

        if (entityDAO == null) {
            return null;
        }

        VedantuBaseMongoModel model = entityDAO.getById(src.id, VedantuRecordState.ACTIVE);
        if (model == null) {
            return null;
        }
        return model;
    }

    public boolean verifyShares(SrcEntity entity) {

        SrcEntity self = new SrcEntity(EntityType.USER, userId);
        Set<SrcEntity> shares = EntityShareDAO.INSTANCE.getAllShares(entity);
        if (CollectionUtils.isEmpty(shares)) {
            return false;
        }
        if (shares.contains(self)) {
            LOGGER.debug("Checking if entity is shared with usres himself" + self);
            return true;
        }
        LOGGER.info("Checking if entity is shared with usres sections ");

        if (CollectionUtils.isEmpty(memberInfo.mappings)) {
            LOGGER.debug("Checking if entity is shared with user doesnt belong to any of the section ie it he has no mapping ");
            return false;
        }

        for (OrgMemberMappingInfo orgMemberMappingInfo : memberInfo.mappings) {
            SrcEntity section = new SrcEntity(EntityType.SECTION, orgMemberMappingInfo.sectionId);
            LOGGER.debug("Checking if entity is shared with user's section" + section);
            if (shares.contains(section)) {
                return true;
            }   
        }
        return false;
    }
}
